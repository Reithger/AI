package other;
import java.util.ArrayList;

/**
 * 
 * 
 * @author Reithger
 *
 */

public class Assgt3Qst3{
	
	public static void main(String[] args) {
		//Network 1
		String[][] variables = new String[][] {{"A", "F", "T"},{"B", "F", "T"},{"C", "F", "T"},{"D", "F", "T"}};
		double[][] prob = new double[][] {{.5, .5, .1, .9}, {.9, .1, .8, .2}, {.7, .3, .4, .6}};
		String[][] facVar = new String[][] {{"A", "B"}, {"B", "C"}, {"C", "D"}};
		VariableElimination varEli = new VariableElimination(prob, facVar, variables);
		varEli.queryVariable("A", new String[] {"B", "C"}, new String[] {"D", "T"});
		varEli.printOut();
		varEli = new VariableElimination(prob, facVar, variables);
		varEli.queryVariable("D", new String[] {"B", "C"}, new String[] {"A", "T"});
		varEli.printOut();
		//Network 2
		variables = new String[][] {{"A", "F", "T"},{"B", "F", "T"},{"C", "F", "T"},{"D", "F", "T"}};
		prob = new double[][] {{.9, .1, .5, .5}, {.2, .8, .3, .7}, {.9, .1, .8, .2, .7, .3, .6, .4}};
		facVar = new String[][] {{"A", "B"}, {"A", "C"}, {"B", "C", "D"}};
		varEli = new VariableElimination(prob, facVar, variables);
		varEli.queryVariable("A", new String[] {"B",  "C"}, new String[] {"D", "F"});
		varEli.printOut();
		varEli = new VariableElimination(prob, facVar, variables);
		varEli.queryVariable("D", new String[] {"B",  "C"}, new String[] {"A", "F"});
		varEli.printOut();
		
		//Network 3
		variables = new String[][] {{"S1", "F", "T"},{"S2", "F", "T"},{"S3", "F", "T"},{"S4", "F", "T"},
									{"O1", "F", "T"},{"O2", "F", "T"},{"O3", "F", "T"},{"O4", "F", "T"}};
		prob = new double[][] {{.8, .2, .8, .2}, {.8, .2, .8, .2}, {.8, .2, .8, .2}, {.9, .1, .9, .1}, {.9, .1, .9, .1}, {.9, .1, .9, .1}, {.9, .1, .9, .1}};
		
		facVar = new String[][] {{"S1", "S2"}, {"S2", "S3"}, {"S3", "S4"}, {"S1", "O1"}, {"S2", "O2"}, {"S3", "O3"}, {"S4", "O4"}};
		varEli = new VariableElimination(prob, facVar, variables);
		varEli.queryVariable("S4", new String[] {"S2", "S3", "S1"}, new String[][] {{"O1", "T"}, {"O2", "F"}, {"O3", "T"}, {"O4", "T"}});
		varEli.printOut();
		varEli = new VariableElimination(prob, facVar, variables);
		varEli.queryVariable("S1", new String[] {"S4", "S3", "S2"}, new String[][] {{"O1", "T"}, {"O2", "F"}, {"O3", "T"}, {"O4", "T"}});
		varEli.printOut();
		
		/*
		varEli.restriction("D", "F");
		varEli.printOut();
		varEli.multiplication(varEli.factors[0], varEli.factors[2]);
		varEli.printOut();
		varEli.summation("B");
		varEli.printOut();
		varEli.multiplication(varEli.factors[0], varEli.factors[1]);
		varEli.printOut();
		varEli.summation("C");
		varEli.printOut();
		varEli.normalize();
		varEli.printOut();
		*/
	}
	
}

/**
 * General note on getting results: After queryVariable is called, printOut() will print
 * the only remaining Factor, which corresponds to that of the requested queryVariable.
 * 
 * @author Reithger
 *
 */

class VariableElimination{
	
//---  Instance Variables   -------------------------------------------------------------------
	
