package overlapclass;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * 
 * @author Reithger
 *
 */

public class Assignment2 {
	
	/**
	 * 
	 * @param args
	 */
	
	/* Benchmark base is 5-5
	 * Compare performance as increase by 5, max out at 25.
	 * Averages of 200 cases. (Count the unsolvable and do not include in average)
	 * 
	 * Benchmark base is 50-50
	 * Compare performance as increase by 50, max out at 250.
	 * Averages of 200 cases. (Count the unsolvable and do not include in average)
	 * 
	 * Benchmark base is 500-500
	 * Compare performance as increase by 500, max out at 2500.
	 * Averages of 200 cases. (Count the unsolvable and do not include in average)
	 * 
	 * Benchmark base is 5000-5000
	 * Compare performance as increase by 5000, max out at 25000.
	 * Average of 200 cases. (Count the unsolvable and do not include in average)
	 */
	
	public static void main(String[] args) {
		
		double[][] result50 = new double[14][5];
		
		double numberTestCases = 100;
		int baselineTest = 5;
		int numberTracking = 5;
		
		for(int k = 1; k <= 1000; k *= 10) {
		
		  for(int i = baselineTest * k; i <= baselineTest * numberTracking * k; i += baselineTest*k) {
			double[] carryBacktrack = new double[2];
			double[] carryLocal = new double[2];
			int unsolveableVariable = 0;
			int unsolveableClause = 0;
			int[] lowHighVal = new int[8];	//Clause/Variable: Local/Back: High/Low
			System.out.println(i);
			CNFSolver solve = new CNFSolver(new CNF(0,0));
			for(int j = 0; j < numberTestCases; j++) {
				int lowHighIndex = 0;
				
				unsolveableVariable += getWorkingCNF(baselineTest * k, i, solve);
				carryBacktrack[0] += solve.getNumberStates();
				lowHighIndex += checkLowHigh(lowHighVal, lowHighIndex, solve.getNumberStates());
				solve.localSearch();
				carryLocal[0] += solve.getNumberStates();
				lowHighIndex += checkLowHigh(lowHighVal, lowHighIndex, solve.getNumberStates());
				
				unsolveableClause += getWorkingCNF(i, baselineTest * k, solve);
				carryBacktrack[1] += solve.getNumberStates();
				lowHighIndex += checkLowHigh(lowHighVal, lowHighIndex, solve.getNumberStates());
				solve.localSearch();
				carryLocal[1] += solve.getNumberStates();
				lowHighIndex += checkLowHigh(lowHighVal, lowHighIndex, solve.getNumberStates());
				
			}
			result50[0][i/(baselineTest*k) - 1] = Math.round(carryBacktrack[0]/(numberTestCases) * 100.0) / 100.0;
			result50[1][i/(baselineTest*k) - 1] = Math.round(carryBacktrack[1]/(numberTestCases) * 100.0) / 100.0;
			result50[2][i/(baselineTest*k) - 1] = Math.round(carryLocal[0]/(numberTestCases) * 100.0) / 100.0;
			result50[3][i/(baselineTest*k) - 1] = Math.round(carryLocal[1]/(numberTestCases) * 100.0) / 100.0;
			result50[4][i/(baselineTest*k) - 1] = Math.round(unsolveableVariable / (numberTestCases + unsolveableVariable) * 100) / 100.0;
			result50[5][i/(baselineTest*k) - 1] = Math.round(unsolveableClause / (numberTestCases + unsolveableClause) * 100) / 100.0;
			for(int j = 0; j < lowHighVal.length; j++) {
				result50[6 + j][i/(baselineTest*k)-1] = lowHighVal[j];
			}
		}
		
		String[] notate = new String[] {"Changing variables Backtracking", "Changing clauses Backtracking", "Changing variables Local", 
										"Changing clauses Local\t", "Freq of Unsolvable Variable", "Freq of Unsolvable Clause",
										"Lowest Backtrack Clause\t", "Highest Backtrack Clause", "Lowest Local Clause\t", "Highest Local Clause\t",
										"Lowest Backtrack Variable", "Highest Backtrack Variable", "Lowest Local Variable\t", "Highest Local Variable\t"};
		
		System.out.println("\t\t\t\t-" + k*baselineTest*1 + "-\t-" + k*baselineTest*2 + "-\t-" + k*baselineTest*3 + "-\t-" + k*baselineTest*4 + "-\t-" + k*baselineTest*5 + "-");
		
		for(int i = 0; i < result50.length; i++) {
			System.out.print(notate[i] + "\t");
			for(double d1 : result50[i]) {
				System.out.print(d1 + "\t");
			}
			System.out.println();
		 }
		
		}
	}
	
