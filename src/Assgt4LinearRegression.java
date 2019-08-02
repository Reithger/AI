import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class Assgt4LinearRegression {

	public static void main(String[] args) {
		MultivariateRegression mR = new MultivariateRegression();
		mR.doExperiment(1000, 10, -10);
	}
	
}

class MultivariateRegression{	//Receives input to process examples, get a hypothesis, and experiment on it
	
	public static final double[] TRUE_ONE = new double[] {-20, 3, .5};
	public static final double[] TRUE_TWO = new double[] {2, 0, -.2, 0, 0.01};
	public static final double[] TRUE_THREE = new double[] {-1, 3, 5, -6, 1};
	
	public void doExperiment(int sizeExample, int upperRange, int lowerRange) {
		double[][] vals = TrueFunctionData.producePolynomialDataSet(sizeExample, TRUE_ONE, upperRange, lowerRange);
		MultivariateExample teachData = new MultivariateExample(vals);
		double[][] hypothesis = teachData.calculateHypothesis();
		print(hypothesis, "Test One");
		System.out.println("Average Distance: " + averageClosenessPolynomial(1000, rotate(hypothesis), TRUE_ONE, upperRange, lowerRange));
		vals = TrueFunctionData.producePolynomialDataSet(sizeExample, TRUE_TWO, upperRange, lowerRange);
		teachData = new MultivariateExample(vals);
		hypothesis = teachData.calculateHypothesis();
		print(hypothesis, "Test Two");
		System.out.println("Average Distance: " + averageClosenessPolynomial(1000, rotate(hypothesis), TRUE_TWO, upperRange, lowerRange));
		vals = TrueFunctionData.produceMultiVariableDataSet(sizeExample, TRUE_THREE, upperRange, lowerRange);
		teachData = new MultivariateExample(vals);
		hypothesis = teachData.calculateHypothesis();
		print(hypothesis, "Test Three");
		System.out.println("Average Distance: " + averageClosenessMultivariable(1000, rotate(hypothesis), TRUE_THREE, upperRange, lowerRange));
	}

	public double averageClosenessPolynomial(int sizeSet, double[] coefficients, double[] realFunction, int upperRange, int lowerRange) {
		double[][] exampleSet = TrueFunctionData.producePolynomialDataSet(sizeSet, realFunction, upperRange, lowerRange);
		
		double dist = 0;

		double small = 100;
		double large = -100;
		double largestDiff = 0;
		
		for(double[] d : exampleSet) {
			
			double calc = coefficients[0];
			for(int i = 1; i < d.length - 1; i++) {
			//	System.out.print(d[i] + " ");
				calc += d[i] * coefficients[i];
			}
			
			//System.out.println(" Out: " + calc + " " + d[d.length - 1]);
			if(calc < small)
				small = calc;
			if(calc > large)
				large = calc;
			double dif = Math.abs(calc - d[d.length - 1]);
			if(dif > largestDiff)
				largestDiff = dif;
			dist += dif;
		}
		
		System.out.println("Smallest: " + small + ", Largest: " + large + ", Largest Distance: " + largestDiff);
		return dist / (double)sizeSet;
	}

	public double averageClosenessMultivariable(int sizeSet, double[] coefficients, double[] realFunction, int upperRange, int lowerRange) {
		double[][] exampleSet = TrueFunctionData.produceMultiVariableDataSet(sizeSet, realFunction, upperRange, lowerRange);
			
		double dist = 0;
		
		double small = 100;
		double large = -100;
		double largestDiff = 0;
		
		for(double[] d : exampleSet) {
			
			double calc = coefficients[0];
			for(int i = 1; i < d.length - 1; i++) {
			//	System.out.print(d[i] + " ");
				calc += d[i] * coefficients[i];
			}
			
			//System.out.println(" Out: " + calc + " " + d[d.length - 1]);
			if(calc < small)
				small = calc;
			if(calc > large)
				large = calc;
			double dif = Math.abs(calc - d[d.length - 1]);
			if(dif > largestDiff)
				largestDiff = dif;
			dist += dif;
		}
		
		System.out.println("Smallest: " + small + ", Largest: " + large + ", Largest Distance: " + largestDiff);
		
		return dist / (double)sizeSet;
	}
	
