import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class Assgt4TreeLearning {

	public static void main(String[] args) {
		Scanner sc = null;
		try {
		sc = new Scanner(new File("src/antiques.csv"));
		}
		catch(Exception e) {
			
		}
		
		String[] featureNames = new String[] {"Pre1800", "RareMake", "RareType", "GoodCondition", "Cheap"};
		String[][] featureValues = new String[][] {{"True", "False"},{"True", "False"},{"True", "False"},{"True", "False"},{"True", "False"}};
		String outputName = "Buy";
		String[] outputValues = new String[] {"True", "False"};
		String[][] input = new String[500][];
		int numberTests = 350;
		
		sc.nextLine();
		
		int index = 0;
		while(sc.hasNextLine()) {
			input[index++] = sc.nextLine().split(",");
		}
		
		sc.close();
		
		TestProtocol tP = new TestProtocol(featureNames, featureValues, outputName, outputValues, input, numberTests);
		
		double[] ratio = new double[input.length];
		
		int count = 0;
		
		for(int i = 4; i <= ratio.length / 2; i++) {
			try {
			ratio[i] = tP.runExperimentTests(i);
			}
			catch(Exception e) {
				//System.out.println(count++ + " " + i);
				i--;
				continue;
			}
		}
		
		for(int i = 0; i <= ratio.length / 2; i++)
			System.out.println(i + ": " + ratio[i]);
	}
	
}

class TestProtocol{
	
	DecisionTree learnTree;
	String[][] testData;
	HashSet<Integer> usedExamples;
	int numberTests;
	
	public TestProtocol(String[] featureNames, String[][] featureValues, String outputName, String[] outputValues, String[][] input, int sizeTest) {
		learnTree = new DecisionTree(featureNames, featureValues, outputName, outputValues);
		testData = input;
		numberTests = sizeTest;
	}

	public double runExperimentTests(int numExamp) {
		double ave = 0.0;
		for(int i = 0; i < numberTests; i++) {
			ave += experimentRatio(numExamp);
		}
		ave /= (double)numberTests;
		return ave;
	}
	
	public double experimentRatio(int numExamp) {
		usedExamples = new HashSet<Integer>();
		String[][] teachingSet = new String[numExamp][];
		int add = 0;
		Random rand = new Random();
		while(add < numExamp) {
			int val = rand.nextInt(testData.length);
			while(usedExamples.contains(val))
				val = rand.nextInt(testData.length);
			teachingSet[add++] = testData[val];
		}
		learnTree.buildTree(teachingSet);
		return experiment();
	}
	
	private double experiment() {
		int fail = 0;
		int counter = 0;
		for(int i = 0; i < testData.length; i++) {
			if(counter == testData.length / 2)
				break;
			if(usedExamples.contains(i))
				continue;
			counter++;
			String result = learnTree.queryTree(testData[i]);
			if(!result.equals(testData[i][testData[i].length-1]))
				fail++;
		}
		return (double)fail / (double)(testData.length / 2) * 100.0;
	}
	
}

class DecisionTree{
	
	Feature[] features;
	DecisionNode root;
	String outputTitle;
	
	public DecisionTree(String[] featureNames, String[][] featureValues, String outputName, String[] outputValues) {
		features = new Feature[featureNames.length];
		for(int i = 0; i < featureNames.length; i++)
			features[i] = new Feature(featureNames[i], featureValues[i], outputValues);
		outputTitle = outputName;
	}
	