	private static int getWorkingCNF(int clause, int variable, CNFSolver solve) {
		int val = -1;
		do{
			val++;
			solve.setCNF(new CNF(GenerateCNF.randomCNF(clause, variable)));
			solve.backtrackSearch();
		} while(solve.getNumberStates() == 0);
		return val;
	}
	
	private static int checkLowHigh(int[] lowHighVal, int lowHigh, int states) {
		if(states != 0) {
			if(states < lowHighVal[lowHigh] || lowHighVal[lowHigh] == 0)
				lowHighVal[lowHigh] = states;
			lowHigh++;
			if(states > lowHighVal[lowHigh] || lowHighVal[lowHigh] == 0)
				lowHighVal[lowHigh] = states;
			lowHigh++;
		}
		return 2;
	}
	
}

class CNFSolver{
	
	private CNF cnf;
	private int solveStates;
	private int[] solution;
	
	
	public CNFSolver(CNF inCNF) {
		cnf = inCNF;
	}
	
	public void backtrackSearch() {
		solveStates = 0;
		solution = new int[cnf.getNumVariable()];
		boolean val = recurseBacktrack(new VerifyCNF(cnf));
		if(!val) {
			solveStates = 0;
			solution = new int[cnf.getNumVariable()];
		}
	}
	
	public boolean recurseBacktrack(VerifyCNF vCNF) {
		solveStates++;
		if(vCNF.isSolved()) {
			solution = vCNF.getAssignedValues();
			return true;
		}
		int in = vCNF.getFrontValue();
		if(in != -1) {
			int val1 = (new VerifyCNF(vCNF, in, 1)).getNumVerified();
			int val2 = (new VerifyCNF(vCNF, in, -1)).getNumVerified();
			int change = val1 > val2 ? 1 : -1;
			boolean test = recurseBacktrack(new VerifyCNF(vCNF, in, change));
			if(!test)
				test = recurseBacktrack(new VerifyCNF(vCNF, in, -1 * change));
			return test;
		}
		return false;
	}
	
	public void localSearch() {
		VerifyCNF vCNF = new VerifyCNF(cnf);
		Random rand = new Random();
		
		solveStates = 0;
		solution = new int[cnf.getNumVariable()];
		ArrayList<Integer> ties = new ArrayList<Integer>();
		int numVar = vCNF.getCNF().getNumVariable();
		int numClause = vCNF.getCNF().getNumClause();
		HashSet<String> visited = new HashSet<String>();
		int bound = numClause * numVar > 10 ? numClause * numVar : 10;
		
		while(!vCNF.isSolved() && solveStates < bound) {
			solveStates++;
			int greatestVal = -1;
			int greatestInd = -1;
			ties.clear();
			for(int i = 0; i < numVar * 2; i++) {
				VerifyCNF test = new VerifyCNF(vCNF, i % numVar, (i / numVar) == 0 ? 1 : -1);
				int val = test.getNumVerified();
				if(val > greatestVal && !visited.contains(test.convertToString())) {
					ties.clear();
					ties.add(i);
					greatestVal = val;
				}
				else if(val == greatestVal && !visited.contains(test.convertToString())) {
					ties.add(i);
				}
			}
			if(ties.size() > 0) {
				greatestInd = ties.get(rand.nextInt(ties.size()));
				vCNF = new VerifyCNF(vCNF, greatestInd%vCNF.getCNF().getNumVariable(), (greatestInd/vCNF.getCNF().getNumVariable())%2 == 0 ? 1 : -1);
				visited.add(vCNF.convertToString());
			}
			else {
				int noBlock = 0;
				while(visited.contains(vCNF.convertToString()) && noBlock++ < bound) {
					greatestInd = rand.nextInt(numVar * 2);
					vCNF = new VerifyCNF(vCNF, greatestInd%vCNF.getCNF().getNumVariable(), (greatestInd/vCNF.getCNF().getNumVariable())%2 == 0 ? 1 : -1);
				}
				visited.add(vCNF.convertToString());
			}
		}
		if(!vCNF.isSolved()) {
			solution = new int[cnf.getNumVariable()];
			solveStates = 0;
		}
		else {
			solution = vCNF.getAssignedValues();
		}
	}
	
	public int getNumberStates() {
		return solveStates;
	}
	
	public int[] getSolution() {
		return solution;
	}
	
	public void printResults() {
		System.out.println("Number of Visited States: " + solveStates);
		System.out.print("Assigned Variables in Solution: ");
		for(int i : solution)
			System.out.print(((i==1) ? "T" : (i==-1) ? "F" : "O") + ", ");
		System.out.println();
	}
	
	public void setCNF(CNF in) {
		cnf = in;
		solveStates = 0;
	}
	
}

class VerifyCNF{
	
	CNF cnf;
	int[] assigned;
	boolean[] verified;
	