	Factor[] factors;
	String[][] variables;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public VariableElimination(double[][] probabilityInput, String[][] factorVariables, String[][] variableInput) {
		factors = new Factor[probabilityInput.length];
		for(int i = 0; i < factors.length; i++) {
			String[][] indVar = new String[factorVariables[i].length][];
			for(int j = 0; j < factorVariables[i].length; j++) {
				for(int k = 0; k < variableInput.length; k++) {
					if(variableInput[k][0].equals(factorVariables[i][j])) {
						indVar[j] = variableInput[k];
					}
				}
			}
			factors[i] = new Factor(probabilityInput[i], indVar, i);
		}
		variables = variableInput;
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public void queryVariable(String query, String[] removal, String[] ... evidence) {
		for(String[] s : evidence) {
			restriction(s[0], s[1]);
		}
		for(String s : removal) {
			Factor fZ = null;
			for(int i = 0; i < factors.length; i++) {
				if(factors[i].hasVariable(s)) {
					if(fZ == null)
						fZ = factors[i];
					else {
						fZ.product(factors[i]);
						removeFactor(factors[i]);
						i--;
					}
				}
			}
			fZ.summation(s);
		}
		while(factors.length > 1) {
			factors[0].product(factors[1]);
			removeFactor(factors[1]);
		}
		normalize();
	}
	
	public void restriction(String variable, String value) {
		for(int i = 0; i < factors.length; i++) {
			if(factors[i].hasVariable(variable))
				factors[i].restriction(variable, value);
		}
	}
	
	public void multiplication(Factor facOne, Factor facTwo) {
		facOne.product(facTwo);
		removeFactor(facTwo);
	}
	
	public void summation(String variable) {
		for(Factor f : factors) {
			if(f.hasVariable(variable)) {
				f.summation(variable);
			}
		}
	}
	
	public void normalize() {
		for(Factor f : factors)
			f.normalize();
	}
	
	public void printOut() {
		for(Factor f : factors)
			f.printOut();
		System.out.println();
	}
	
//---  Helper Methods   -----------------------------------------------------------------------
	
	private void removeFactor(Factor f) {
		Factor[] newFactor = new Factor[factors.length - 1];
		boolean reduce = false;
		for(int i = 0; i < factors.length; i++) {
			if(factors[i].getIdentity() != f.getIdentity())
				newFactor[reduce ? i - 1 : i] = factors[i];
			else
				reduce = true;
		}
		factors = newFactor;
	}
	
}

/**
 * 
 * @author Reithger
 *
 */

class Factor{
	
//---  Instance Variables   -------------------------------------------------------------------
	