	public void buildTree(String[][] examples) {
		String[][] assume = new String[0][0];
		root = null;
		
		while(assume != null) {
			
			//Process examples to ready features for information gain
			
			top:
			for(int i = 0; i < examples.length; i++) {
				
				//Skip examples outside of the current paradigm
				
				for(int j = 0; j < features.length; j++) {
					for(int k = 0; k < assume.length; k++) {
						if(features[j].getName().equals(assume[k][0]) && !examples[i][j].equals(assume[k][1])) {
							continue top;
						}
					}
				}
				
				String result = examples[i][examples[i].length-1];
				for(int j = 0; j < features.length; j++) {
					features[j].addCount(examples[i][j], result);
				}
			}
			
			//Find the largest information gain for adding a new node
			
			double largest = -10000000;
			int index = -1;
			for(int i = 0; i < features.length; i++) {
				double val = features[i].calculateInformationGain();
				for(int j = 0; j < assume.length; j++) {
					if(features[i].getName().equals(assume[j][0]))
						continue;
				}
				if(val >= largest) {
					largest = val;
					index = i;
				}
			}
			
			//Add the new node as a feature or leaf to the tree
			

			if(root == null)
				root = new DecisionNode(features[index]);
			else {
				root.addFeatureNode(features[index]);
			}
			
			for(String s : features[index].getFeatureValueNames()) {
				if(features[index].isPure(s)) {
					root.addPureLeaf(assume, 0, s, features[index].getPureResult(s));
				}
			}
			
			//Get the next location for a node
			
			assume = root.searchForExpansion(new String[0][0]);
			
			//Reset counts for next pass of Features
			
			for(Feature f : features) {
				f.resetFeature();
			}
			
		}
	}
	
	public String queryTree(String[] input) {
		HashMap<String, String> map = new HashMap<String, String>();
		for(int i = 0; i < features.length; i++) {
			map.put(features[i].getName(), input[i]);
		}
		return root.queryAccount(map);
	}
	
}

class DecisionNode{
	
	String nodeName;
	HashMap<String, DecisionNode> childrenNodes;
	
	public DecisionNode(Feature feature) {
		nodeName = feature.getName();
		childrenNodes = new HashMap<String, DecisionNode>();
		for(String s : feature.getFeatureValueNames())
			childrenNodes.put(s, null);
	}
	
	public DecisionNode(String result) {
		nodeName = result;
		childrenNodes = null;
	}
	
	public Collection<DecisionNode> getChildren() {
		if(childrenNodes == null)
			return new HashMap<String, DecisionNode>().values();
		return childrenNodes.values();
	}
	
	public Collection<String> getFeatureValues(){
		if(childrenNodes == null)
			return new HashMap<String, DecisionNode>().keySet();
		return childrenNodes.keySet();
	}
	
	public String[][] searchForExpansion(String[][] choices) {
		for(String s : getFeatureValues()) {
			DecisionNode dN = childrenNodes.get(s);
			String[][] next = new String[choices.length+1][2];
			for(int i = 0; i < choices.length; i++) {
				next[i] = new String[] {choices[i][0], choices[i][1]};
			}
			next[next.length-1] = new String[] {getName(), s};
			if(dN == null)
				return next;
			String[][] poss = dN.searchForExpansion(next);
			if(poss != null)
				return poss;
		}
		return null;
	}
	
	public boolean addFeatureNode(Feature feature) {
		for(String s : getFeatureValues()) {
			DecisionNode dN = childrenNodes.get(s);
			if(dN == null) {
				childrenNodes.put(s, new DecisionNode(feature));
				return true;
			}
			boolean result = dN.addFeatureNode(feature);
			if(result)
				return result;
		}
		return false;
	}
	
	public void addPureLeaf(String[][] path, int index, String valueName, String placeValue) {
		if(index == path.length) {
			childrenNodes.put(valueName, new DecisionNode(placeValue));
			return;
		}
		childrenNodes.get(path[index][1]).addPureLeaf(path, index + 1, valueName, placeValue);
	}
	
	public String getName() {
		return nodeName;
	}
	
	public String queryAccount(HashMap<String, String> input) {
		if(childrenNodes == null)
			return nodeName;
		return childrenNodes.get(input.get(getName())).queryAccount(input);
	}
	
	public int sizeTree(int val) {
		if(childrenNodes == null)
			return val;
		for(DecisionNode dN : childrenNodes.values()) {
			if(dN != null)
				val = dN.sizeTree(val + 1);
		}
		return val;
	}
	
}

class Feature{ 		//Features associated to a DecisionTree instance ("[Outlook]: Sunny, Rainy, Windy")
	