	public VerifyCNF(VerifyCNF inCNF, int index, int val) {
		cnf = inCNF.getCNF();
		assigned = inCNF.getAssignedValues();
		int hold = assigned[index];
		assigned[index] = val;
		verified = inCNF.getVerifiedList();
		verified = getVerified(hold != 0);
	}
	
	public VerifyCNF(CNF inCNF) {
		cnf = inCNF;
		assigned = new int[cnf.getNumVariable()];
		verified = new boolean[cnf.getNumClause()];
	}
	
	public CNF getCNF() {
		return cnf;
	}
	
	public boolean isSolved() {
		boolean pass = true;
		for(boolean b : verified)
			if(!b)
				pass = false;
		return pass;
	}
	
	public boolean[] getVerified(boolean changed) {
		boolean[] out = new boolean[cnf.getNumClause()];
		for(int i = 0; i < cnf.getNumClause(); i++) {
			boolean test = false;
			if(!changed && verified[i])
				test = true;
			else {
			  for(int j = 0; j < cnf.getNumVariable(); j++) {
				if(assigned[j] == cnf.getClause(i)[j] && assigned[j] != 0) {
					test = true;
					break;
				}
			  }
			}
			out[i] = test;
		}
		return out;
	}
	
	public int getNumVerified() {
		int c = 0;
		for(boolean b : verified)
			if(b)
				c++;
		return c;
	}
	
	public void assignValues(int ... vals) {
		for(int i = 0; i < assigned.length && i < vals.length; i++) {
			assigned[i] = vals[i];
		}
	}
	
	public void assignValue(int index, int val) {
		assigned[index] = val;
	}
	
	public String convertToString() {
		String out = "";
		for(int i : assigned)
			out += i;
		return out;
	}
	
	public int[] getAssignedValues() {
		int[] out = new int[assigned.length];
		for(int i = 0; i < assigned.length; i++)
			out[i] = assigned[i];
		return out;
	}
	
	public int getFrontValue() {
		int index = -1;
		for(int i = 0; i < assigned.length; i++) {
			if(assigned[i] == 0) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	public boolean[] getVerifiedList() {
		return verified;
	}
}

/**
 * 
 * @author Reithger
 *
 */

class CNF {

	/** */
	int[][] clauses;
	
	/**
	 * 
	 * @param inClause
	 */
	
	public CNF(int[][] inClause) {
		clauses = inClause;
	}

	/**
	 * 
	 * @param numClause
	 * @param numVar
	 */
	
	public CNF(int numClause, int numVar) {
		clauses = new int[numClause][numVar];
	}
	
	/**
	 * 
	 * @param ind
	 * @param clause
	 */
	
	public void assignClause(int ind, int[] clause) {
		clauses[ind] = clause;
	}
	
	/**
	 * 
	 * @param ind
	 * @return
	 */
	
	public int[] getClause(int ind) {
		return clauses[ind];
	}
	
	/**
	 * 
	 * @return
	 */
	
	public int getNumClause() {
		return clauses.length;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public int getNumVariable() {
		if(clauses.length > 0)
			return clauses[0].length;
		else
			return -1;
	}

	/**
	 * 
	 * @return
	 */
	
	public int[][] copyCNF(){
		int[][] out = new int[clauses.length][clauses[0].length];
		for(int i = 0; i < clauses.length; i++) {
			for(int j = 0; j < clauses[0].length; j++) {
				out[i][j] = clauses[i][j];
			}
		}
		return out;
	}
	
	/**
	 * 
	 */
	
	public void printCNF() {
		for(int i = 0; i < clauses.length; i++) {
			for(int j = 0; j < clauses[i].length; j++) {
				System.out.print(clauses[i][j] + (j + 1 < clauses[i].length ? ", " : "\n"));
			}
		}
	}
	
}

/**
 * 
 * @author Reithger
 *
 */

class GenerateCNF{

	/**
	 * 
	 * @param numClause
	 * @param numVar
	 * @return
	 */
	
	public static int[][] randomCNF(int numClause, int numVar){
		int[][] out = new int[numClause][numVar];
		Random rand = new Random();
		for(int i = 0; i < numClause; i++) {
			int[] single = new int[numVar];
			for(int j = 0; j < numVar; j++) {
				single[j] = rand.nextInt(3) - 1;
			}
			boolean blank = true;
			for(int j = 0; j < numVar; j++) {
				if(single[j] != 0)
					blank = false;
			}
			out[i] = single;
			if(blank)
				i--;
		}
		for(int i = 0; i < numVar; i++) {
			boolean blank = true;
			for(int j = 0; j < numClause; j++) {
				if(out[j][i] != 0)
					blank = false;
			}
			if(blank) {
				out[0][i] = 1;
			}
		}
		return out;
	}
	
}