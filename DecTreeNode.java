import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Possible class for internal organization of a decision tree.
 * Included to show standardized output method, print().
 * 
 * Do not modify. If you use,
 * create child class DecTreeNodeImpl that inherits the methods.
 * 
 */
public class DecTreeNode {
	String label;
	/**
	 * Question to ask children of this node
	 */
	Question nextQuestion;
	/**
	 * Answer to parent's question (the node's value)
	 */
	String attributeValue; // if is the root, set to "ROOT"
	boolean terminal;
	List<DecTreeNode> children;

	/**
	 * Constructor for a decision tree node
	 * @param _attributeValue the value of this node for the question its
	 * parent asked
	 * @param _label the label at this node for majority of its examples
	 * @param _terminal true if it is a leaf node
	 * @param _nextAttribute the attribute to ask the next
	 * @param _nextAttrThresh
	 */
	public DecTreeNode(String _attributeValue, String _label, boolean _terminal, Question _nextQ) {
		label = _label;
		nextQuestion = _nextQ;
		attributeValue = _attributeValue;
		terminal = _terminal;
		if (_terminal) {
			children = null;
		} else {
			children = new ArrayList<DecTreeNode>();
		}
	}

	/**
	 * Add child to the node.
	 * 
	 * For printing to be consistent, children should be added
	 * in order of the attribute values as specified in the
	 * dataset.
	 */
	public void addChild(DecTreeNode child) {
		if (children != null) {
			children.add(child);
		}
	}
	
	/**
	 * Prints the subtree of the node
	 */
	public void print(Question parentQ, int k) {
		
		// for root just pass the children
		if(k < 0 || parentQ == null) {
			for(DecTreeNode child: children) {
				child.print(nextQuestion, k+1);
			}
			return;
		}
		
		// print node info
		StringBuilder sb = new StringBuilder();
		
		// tabs for tree depth
		for (int i = 0; i < k; i++) {
			sb.append("|\t");
		}
		
		// question and answer values
		sb.append(parentQ.attr.name + " ");
		if(parentQ.attr.isReal) {
			if(attributeValue == "LE") {
				sb.append("<= ");
			} else {
				sb.append("> ");
			}
			DecimalFormat df = new DecimalFormat("#0.000000");
			sb.append(String.format(df.format(parentQ.threshold)));
		} else {
			sb.append("= " + attributeValue);
		}
		
		if (terminal) {
			// if a leaf, print label
			sb.append(": " + label);
			System.out.println(sb.toString());
		} else {
			// print the rest of the tree
			System.out.println(sb.toString());
			for(DecTreeNode child: children) {
				child.print(nextQuestion, k+1);
			}
		}
	}
}
