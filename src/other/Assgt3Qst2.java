package other;
import java.util.Random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;

/**
 * 
 * @author Reithger
 *
 */

public class Assgt3Qst2 {

	public static void main(String[] args) {
		int numClause = 6;
		int numVar = 6;
		double numTests = 10000;
		boolean result;
		
		double unsatisf = 0;
		
		double[][] salve = new double[numClause][numVar];
		double[][] time = new double[numClause][numVar];
		
		System.out.println("\nClauses up to " + numClause + ", Variables up to " + numVar + ".");
		
		for(double d = 2.0/3.0; d >= 1.0/3.0; d -= .5/3.0) {
			for(int i = 1; i <= numClause; i++) {
				for(int j = 1; j <= numVar; j++) {
					//System.out.println("Clause: " + i + " Variable: " + j);
					unsatisf = 0;
					long start = System.nanoTime();
					for(int k = 0; k < numTests; k++) {
						CNF cnf = new CNF(GenerateCNF.randomCNF(i, j, d));
						result = cnf.resolution();
						if(result)
							unsatisf++;
					}
					long elapsed = System.nanoTime() - start;
					double elapsedTime = (double)((int)(elapsed / Math.pow(10, 6)));	//Average Microseconds to solve
					double solvable =  ((numTests - unsatisf) / numTests) * 100;	//Average solvability of a random problem
					salve[i-1][j-1] = (int)(100*solvable)/100.0;
					time[i-1][j-1] = (int)(100*elapsedTime)/100.0;
				}
			}

			System.out.println("\nAt ratio " + (int)(100*d) + "% for an entry in a clause to be empty.");
			System.out.println(" - Solvability Probability - ");
			for(double[] d2 : salve)
				System.out.println(Arrays.toString(d2));
			System.out.println(" - Time to Complete " + (int)numTests + " Problems (In 10^-3 of a Second) - ");
			for(double[] d2 : time)
				System.out.println(Arrays.toString(d2));
		}

	}

}

/**
 * 
 * @author Reithger
 *
 */

class CNF {

//---  Instance Variables   -------------------------------------------------------------------
		
	ArrayList<Clause> clauses;
	HashSet<String> unique;
	ArrayList<Clause> secondaryClauses;	
	boolean resolutionResult;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public CNF(int[][] inClause) {
		clauses = new ArrayList<Clause>();
		for(int[] a : inClause)
			clauses.add(new Clause(a));
		unique = new HashSet<String>();
		for(Clause clause : clauses)
			unique.add(clause.convertToString());
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public boolean resolution() {
		int size = -1;
		while(size != unique.size()) {
			int top = size;
			size = unique.size();
			secondaryClauses = new ArrayList<Clause>();
			boolean val = recursiveGeneration(0, clauses.size(), top-1);
			clauses.addAll(secondaryClauses);
			if(val) {
				resolutionResult = val;
				return val;
			}
		}
		resolutionResult = false;
		return false;
	}

	public void printCNF() {
		for(Clause c : clauses)
			System.out.println(Arrays.toString(c.getClause()));
	}
	
	public void printResult() {
		System.out.println(resolutionResult ? "Unsatisfiable" : "Satisfiable");
	}
		
//---  Helper Methods   -----------------------------------------------------------------------
	
	private boolean recursiveGeneration(int index, int max, int size) {
		if(index >= max)
			return false;
		for(int i = (index == 0 || index >= size) ? (index) : (size); i < max; i++) {
			Clause nC = new Clause(clauses.get(index), clauses.get(i));
			if(!unique.contains(nC.convertToString())) {
				secondaryClauses.add(nC);
				unique.add(nC.convertToString());
			}
			if(Arrays.equals(nC.getClause(), new int[nC.getClause().length])) {
				return true;
			}
		}
		return(recursiveGeneration(index + 1, max, size));
	}

}

/**
 * 
 * @author Reithger
 *
 */

class Clause{
	
//---  Instance Variables   -------------------------------------------------------------------
	
	int[] clause;
	
	
//---  Constructors   -------------------------------------------------------------------------
		
	public Clause(int[] val) {
		clause = val;
	}
	
	public Clause(Clause a, Clause b) {
		clause = resolve(a, b);
	}
	
//---  Operations  ----------------------------------------------------------------------------
	
	public int[] resolve(Clause a, Clause b) {
		int[] val = new int[a.getClause().length];
		for(int i = 0; i < a.getClause().length; i++) {
			val[i] = a.getIndex(i) == b.getIndex(i) ? a.getIndex(i) : a.getIndex(i) + b.getIndex(i);
		}
		return val;
	}

	public void printClause() {
		System.out.println(Arrays.toString(clause));
	}
	
//---  Getter Methods   -----------------------------------------------------------------------
	
	public int[] getClause() {
		return clause;
	}
	
	public int getIndex(int ind) {
		return clause[ind];
	}
	
	public boolean isEmpty() {
		boolean good = false;
		for(int a : clause)
			if(a != 0)
				good = true;
		return good;
	}

//---  Mechanics   ----------------------------------------------------------------------------
	
	public String convertToString() {
		String out = "";
		for(int a : clause)
			out += a;
		return out;
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
	
	public static int[][] randomCNF(int numClause, int numVar, double weight){
		int[][] out = new int[numClause][numVar];
		Random rand = new Random();
		for(int i = 0; i < numClause; i++) {
			int[] single = new int[numVar];
			for(int j = 0; j < numVar; j++) {
				double v = rand.nextDouble();
				single[j] = (v <= weight ? 0 : v <= (weight + (1 - weight) / 2.0) ? 1 : -1);
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