	String name;
	FeatureValue[] featureValues;
	String[] resultValues;
	int[] numberOfValues;
	
	public Feature(String inName, String[] vals, String[] results) {
		name = inName;
		featureValues = new FeatureValue[vals.length];
		for(int i = 0; i < vals.length; i++)
			featureValues[i] = new FeatureValue(vals[i], results);
		numberOfValues = new int[results.length];
		resultValues = results;
	}
	
	public String getName() {
		return name;
	}

	public void addCount(String value, String result) {
		for(int i = 0; i < featureValues.length; i++) {
			if(featureValues[i].getName().equals(value)) {
				featureValues[i].addResult(result);
				numberOfValues[indexOf(resultValues, result)]++;
			}
		}
	}

	public double calculateInformationGain() {
		return calculateAverageEntropy() - calculateFeatureValuesEntropy();
	}
	
	public void resetFeature() {
		for(FeatureValue fV : featureValues) {
			fV.clearResults();
		}
		numberOfValues = new int[resultValues.length];
	}
	
	private double calculateAverageEntropy() {
		double total = 0.0;
		for(int i : numberOfValues) {
			double val = (double)i / (double)totalAmountOfValues();
			if(val == 0)
				continue;
			total += -1 * val * Math.log(val) / Math.log(2);
		}
		return total;
	}
	
	private double calculateFeatureValuesEntropy() {
		double total = 0.0;
		for(FeatureValue fV : featureValues) {
			total += (double)fV.getNumberOfResults() / (double)totalAmountOfValues() * fV.calculateFeatureValueEntropy();
		}
		return total;
	}
	
	public String[] getFeatureValueNames() {
		String[] out = new String[featureValues.length];
		for(int i = 0; i < out.length; i++)
			out[i] = featureValues[i].getName();
		return out;
	}
	
	public boolean isPure(String valueName) {
		for(FeatureValue fV : featureValues) 
			if(fV.getName().equals(valueName))
				return fV.isPure();
		return false;
	}
	
	public String getPureResult(String valueName) {
		for(FeatureValue fV : featureValues) {
			if(fV.getName().equals(valueName))
				return fV.getPureResult();
		}
		return null;
	}
	
	private int totalAmountOfValues() {
		int out = 0;
		for(int i : numberOfValues) {
			out += i;
		}
		return out;
	}
	
	private int indexOf(String[] arr, String key) {
		for(int i = 0; i < arr.length; i++)
			if(arr[i].equals(key))
				return i;
		return -1;
	}
	
}

class FeatureValue{		//Potential values a Feature can have ("Outlook: [Sunny, Rainy, Windy]") which then store result counts
	
	String name;				//Name of a value a Feature can have
	String[] resultNames;		//Name of the overall result for that example
	int[] resultCount;			//Number of results that this FeatureValue has led to of each kind
	
	public FeatureValue(String inName, String[] results) {
		name = inName;
		resultNames = results;
		resultCount = new int[results.length];
	}
	
	public void addResult(String key) {
		for(int i = 0; i < resultNames.length; i++) {
			if(key.equals(resultNames[i]))
				resultCount[i]++;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void clearResults() {
		resultCount = new int[resultNames.length];
	}
	
	public double calculateFeatureValueEntropy() {
		double total = 0;
		double sum = getNumberOfResults();
		for(int i : resultCount) {
			if(i == 0)
				continue;
			double val = (double)i / (double)sum;
			total += -1.0 * val * Math.log(val) / Math.log(2);
		}
		return total;
	}
	
	public int getNumberOfResults() {
		int sum = 0;
		for(int i : resultCount)
			sum += i;
		return sum;
	}
	
	public boolean isPure() {
		int count = 0;
		for(int i : resultCount)
			if(i != 0)
				count++;
		return count == 1;
	}
	
	public String getPureResult() {
		String out = null;
		for(int i = 0; i < resultNames.length; i++)
			if(resultCount[i] != 0)
				if(out == null)
					out = resultNames[i];
				else
					return null;
		return out;
	}

}