	private void print(double[][] matrix) {
		System.out.println("Print:");
		for(double[] d : matrix)
			System.out.println(Arrays.toString(d));
	}
	
	private void print(double[][] matrix, String label) {
		System.out.println(label);
		print(matrix);
	}
	
	private double[] rotate(double[][] singleLayer) {
		double[] out = new double[singleLayer.length];
		for(int i = 0; i < out.length; i++) {
			out[i] = singleLayer[i][0];
		}
		return out;
	}
	
}

class TrueFunctionData{
	
	public static double[][] producePolynomialDataSet(int sizeSet, double[] coefficients, int upperRangeX, int lowerRangeX) {
		int numVariable = 0;
		for(int i = 0; i < coefficients.length; i++)
			numVariable = numVariable + (coefficients[i] == 0 ? 0 : 1);
		double[][] out = new double[sizeSet][numVariable + 1];
		//double[][] out = new double[sizeSet][coefficients.length + 1];
		double magnitudeShift = (upperRangeX - lowerRangeX)/50.0;
		Random rand = new Random();
		for(int i = 0; i < sizeSet; i++) {
			double x = (rand.nextDouble() * (upperRangeX - lowerRangeX) - (upperRangeX - lowerRangeX) / 2.0);
			int count = 0;
			for(int j = 0; j < coefficients.length; j++) {
				if(coefficients[j] != 0)
					out[i][count++] = Math.pow(x, j);
				out[i][out[i].length-1] += Math.pow(x, j) * coefficients[j];
			}
			double shift = rand.nextDouble() * 2 - 1;
			out[i][out[i].length-1] += shift * magnitudeShift;
		}
		return out;
	}
	
	public static double[][] produceMultiVariableDataSet(int sizeSet, double[] coefficients, int upperRangeX, int lowerRangeX){
		double[][] out = new double[sizeSet][coefficients.length + 1];
		Random rand = new Random();
		double magnitudeShift = (upperRangeX - lowerRangeX)/50.0;
		for(int i = 0; i < sizeSet; i++) {
			double val = coefficients[0];
			
			for(int j = 0; j < coefficients.length; j++) {
				double x = rand.nextDouble() * (upperRangeX - lowerRangeX) - (upperRangeX - lowerRangeX) / 2.0;
				out[i][j] = x;
				val += coefficients[j] * x;
			}
			double shift = rand.nextDouble() * 2 - 1;
			out[i][out[i].length-1] = val + shift * magnitudeShift;
		}
		
		return out;
	}
	
	private static void print(double[][] matrix) {
		System.out.println("Print:");
		for(double[] d : matrix)
			System.out.println(Arrays.toString(d));
	}
	
	private static void print(double[][] matrix, String label) {
		System.out.println(label);
		print(matrix);
	}
	
}

class MultivariateExample{		//Holds matrix of input values; performs transpose/inversion (permit arbitrary calculation)
	
	double[][] trainingExamples;	//Value of each feature; first index is 1 for constant coefficient
	double[][] associatedValues;		//Provided resulting value from the given features; associated by index
	
	public MultivariateExample(double[][] inputExample) {
		if(inputExample == null)
			return;
		trainingExamples = new double[inputExample.length][];
		associatedValues = new double[inputExample.length][1];
		for(int i = 0; i < inputExample.length; i++) {
			double[] train = new double[inputExample[i].length - 1];
			for(int j = 0; j < train.length; j++)
				train[j] = inputExample[i][j];
			trainingExamples[i] = freshCopy(train);
			associatedValues[i][0] = inputExample[i][inputExample[i].length-1];
		}
	}
	
