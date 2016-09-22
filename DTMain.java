import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class DTMain {
	
	public static List<Attribute> attrs = new ArrayList<Attribute>();
	public static Attribute classes;

	public static void main(String[] args) {
		
		String trainFile = args[0];
		String testFile = args[1];
		
		// parse train file
		List<Instance> trainInsts = new ArrayList<Instance>();
		parseARFF(trainFile,attrs,trainInsts);
		
		// parse test file
		List<Attribute> notAttrs = new ArrayList<Attribute>();
		List<Instance> testInsts = new ArrayList<Instance>();
		parseARFF(testFile,notAttrs,testInsts);
		notAttrs = null; // don't need the duplicate
		
		int m = Integer.parseInt(args[2]);
//		int m = 4;
		
		part1(trainInsts, testInsts, m);
		//part2(trainInsts, testInsts, 4);
		//part3(trainInsts, testInsts);
	}
	
	public static void part1(List<Instance> trainInsts, List<Instance> testInsts, int m) {
		DecisionTreeImpl dc = new DecisionTreeImpl(trainInsts, m);
		dc.print();
		accuracy(dc, testInsts, true);
	}

	public static void part2(List<Instance> trainInsts, List<Instance> testInsts, int m) {
		double totalAcc = 0;
		double maxAcc = 0;
		double minAcc = 1;
		for(int i = 0; i < 10; i++) {
			List<Instance> toTrain = stratSamp(trainInsts, 200);
			DecisionTreeImpl dc = new DecisionTreeImpl(toTrain, m);
			double acc = accuracy(dc, testInsts, false);
			System.out.println(acc);
			// updates
			totalAcc += acc;
			if(acc > maxAcc) maxAcc = acc;
			if(acc < minAcc) minAcc = acc;
		}
		System.out.println("Ave: " + totalAcc / 10);
		System.out.println("Max: " + maxAcc);
		System.out.println("Min: " + minAcc);
	}
	
	public static void part3(List<Instance> trainInsts, List<Instance> testInsts) {
		int[] ms = {2,5,10,20};
		for(int m : ms) {
			DecisionTreeImpl dc = new DecisionTreeImpl(trainInsts, m);
			double acc = accuracy(dc, testInsts, false);
			System.out.println(m + ": " + acc);
		}
	}
	
	private static void parseARFF(String file, List<Attribute> attrs, List<Instance> instances) {
		
		// check file
		File in = new File(file); // file to be read
		Scanner scan; // scanner for file reading
		try {
			scan = new Scanner(in);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Cannot access input file");
			return;
		}
		
		// read attributes
		String line = null;
		while(true) {
			line = scan.nextLine();
			if(line.contains("@attribute")) {
				break;
			}
		}
		do {
			String attrName = line.substring(line.indexOf(39) + 1, line.indexOf(39, line.indexOf(39) + 1));
			boolean real = !line.contains("{");
			Attribute attr = new Attribute(attrName,real);
			
			// get attribute values
			if(!real) {
				String valStr = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
				Scanner valScan = new Scanner(valStr).useDelimiter(",| |'");
				while(valScan.hasNext()) {
					String nextStr = valScan.next();
					if(nextStr != null && !nextStr.equals("")) {
						attr.values.add(nextStr);
					}
				}
			}
			
			// add attribute
			if(!attr.name.equalsIgnoreCase("class")) {
				attrs.add(attr);
			} else {
				classes = attr;
			}
		} while((line = scan.nextLine()).contains("@attribute"));
		
		// read instances
		while(scan.hasNext()) {
			line = scan.next();
			List<String> attributes = new ArrayList<String>(Arrays.asList(line.split(",|'")));
			
			// remove empty strings
			List<String> toRemove = new ArrayList<String>();
			toRemove.add("");
			attributes.removeAll(toRemove);
			
			String label = attributes.get(attributes.size() - 1);
			attributes.remove(attributes.size() - 1);
			Instance inst = new Instance(label, attributes);
			instances.add(inst);
		}
		
	}
	
	public static double accuracy(DecisionTreeImpl dc, List<Instance> toTest, boolean print) {

		// classify test set and print results
		String[] labels = dc.classify(toTest);
		int numCorrect = 0;
		for(int i = 0; i < toTest.size(); i++) {
			
			// print attributes
			if(print) {
				for(String str : toTest.get(i).attributes) {
					System.out.print(str + " ");
				}
			}
			
			// print classifications
			String predicted = labels[i];
			String actual = toTest.get(i).label;
			if(print) System.out.println(predicted + " " + actual);
			if(predicted.equals(actual)) numCorrect++;
		}
		if(print) System.out.println(numCorrect + " " + toTest.size());
		
		return (double) numCorrect / (double) toTest.size();
	}
	
	public static List<Instance> stratSamp(List<Instance> insts, int n) {
		
		List<Instance> list1 = new ArrayList<Instance>();
		List<Instance> list2 = new ArrayList<Instance>();
		
		int num1 = 0;
		int num2 = 0;
		
		// count negative and positive
		for(Instance i : insts) {
			if(i.label.equals(classes.values.get(0))) {
				list1.add(i);
				num1++;
			} else {
				list2.add(i);
				num2++;
			}
		}
		
		// find percentage for classes
		double prob1 = (double) num1 / (double) (num1 + num2);
		int newNum1 = (int) (n * prob1);
		
		// add new instances to final list
		List<Instance> newInsts = new ArrayList<Instance>();
		Random rand = new Random();
		for(int i = 0; i < newNum1; i++) {
			int index = rand.nextInt(list1.size());
			// add to new list and delete original instance for no replacement
			newInsts.add(list1.remove(index));
		}
		for(int i = 0; i < (n - newNum1); i++) {
			int index = rand.nextInt(list2.size());
			// add to new list and delete original instance for no replacement
			newInsts.add(list2.remove(index));
		}
		
		return newInsts;
		
	}
	
}