	int identity;
	double[] probabilities;
	String[][] variables;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public Factor(double[] prob, String[][] var, int i) {
		probabilities = prob;
		variables = var;
		identity = i;
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public double getProbability(String[] input) {
		int index = 0;
		int size = probabilities.length;
		for(int i = 0; i < input.length; i++) {
			index += size * (indexOf(variables[i], input[i]) - 1) / (variables[i].length - 1);
			size /= (variables[i].length - 1);
		}
		return probabilities[index];
	}
	
	public void restriction(String variable, String value) {
		int choix = 0;		//Which variable is being reduced
		for(int j = 0; j < variables.length; j++){
			if(variables[j][0].equals(variable)) {
				choix = j;
				break;
			}
		}
		String[][] newVariables = new String[variables.length-1][];	//New variable set with chosen variable removed.
		double[] newProb = new double[probabilities.length / (variables[choix].length-1)];
		for(int i = 0; i < variables.length; i++) {
			if(i != choix)
				newVariables[i > choix ? i - 1 : i] = variables[i];
		}
		recursivelyAssign(0, variable, value, new String[variables.length], newProb);
		probabilities = newProb;
		variables = newVariables;
	}
	
	public void product(Factor facTwo) {
		ArrayList<String> newVar = new ArrayList<String>();
		for(String[] s : getVariables()) {
			if(!newVar.contains(s[0]))
				newVar.add(s[0]);
		}
		for(String[] s : facTwo.getVariables()) {
			if(!newVar.contains(s[0]))
				newVar.add(s[0]);
		}
		String[][] newVariables = new String[newVar.size()][];
		int sizeProb = 1;
		for(int i = 0; i < newVariables.length; i++) {
			String var = newVar.get(i);
			String[] vals = null;
			top:
			for(Factor f : new Factor[] {this, facTwo}) {
				for(int j = 0; j < f.getVariables().length; j++) {
					if(f.getVariables()[j][0].equals(var)) {
						vals = f.getVariables()[j];
						sizeProb *= (f.getVariables()[j].length - 1);
						break top;
					}
				}
			}
			newVariables[i] = vals;
		}
		double[] newProbabilities = new double[sizeProb];
		recursiveProduct(0, newVariables, new String[newVariables.length], newProbabilities, facTwo);
		probabilities = newProbabilities;
		variables = newVariables;
	}
	
	public void summation(String variable) {
		String[][] newVariables = new String[variables.length-1][];
		int newSizeProb = 1;
		boolean pass = false;
		for(int i = 0; i < variables.length; i++) {
			if(!variables[i][0].equals(variable)) {
				newVariables[pass ? i - 1 : i] = variables[i];
				newSizeProb *= (variables[i].length - 1);
			}
			else
				pass = true;
		}
		double[] newProbabilities = new double[newSizeProb];
		recursiveSummation(0, variable, new String[variables.length], newProbabilities);
		probabilities = newProbabilities;
		variables = newVariables;
	}
	
	public void normalize() {
		double totalVal = 0;
		for(double d : probabilities)
			totalVal += d;
		for(int i = 0; i < probabilities.length; i++)
			probabilities[i] /= totalVal;
	}
	
	public void printOut() {
		for(String[] s : variables)
			System.out.print(s[0] + " ");
		System.out.println();
		recurseOut(0, new String[variables.length]);
	}
	
//---  Getter Methods   -----------------------------------------------------------------------
	
	public boolean hasVariable(String var) {
		for(String[] s : variables)
			if(s[0].equals(var))
				return true;
		return false;
	}

	public String[][] getVariables(){
		return variables;
	}
	
	public int getIdentity() {
		return identity;
	}
	
//---  Helper Methods   -----------------------------------------------------------------------
	
	private void recursivelyAssign(int index, String var, String choi, String[] build, double[] newProb) {
		if(index == variables.length) {
			newProb[reducedIndex(build, var)] = getProbability(build);
			return;
		}
		if(variables[index][0].equals(var)) {
			build[index] = choi;
			recursivelyAssign(index + 1, var, choi, build, newProb);
		}
		else {
			for(int i = 1; i < variables[index].length; i++) {
				build[index] = variables[index][i];
				recursivelyAssign(index + 1, var, choi, build, newProb);
			}
		}
	}

	private void recursiveProduct(int index, String[][] newVar, String[] build, double[] newProb, Factor facTwo) {
		if(index == newVar.length) {
			int probInd = 0;
			int probSize = 1;
			for(String[] s : newVar) {
				probSize *= (s.length - 1);
			}
			String[] facOneInput = new String[getVariables().length];
			String[] facTwoInput = new String[facTwo.getVariables().length];
			int indOne = 0;
			int indTwo = 0;
			for(int i = 0; i < build.length; i++) {
				if(hasVariable(newVar[i][0]))
					facOneInput[indOne++] = build[i];
				if(facTwo.hasVariable(newVar[i][0]))
					facTwoInput[indTwo++] = build[i];
				probInd += probSize * (indexOf(newVar[i], build[i]) - 1) / (newVar[i].length - 1);
				probSize /= (newVar[i].length - 1);
			}
			newProb[probInd] = getProbability(facOneInput) * facTwo.getProbability(facTwoInput);
			return;
		}
		for(int i = 1; i < newVar[index].length; i++) {
			build[index] = newVar[index][i];
			recursiveProduct(index + 1, newVar, build, newProb, facTwo);
		}
	}
	
	private void recursiveSummation(int index, String variable, String[] build, double[] newProb) {
		if(index == variables.length) {
			double sumProb = 0;
			int buildIndex = -1;
			for(int i = 0; i < variables.length; i++) {
				if(variable.equals(variables[i][0])) {
					buildIndex = i;
					break;
				}
			}
			for(int i = 1; i < variables[buildIndex].length; i++) {
				build[buildIndex] = variables[buildIndex][i];
				sumProb += getProbability(build);
			}
			newProb[reducedIndex(build, variable)] = sumProb;
			return;
		}
		if(variables[index][0].equals(variable)) {
			recursiveSummation(index + 1, variable, build, newProb);
		}
		else {
			for(int i = 1; i < variables[index].length; i++) {
				build[index] = variables[index][i];
				recursiveSummation(index + 1, variable, build, newProb);
			}
		}
	}
	
	private int reducedIndex(String[] input, String fixVar) {
		int index = 0;
		int size = probabilities.length;
		for(String[] c : variables) {			//Reduce size of storage by removed variable
			if(c[0].equals(fixVar))
				size /= (c.length - 1);
		}
		for(int i = 0; i < input.length; i++) {
			if(variables[i][0].equals(fixVar))
				continue;
			index += size * (indexOf(variables[i], input[i]) - 1) / (variables[i].length - 1);
			size /= (variables[i].length - 1);
		}
		return index;
	}
	
	private void recurseOut(int index, String[] build) {
		if(index == variables.length) {
			for(String s : build)
				System.out.print(s + " ");
			System.out.println(Math.round(10000*getProbability(build))/10000.0);
			return;
		}
		for(int i = 1; i < variables[index].length; i++) {
			build[index] = variables[index][i];
			recurseOut(index + 1, build);
		}
	}

//---  Mechanics   ----------------------------------------------------------------------------
	
	private int indexOf(String[] arr, String key) {
		for(int i = 0; i < arr.length; i++) {
			if(arr[i].equals(key))
				return i;
		}
		return -1;
	}

}