	public double[][] transposeMatrix(double[][] givenMatrix){
		double[][] out = new double[givenMatrix[0].length][givenMatrix.length];
		for(int i = 0; i < givenMatrix.length; i++) {
			for(int j = 0; j < givenMatrix[i].length; j++) {
				out[j][i] = givenMatrix[i][j];
			}
		}
		return out;
	}
	
	public double[][] invertMatrix(double[][] givenMatrixIn){
		double[][] givenMatrix = freshCopy(givenMatrixIn);
		double[][] identity = new double[givenMatrix.length][givenMatrix[0].length];
		
		for(int i = 0; i < identity.length; i++) {
			identity[i][i] = 1;
		}
		
		for(int i = 0; i < identity.length; i++) {
			if(givenMatrix[i][i] == 0) {
				for(int j = i + 1; j < givenMatrix.length; j++) {
					if(givenMatrix[j][i] != 0) {
						double[] copy = new double[givenMatrix[j].length];
						double[] copyIdentity = new double[givenMatrix[j].length];
						for(int k = 0; k < copy.length; k++) {
							copy[k] = givenMatrix[j][k];
							copyIdentity[k] = identity[j][k];
						}
						givenMatrix[j] = freshCopy(givenMatrix[i]);
						identity[j] = freshCopy(identity[i]);
						givenMatrix[i] = freshCopy(copy);
						identity[i] = freshCopy(copyIdentity);
					}
				}
			}
			double coeff = givenMatrix[i][i];
			for(int j = 0; j < givenMatrix[i].length; j++) {
				givenMatrix[i][j] /= coeff;
				identity[i][j] /= coeff;
			}
			for(int j = 0; j < identity.length; j++) {
				if(j == i)
					continue;
				double removeVal = givenMatrix[j][i];
				for(int k = 0; k < givenMatrix[j].length; k++) {
					givenMatrix[j][k] -= removeVal * givenMatrix[i][k];
					identity[j][k] -= removeVal * identity[i][k];
				}
			}
		}
		
		return identity;
	}
	
	public double[][] multiplyMatrices(double[][] givenMatrixOne, double[][] givenMatrixTwo){
		double[][] out = new double[givenMatrixOne.length][givenMatrixTwo[0].length];
		for(int i = 0; i < out.length; i++) {
			for(int j = 0; j < out[i].length; j++) {
				for(int k = 0; k < givenMatrixOne[0].length; k++) {
					out[i][j] += givenMatrixOne[i][k] * givenMatrixTwo[k][j];
				}
			}
		}
		return out;
	}
	
	public double[][] calculateHypothesis() {
		double[][] pieceOne = multiplyMatrices(transposeMatrix(trainingExamples), trainingExamples);
		double[][] pieceTwo = transposeMatrix(trainingExamples);
		double[][] pieceThree = associatedValues;
		
		//print(trainingExamples, "examp");
		//print(associatedValues, "output");
		/*
		RealMatrix main = new Array2DRowRealMatrix(trainingExamples);
		RealMatrix transpose = main.transpose();
		RealMatrix carry = transpose.multiply(main);
		RealMatrix output = new Array2DRowRealMatrix(associatedValues);
		carry = new LUDecomposition(carry).getSolver().getInverse();
		carry = carry.multiply(transpose);
		carry = carry.multiply(output);
		*/
		double[][] carry = multiplyMatrices(invertMatrix(pieceOne), multiplyMatrices(pieceTwo, pieceThree));
		return carry;
	}
	
	private void print(double[][] matrix) {
		System.out.println("Print:");
		for(double[] d : matrix)
			System.out.println(Arrays.toString(d));
	}
	
	private void print(double[][] matrix, String label) {
		System.out.println(label);
		print(matrix);
	}
	
	private double[] freshCopy(double[] in) {
		double[] out = new double[in.length];
		for(int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}
		return out;
	}
	
	private double[][] freshCopy(double[][] in){
		double[][] out = new double[in.length][in[0].length];
		for(int i = 0; i < in.length; i++) {
			out[i] = freshCopy(in[i]);
		}
		return out;
	}
	
}