import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fill in the implementation details of the class DecisionTree
 * using this file. Any methods or secondary classes
 * that you want are fine but we will only interact
 * with those methods in the DecisionTree framework.
 * 
 * You must add code for the 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {

	// class labels
	private String label1 = null;
	private String label2 = null;

	private DecTreeNode root; // the root of the tree

	/**
	 * Build a decision tree given only a training set.
	 * 
	 * @param train the training set
	 * @param m the minimum number of training instances at a node to continue
	 * building the tree
	 */
	DecisionTreeImpl(List<Instance> train, int m) {
		
		// find class labels
		label1 = DTMain.classes.values.get(0);
		label2 = DTMain.classes.values.get(1);
		
		// build tree
		this.root = buildTree(train, "Root", m);
	}
	
	/**
	 * Recursively builds a decision tree by choosing the next question with
	 * highest entropy
	 * @param examples the instances to build from
	 * @param attribute the value of the current node
	 * @param m the minimum number of training instances at a node to continue
	 * building the tree
	 * @return the root of the finished tree
	 */
	private DecTreeNode buildTree(List<Instance> examples, String attribute, int m) {

		// find majority at this node, if tied, use default
		String label = majority(examples);
		
		// if less that m instances, choose default
		if(examples.size() < m) {
			return new DecTreeNode(attribute, label, true, null);
		}

		// if the instances are pure, return pure value
		if(entropy(examples) == 0) {
			return new DecTreeNode(attribute, label, true, null);
		}
		
		// find next question
		Question nextQuestion = bestQuestion(examples);
		
		// if no more questions, choose default
		if(nextQuestion == null) {
			return new DecTreeNode(attribute, label, true, null);
		}

		// create new node and recursively build tree for children of this node
		DecTreeNode curNode = new DecTreeNode(attribute, label, false, nextQuestion);
		List<List<Instance>> groups = groupByQuestionAns(nextQuestion, examples);
		if(nextQuestion.attr.isReal) {
			
			// add less than or equal and greater than
			curNode.addChild(buildTree(groups.get(0), "LE", m));
			curNode.addChild(buildTree(groups.get(1), "G", m));
		} else {
			
			// add each discrete choice
			for(int i = 0; i < groups.size(); i++) {
				String value = nextQuestion.attr.values.get(i);
				curNode.addChild(buildTree(groups.get(i), value, m));
			}
		}

		return curNode;
	}

	/**
	 * Predicts label based on majority vote for a set of instances
	 * @param instances the instances to find majority for
	 * @return the majority class or default if tied vote
	 */
	private String majority(List<Instance> instances) {
		
		// counts
		int sum1 = 0;
		int sum2 = 0;

		// find how many are label 1 and 2
		for(Instance inst : instances) {
			if(inst.label.equals(label1)) sum1++;
			else sum2++;
		}

		if(sum1 == sum2) {
			return label1;
		}
		if(sum1 > sum2) {
			return label1;
		} else {
			return label2;
		}
	}

	/**
	 * Calculates the entropy for a set of instances
	 * @param instances the data to find entropy for
	 * @return the entropy value
	 */
	private double entropy(List<Instance> instances) {

		// if no instances, entropy = 0
		if(instances.size() == 0) return 0;

		int sum1 = 0;
		int sum2 = 0;

		// find how many are label 1 and 2
		for(Instance inst : instances) {
			if(inst.label.equals(label1)) sum1++;
			else sum2++;
		}
		int total = sum1 + sum2;

		// calculate entropy
		double prob1 = (double)sum1 / (double)total;
		double part1;
		if(prob1 != 0) {
			part1 = -1 * prob1 * Math.log(prob1) / Math.log(2);
		} else {
			part1 = 0;
		}

		double prob2 = (double)sum2 / (double)total;
		double part2;
		if(prob2 != 0) {
			part2 = -1 * prob2 * Math.log(prob2) / Math.log(2);
		} else {
			part2 = 0;
		}

		return part1 + part2;
	}
	
	/**
	 * Finds the best question for a list of examples based on max information
	 * gain.
	 * @param examples the list of instances
	 * @return the best question, null if there are no more possible questions
	 */
	private Question bestQuestion(List<Instance> examples) {
		
		// possible questions and information gain lists
		// index matched after for loop
		List<Question> questions = possibleQuestions(examples);
		List<Double> infoGainVals = new ArrayList<Double>();
		
		// if no more questions left return null
		if(questions.size() == 0) return null;

		// for each question
		for(Question q : questions) {
			// calculate information gain for the question

			// split instances by answers
			List<List<Instance>> instancesByAttr = groupByQuestionAns(q, examples);

			// find entropy of new groupings
			double totalEntropy = 0;
			int totalInst = examples.size();
			for(int i = 0; i < instancesByAttr.size(); i++) {
				List<Instance> sameValue = instancesByAttr.get(i);
				int numWithValue = sameValue.size();
				double entropyPart = ((double)numWithValue / (double)totalInst) * entropy(sameValue);
				totalEntropy += entropyPart;
			}

			// information gain = entropy of all examples - conditional entropy
			double infoGain = entropy(examples) - totalEntropy;
			infoGainVals.add(infoGain);
		}

		// return question with max information gain
		double maxGain = -1;
		int maxGainIndex = -1;
		for(int i = 0; i < infoGainVals.size(); i++) {
			// find the question with the max gain
			// ties broken by choosing the earlier one
			if(infoGainVals.get(i) > maxGain) {
				maxGain = infoGainVals.get(i);
				maxGainIndex = i;
			}
		}

		return questions.get(maxGainIndex);
	}
	
	/**
	 * Finds possible questions to ask (all examples aren't all the same for
	 * that question).
	 * @param examples examples to determine which questions are valid
	 * @return list of questions
	 */
	private List<Question> possibleQuestions(List<Instance> examples) {
		
		ArrayList<Question> questions = new ArrayList<Question>();
		
		// for each attribute
		for(Attribute attr : DTMain.attrs) {
			int attrsIndex = DTMain.attrs.indexOf(attr);
			if(attr.isReal) {
				// find possible thresholds for real values
				
				// find values represented
				List<LabelValuePair> values = new ArrayList<LabelValuePair>();
				for(Instance i : examples) {
					String label = i.label;
					String instValue = i.attributes.get(attrsIndex);
					double value = Double.parseDouble(instValue);
					values.add(new LabelValuePair(label, value));
				}
				
				// sort by value, then label
				Collections.sort(values, LabelValuePair.labelComp);
				Collections.sort(values, LabelValuePair.valueComp);
				
				// find midpoints separating two labels
				for(int i = 0; i < values.size(); i++) {
					if(i != values.size() - 1) {
						if(values.get(i).value < values.get(i+1).value &&
								!values.get(i).label.equals(values.get(i+1))) {
							double midPoint = (values.get(i).value + values.get(i+1).value) / 2;
							questions.add(new Question(attr, midPoint));
						}
					}
				}
			} else {
				// find if discrete attribute has already been used
				
				// count instances for each value
				int[] numPerValue = new int[attr.values.size()];
				for(Instance i : examples) {
					String instValue = i.attributes.get(attrsIndex);
					int valuesIndex = attr.values.indexOf(instValue);
					numPerValue[valuesIndex]++;
				}
				
				// find number of values with values represented
				int numValsAtLeastOneInst = 0;
				for(int i : numPerValue) {
					if(i > 0) numValsAtLeastOneInst++;
				}
				
				// add only if there are more that one value represented
				if(numValsAtLeastOneInst > 1) {
					questions.add(new Question(attr, -1));
				}
			}
		}
		
		return questions;
	}
	
	/**
	 * Returns multiple lists based on their answers to a specified question,
	 * in the same order as in the attribute
	 * @param q the question to ask
	 * @param examples the list of instances to group
	 * @return a list of grouped instances
	 */
	private List<List<Instance>> groupByQuestionAns(Question q, List<Instance> examples) {

		// split instances by answers
		List<String> choicesInAttr = new ArrayList<String>();
		List<List<Instance>> instancesByAttrChoice = new ArrayList<List<Instance>>();
		
		// initialize lists
		if(q.attr.isReal) {
			choicesInAttr.add("LE");
			choicesInAttr.add("G");
			instancesByAttrChoice.add(new ArrayList<Instance>());
			instancesByAttrChoice.add(new ArrayList<Instance>());
		} else {
			for(String choice : q.attr.values) {

				// add choice name to attrChoices
				choicesInAttr.add(choice);

				// add list for each name
				instancesByAttrChoice.add(new ArrayList<Instance>());
			}
		}

		// add instances to correct list
		for(Instance inst : examples) {

			// find instances value
			String value = findValue(inst, q);

			// put instance into right list
			int attrIndex = choicesInAttr.indexOf(value);
			instancesByAttrChoice.get(attrIndex).add(inst);
		}

		return instancesByAttrChoice;
	}
	
	/**
	 * Find the attribute value for an instance and question
	 * @param inst the instance to check
	 * @param q the question to look at
	 * @return the instance's value
	 */
	private String findValue(Instance inst, Question q) {
		
		// find instance's value for current attribute
		int index = DTMain.attrs.indexOf(q.attr);
		String instVal = inst.attributes.get(index);
		
		// find threshold side if real
		if(q.attr.isReal) {
			Double val = Double.parseDouble(instVal);
			double threshVal = q.threshold;
			if(val <= threshVal) return "LE";
			else return "G";
		}
		return instVal;
	}

	@Override
	/**
	 * Evaluates the learned decision tree on a test set.
	 * @return the label predictions for each test instance 
	 * 	according to the order in data set list
	 */
	public String[] classify(List<Instance> test) {

		String[] classification = new String[test.size()];

		for(int i = 0; i < test.size(); i++) {
			Instance inst = test.get(i);
			DecTreeNode node = this.root;

			// travel decision tree
			while(!node.terminal) {

				// find value for next choice
				String value = findValue(inst, node.nextQuestion);

				// choose next child with same value
				for(DecTreeNode child : node.children) {
					if(child.attributeValue.equals(value)) {
						node = child;
						break;
					}
				}
			}
 
			// now at leaf node for current instance, record label
			classification[i] = node.label;
		}

		return classification;
	}

	@Override
	/**
	 * Prints the tree in specified format. It is recommended, but not
	 * necessary, that you use the print method of DecTreeNode.
	 * 
	 * Example:
	 * Root {Existing checking account?}
	 *   A11 (2)
	 *   A12 {Foreign worker?}
	 *     A71 {Credit Amount?}
	 *       A (1)
	 *       B (2)
	 *     A72 (1)
	 *   A13 (1)
	 *   A14 (1)
	 *         
	 */
	public void print() {
		this.root.print(null, -1);
	}

}

