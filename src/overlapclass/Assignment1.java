package overlapclass;
import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;

/**
 * This class acts as a testing harness and User Interface for the solving of Sliding Tile Puzzles.
 * 
 * You can either generate a random puzzle at a specified number of scrambled steps, present a specific
 * problem to be solved, or run a statistical analysis on the number of steps required and states visited
 * as an average between a specified number of test puzzles scrambled to a specified level of complexity.
 * 
 * This exists to satisfy Assignment 1 in COMP-3651, Artificial Intelligence.
 * 
 * @author Mac Clevinger
 */

public class Assignment1 {
	
//---  Constants   ----------------------------------------------------------------------------
	
	/** int constant value representing the size of the Puzzle Board (width/height)*/
	private static final int BOARD_SIZE = 3;
	/** */
	private static final int NUMBER_TESTS = 4;

//---  Operations   ---------------------------------------------------------------------------
	
	/**
	 * This method manages the Terminal User Interface for presenting the three options available
	 * for solving Tile Puzzles.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws Exception{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Enter 'a' to auto-generate puzzles, 'b' to supply a specific puzzle, or 'c' to run a statistical test.");

		String choice = br.readLine();
		
		if(choice.equals("a")) {
			SolveTilePuzzle solver = new SolveTilePuzzle();
			String in = "";
			while(in != null) {
				String[][] test = RandomTilePuzzle.makeNewPuzzle(3, 200);
				
				for(String[] s : test) {
					for(String s2 : s) {
						System.out.print(s2 + " ");
					}
					System.out.println();
				}
				
				TilePuzzle tileSolve = new TilePuzzle(test);
				for(int j = 0; j < NUMBER_TESTS; j++) {
					switch(j) {
					case 0: solver.solveBreadthFirst(tileSolve); break;
					case 1: solver.solveStarOutOfPlace(tileSolve); break;
					case 2: solver.solveStarManhattan(tileSolve); break;
					case 3: solver.solveStarEuclidean(tileSolve); break;
					default: break;
					}
					System.out.println("Number of States Visited: " + solver.getNumberStatesVisited() + ", Solution Cost: " + solver.getSolutionCost());
					System.out.print("Moves: ");
					for(String s : solver.getSolution()) {
						System.out.print(s + ", ");
					}
					System.out.println();
				}				
				System.out.println("\nPress and enter any key to generate a new Puzzle.");
				in = br.readLine();
			}
		}
		else if (choice.equals("b")){
			System.out.println("Submit your puzzle below. After processing, you may submit another.");
			String[][] tileBoard = new String[BOARD_SIZE][BOARD_SIZE];
			SolveTilePuzzle solver = new SolveTilePuzzle();
			String in = "";
			while(in != null) {
				for(int i = 0; i < BOARD_SIZE; i++) {
					in = br.readLine();
					tileBoard[i] = in.split(" ");
					if(tileBoard[i].length != BOARD_SIZE)
						throw new Exception();
				}
				TilePuzzle tileSolve = new TilePuzzle(tileBoard);
				for(int j = 0; j < NUMBER_TESTS; j++) {
					switch(j) {
					case 0: solver.solveBreadthFirst(tileSolve); break;
					case 1: solver.solveStarOutOfPlace(tileSolve); break;
					case 2: solver.solveStarManhattan(tileSolve); break;
					case 3: solver.solveStarEuclidean(tileSolve); break;
					default: break;
					}
					System.out.println("Number of States Visited: " + solver.getNumberStatesVisited() + ", Solution Cost: " + solver.getSolutionCost() + " " + solver.getProof());
					System.out.print("Moves: ");					
					for(String s : solver.getSolution()) {
						System.out.print(s + ", ");
					}
					System.out.println();
				}
			}
		}
		else if(choice.equals("c")) {

			System.out.println("Provide the number of tests and the magnitude of these tests on two separate lines.");
			
			String[] methodNames = new String[] {"Breadth First Results: ", "Out of Place Results:  ", "Manhattan Results:     ", "Euclidean Results:    "};
			int numMethods = NUMBER_TESTS;
			
			double[] stateVisit = new double[numMethods];
			double[] cost = new double[numMethods];
			
			int sizeTest = Integer.parseInt(br.readLine());
			int numMoves = Integer.parseInt(br.readLine());

			SolveTilePuzzle solver = new SolveTilePuzzle();
			
			for(int i = 0; i < sizeTest; i++) {
				System.out.println(i);
				String[][] test = RandomTilePuzzle.makeNewPuzzle(3, numMoves);
				TilePuzzle tileSolve = new TilePuzzle(test);
				for(int j = 0; j < NUMBER_TESTS; j++) {
					switch(j) {
						case 0: solver.solveBreadthFirst(tileSolve);break;
						case 1: solver.solveStarOutOfPlace(tileSolve); break;
						case 2: solver.solveStarManhattan(tileSolve); break;
						case 3: solver.solveStarEuclidean(tileSolve); break;
						default: break;
					}
					stateVisit[j] += (double)solver.getNumberStatesVisited();
					cost[j] += (double)solver.getSolutionCost();
			 	}				
			}
			
			File f = new File("C:\\Users\\Reithger\\Documents\\School\\test_" + sizeTest + "_" + numMoves + ".txt");
			f.delete();
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			
			raf.writeBytes("\t\t\t    States Visited \t\tCost");
			System.out.println("\t\t\t    States Visited \t\tCost");
			for(int i = 0; i < numMethods; i++) {
				String info = methodNames[i] + "\t\t" + stateVisit[i]/sizeTest + "\t\t\t" + cost[i]/sizeTest;
				raf.writeBytes(info);
				System.out.println(methodNames[i] + "\t\t" + stateVisit[i]/sizeTest + "\t\t\t" + cost[i]/sizeTest);
			}
			
			raf.close();
		}
	}

}

/**
 * This class represents an individual Tile Puzzle, holding the location of each tile and the
 * empty slot. It also keeps track of how many moves have been taken to reach this particular
 * puzzle configuration.
 * 
 * @author Mac Clevinger
 *
 */

class TilePuzzle{
	
//---  Instance Variables   -------------------------------------------------------------------

	/** String[][] instance variable object representing the Tile Puzzle configuration*/
	private String[][] tileMap;
	/** int instance variable value representing the x location of the empty tile slot*/
	private int emptyLocationX;
	/** int instance variable value representing the y location of the empty tile slot*/
	private int emptyLocationY;
	/** int instance variable vaule representing how many moves have been taken to reach this particular puzzle*/
	private int numMoves;
	
//---  Constructors   -------------------------------------------------------------------------
	
	/**
	 * Constructor for a TilePuzzle object that takes a String[][] to initialize the configuration
	 * of the Tile Puzzle (where the tiles are), assigns the position of the empty tile slot, and
	 * assigns the number of moves needed to reach this point.
	 * 
	 * It also ensures that the provided String[][] is a square map.
	 * 
	 * @param array - String[][] object representing the initial configuration of the Tile Puzzle
	 * @param x - int value representing the x position of the empty tile slot
	 * @param y - int value representing the y position of the empty tile slot
	 * @param cost - int value representing how many moves have been taken to reach this Tile Puzzle
	 * @throws Exception - Throws an Exception if the provided map is not square.
	 */
	
	public TilePuzzle(String[][] array, int x, int y, int cost) throws Exception{
		if(!checkValidSquare(array))
			throw new Exception();
		tileMap = array;
		emptyLocationX = x;
		emptyLocationY = y;
		numMoves = cost;
	}
	
	/**
	 * Constructor for a TilePuzzle object that takes in a String[][] to initialize the configuration
	 * of the Tile Puzzle, and searches through it to find the location of the empty tile slot. The
	 * number of moves to reach this Tile Puzzle is defaulted to 0.
	 * 
	 * @param array - String[][] object representing the initial configuration of the Tile Puzzle
	 * @throws Exception - Throws an Exception if the provided map is not square
	 */
	
	public TilePuzzle(String[][] array) throws Exception{
		if(!checkValidSquare(array))
			throw new Exception();
		tileMap = array;
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array.length; j++) {
				if(array[i][j].equals("0")) {
					emptyLocationX = i;
					emptyLocationY = j;
				}
			}
		}
		numMoves = 0;
	}

//---  Operations   ---------------------------------------------------------------------------
	
	/**
	 * This method creates a new TilePuzzle object that results from moving a particular tile
	 * from one position to another, assumedly that being the empty tile slot and an adjacent tile.
	 * 
	 * @param x - int value representing the location of the x position of the empty tile
	 * @param y - int value representing the location of the y position of the empty tile
	 * @param x2 - int value representing the x position of the tile to swap with the empty tile
	 * @param y2 - int value representing the y position of the tile to swap with the empty tile
	 * @return - Returns a new TilePuzzle object that has been moved in the manner specified.
	 */
	
	public TilePuzzle moveTile(int x, int y, int x2, int y2) {
		try {
			if(x2 < 0 || y2 < 0 || x2 >= tileMap.length || y2 >= tileMap.length)
				return null;
			TilePuzzle newTile = new TilePuzzle(this.copyMap(), x2, y2, this.getNumMoves() + 1);
			newTile.move(x,  y,  x2,  y2);
			return newTile;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method confirms that the provided String[][] is square.
	 * 
	 * @param array - String[][] object representing the Tile Puzzle as an array.
	 * @return - Returns a boolean value reflecting the legality of the provided String[][]
	 */
	
	private boolean checkValidSquare(String[][] array) {
		boolean out = true;
		for(int i = 0; i < array.length; i++)
			if(array[i].length != array.length)
				out = false;
		return out;
	}
	
	/**
	 * This method checks whether two TilePuzzle objects are identical in content or not, comparing
	 * each element within the two.
	 * 
	 * @param puz - TilePuzzle object to compare against the calling object
	 * @return - Returns a boolean; true if the two were the same, false otherwise.
	 */
	
	public boolean checkIfSame(TilePuzzle puz) {
		for(int i = 0; i < puz.copyMap().length; i++) {
			for(int j = 0; j < puz.copyMap().length; j++) {
				if(!this.getTileMap()[i][j].equals(puz.getTileMap()[i][j]))
					return false;
			}
		}
		return true;
	}

	/**
	 * This method performs a heuristic function that checks how many tiles are not in their
	 * correct location, returning that number which are misplaced. (Following the convention
	 * that the order should be the empty tile in the top-left and, from left to right wrapping
	 * around, 1,2,3,... etc.)
	 * 
	 * @return - Returns an int value representing the number of tiles not in their correct location.
	 */
	
	public int heuristicOutOfPlace() {
		int inPlace = 0;
		for(int i = 0; i < tileMap.length; i++) {
			for(int j = 0; j < tileMap.length; j++) {
				if(!(((j*tileMap.length+i)+"").equals(tileMap[j][i])))
					inPlace++;
			}
		}
		return inPlace;
	}

	/**
	 * This method performs a heuristic function that calculates the Manhattan Distance (sum of the
	 * discrete distances in x and y coordinates from the correct location) of each tile, that total
	 * sum being returned as the heuristic's value.
	 * 
	 * @return - Returns an int value representing the total Manhattan distance of all tiles from their correct location.
	 */
	
	public int heuristicManhattan() {
		int totalDistance = 0;
		for(int i = 0; i < tileMap.length; i++) {
			for(int j = 0; j < tileMap.length; j++) {
				int val = Integer.parseInt(tileMap[i][j]);
				if(val != 0) {
					int x = val % tileMap.length;
					int y = val / tileMap.length;
					totalDistance += Math.abs(x - j) + Math.abs(y - i);
				}
			}
		}
		return totalDistance;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public double heuristicEuclidean() {
		double totalDistance = 0;
		for(int i = 0; i < tileMap.length; i++) {
			for(int j = 0; j < tileMap.length; j++) {
				int val = Integer.parseInt(tileMap[i][j]);
				if(val != 0) {
					int x = val % tileMap.length;
					int y = val / tileMap.length;
					totalDistance += Math.sqrt(Math.pow(x - j, 2) + Math.pow(y - i, 2));
				}
			}
		}
		return totalDistance;
	}
	
//---  Getter Methods   -----------------------------------------------------------------------
	
	/**
	 * Getter method that returns whether the state of the Tile Puzzle is solved or not.
	 * 
	 * @return - Returns a boolean value representing whether the Tile Puzzle is solved or not.
	 */
	
	public boolean isSolved() {
		for(int i = 0; i < tileMap.length; i++) {
			for(int j = 0; j < tileMap.length; j++) {
				if(!(((i*tileMap.length+j)+"").equals(tileMap[i][j]) || (i == 0 && j == 0 && tileMap[i][j].equals("0"))))
					return false;
			}
		}
		return true;
	}

	/**
	 * Getter method that returns the x location of the empty tile piece.
	 * 
	 * @return - Returns an int value representing the x location of the empty tile piece.
	 */
	
	public int getEmptyX() {
		return emptyLocationX;
	}

	/**
	 * Getter method that returns the y location of the empty tile piece.
	 * 
	 * @return - Returns an int vaue representing the y location of the empty tile piece.
	 */
	
	public int getEmptyY() {
		return emptyLocationY;
	}

	/**
	 * Getter method that returns the total number of moves that had been needed to reach this Tile Puzzle.
	 * 
	 * This is within the context of solving the puzzle, so the number of moves are derived by another
	 * class' algorithm.
	 * 
	 * @return - Returns an int value representing the total number of moves needed to reach this Tile Puzzle.
	 */
	
	public int getNumMoves() {
		return numMoves;
	}

	/**
	 * Getter method that returns the stored String[][] representing the configuration of the Tile Puzzle.
	 * 
	 * @return - Returns a String[][] representing the configuration of the Tile Puzzle.
	 */
	
	public String[][] getTileMap(){
		return tileMap;
	}
	
	/**
	 * Getter method that returns a copied, reference-free String[][] representing the configuration of the Tile Puzzle.
	 * 
	 * @return - Returns a copied, reference-free String[][] representing the configuration of the Tile Puzzle.
	 */
	
	public String[][] copyMap(){
		String[][] copy = new String[tileMap.length][tileMap.length];
		for(int i = 0; i < tileMap.length; i++) {
			for(int j = 0; j < tileMap.length; j++) {
				copy[i][j] = new String(tileMap[i][j]);
			}
		}
		return copy;
	}

	/**
	 * Getter method that converts the String[][] representing the configuration of tiles in the Tile Puzzle as a String
	 * in the pattern of (0,0), (0,1), (0,2), (1,0), (1,1), etc.
	 * 
	 * @return - Returns a String object representation of the String[][] stored by this object to representing the Tile Puzzle configuration.
	 */
	
	public String convertToString() {
		String out = "";
		for(String[] s : tileMap) {
			for(String s2 : s) {
				out += s2;
			}
		}
		return out;
	}

//---  Setter Methods   -----------------------------------------------------------------------
	
	/**
	 * Setter method that assigns the number of moves
	 * 
	 * @param num
	 */
	
	public void setNumMoves(int num) {
		numMoves = num;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 */
	
	private void move(int x, int y, int x2, int y2) {
		String hold = new String(tileMap[x][y]);
		tileMap[x][y] = new String(tileMap[x2][y2]);
		tileMap[x2][y2] = hold;
	}

}

/**
 * 
 * @author Mac Clevinger
 *
 */

class SolveTilePuzzle{

//---  Instance Variables   -------------------------------------------------------------------
	
	/** */
	private ArrayList<String> solution;
	/** */
	private int solutionCost;
	/** */
	private int statesVisited;
	/** */
	private String proof;
	
//---  Operations   ---------------------------------------------------------------------------
	
	/**
	 * 
	 * @param puz
	 */
	
	public void solveBreadthFirst(TilePuzzle puz) {
		genericSolve(puz, 0);
	}

	/**
	 * 
	 * @param puz
	 */
	
	public void solveStarOutOfPlace(TilePuzzle puz) {
		genericSolve(puz, 1);
	}
	
	/**
	 * 
	 * @param puz
	 */
	
	public void solveStarManhattan(TilePuzzle puz) {
		genericSolve(puz, 2);
	}

	/**
	 * 
	 * @param puz
	 */
	
	public void solveStarEuclidean(TilePuzzle puz) {
		genericSolve(puz, 3);
	}
	
	/**
	 * 
	 * @param puz
	 * @param characterization
	 */
	
	public void genericSolve(TilePuzzle puz, int characterization) {
		reinitialize();
		PriorityQueue<HeuristicTilePuzzle> queue = new PriorityQueue<HeuristicTilePuzzle>();
		HashSet<String> visited = new HashSet<String>();
		
		int timeStamp = 0;
		
		HeuristicTilePuzzle hPuz = new HeuristicTilePuzzle(puz, 0, new ArrayList<String>(), null, timeStamp++);
		
		queue.add(hPuz);
		int visit = 0;
		
		while(!queue.isEmpty()) {
			HeuristicTilePuzzle topHeur = queue.poll();
			TilePuzzle top = topHeur.getTilePuzzle();
			
			if(visited.contains(top.convertToString())) {
				continue;
			}
			
			visited.add(top.convertToString());
			
			visit++;
			if(top.isSolved()) {
				solution = topHeur.getMoves();
				solutionCost = top.getNumMoves();
				statesVisited = visit;
				proof = top.convertToString();
				break;
			}
			
			ArrayList<HeuristicTilePuzzle> possible = new ArrayList<HeuristicTilePuzzle>();
			
			for(int i = 0; i < 4; i++) {
				TilePuzzle newMap = null;
				String move = "";
				switch(i) {
					case 0: 
						newMap = top.moveTile(top.getEmptyX(), top.getEmptyY(), top.getEmptyX()-1, top.getEmptyY());
						move = "up";
						break;
					case 1: 
						newMap = top.moveTile(top.getEmptyX(), top.getEmptyY(), top.getEmptyX()+1, top.getEmptyY()); 
						move = "down";
						break;
					case 2: 
						newMap = top.moveTile(top.getEmptyX(), top.getEmptyY(), top.getEmptyX(), top.getEmptyY()-1); 
						move = "left";
						break;
					case 3: 
						newMap = top.moveTile(top.getEmptyX(), top.getEmptyY(), top.getEmptyX(), top.getEmptyY()+1); 
						move = "right";
						break;
					default:
						break;
				}
				if(newMap != null && !newMap.checkIfSame(top) && !visited.contains(newMap.convertToString())) {
					double toAdd = 0;
					switch(characterization) {
						case 0: toAdd = 0; break;
						case 1: toAdd = top.getNumMoves() + newMap.heuristicOutOfPlace(); break;
						case 2: toAdd = top.getNumMoves() + newMap.heuristicManhattan(); break;
						case 3: toAdd = top.getNumMoves() + newMap.heuristicEuclidean(); break;
						default: break;
					}
					queue.add(new HeuristicTilePuzzle(newMap, toAdd, topHeur.getMoves(), move, timeStamp++));
				}
			}
		}
	}

	/**
	 * 
	 */
	
	private void reinitialize() {
		solution = new ArrayList<String>();
		solutionCost = 0;
		statesVisited = 0;
	}

//---  Getter Methods   -----------------------------------------------------------------------
	
	/**
	 * 
	 * @return
	 */
	
	public ArrayList<String> getSolution() {
		return solution;
	}

	/**
	 * 
	 * @return
	 */
	
	public int getSolutionCost() {
		return solutionCost;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public int getNumberStatesVisited() {
		return statesVisited;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	
	public String getProof() {
		return proof;
	}

}

/**
 * 
 * @author Mac Clevinger
 *
 */

class HeuristicTilePuzzle implements Comparable<HeuristicTilePuzzle>, Comparator<HeuristicTilePuzzle>{
	
//---  Instance Variables   -------------------------------------------------------------------
	
	/** */	
	private TilePuzzle puz;
	/** */
	private double heuristicCost;
	/** */
	private ArrayList<String> moves;
	/** */
	private int timeStamp;
	
//---  Constructors   -------------------------------------------------------------------------
	
	/**
	 * 
	 * 
	 * @param inPuz
	 * @param startCost
	 * @param prevMoves
	 * @param move
	 * @param time
	 */
	
	public HeuristicTilePuzzle(TilePuzzle inPuz, double startCost, ArrayList<String> prevMoves, String move, int time) {
		puz = inPuz;
		heuristicCost = startCost;
		moves = new ArrayList<String>();
		if(prevMoves != null)
			moves.addAll(prevMoves);
		if(move != null)
			moves.add(move);
		timeStamp = time;
	}

//---  Getter Methods   -----------------------------------------------------------------------
	
	/**
	 * 
	 * @return
	 */
	
	public TilePuzzle getTilePuzzle() {
		return puz;
	}

	/**
	 * 
	 * @return
	 */
	
	public double getHeuristicCost() {
		return heuristicCost;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public ArrayList<String> getMoves(){
		return moves;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public int getTimeStamp() {
		return timeStamp;
	}
	
//---  Mechanics   ----------------------------------------------------------------------------
	
	@Override
	public int compareTo(HeuristicTilePuzzle t1) {
		double a = this.getHeuristicCost();
		double b = t1.getHeuristicCost();
		
		if(a > b)
			return 1;
		else if(a < b)
			return -1;
		else if(a == b) {
			int c = this.getTimeStamp();
			int d = t1.getTimeStamp();
			if(c > d)
				return 1;
			else if(c < d)
				return -1;
		}
		return 0;
	}
	
	@Override
	public int compare(HeuristicTilePuzzle t1, HeuristicTilePuzzle t2) {
		double a = t1.getHeuristicCost();
		double b = t2.getHeuristicCost();
		
		if(a > b)
			return 1;
		else if(a < b)
			return -1;
		else if(a == b) {
			int c = t1.getTimeStamp();
			int d = t2.getTimeStamp();
			if(c > d)
				return 1;
			else if(c < d)
				return -1;
		}
		return 0;		
	}

}

/**
 * 
 * @author Mac Clevinger
 *
 */

class RandomTilePuzzle {
	
//---  Operations   ---------------------------------------------------------------------------
	
	/**
	 * 
	 * @param size
	 * @param scrambleFactor
	 * @return
	 */
	
	public static String[][] makeNewPuzzle(int size, int scrambleFactor){
		String[][] puzzleArray = new String[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				puzzleArray[i][j] = "" + (size*i + j);
			}
		}
		Random rand = new Random();
		int x = 0, y = 0;
		for(int i = 0; i < scrambleFactor; i++) {
			int ran = rand.nextInt(4);
			switch(ran) {
			case 0:
				if(x - 1 >= 0) {
					puzzleArray[x][y] = new String(puzzleArray[x-1][y]);
					puzzleArray[x-1][y] = "0";
					x--;
					break;
				}
				else {
					i--;
					break;
				}
			case 1:
				if(x + 1 < size) {
					puzzleArray[x][y] = new String(puzzleArray[x+1][y]);
					puzzleArray[x+1][y] = "0";
					x++;
					break;
				}
				else {
					i--;
					break;
				}
			case 2:
				if(y - 1 >= 0) {
					puzzleArray[x][y] = new String(puzzleArray[x][y-1]);
					puzzleArray[x][y-1] = "0";
					y--;
					break;
				}
				else {
					i--;
					break;
				}
			case 3:
				if(y + 1 < size) {
					puzzleArray[x][y] = new String(puzzleArray[x][y+1]);
					puzzleArray[x][y+1] = "0";
					y++;
					break;
				}
				else {
					i--;
					break;
				}
			default:
				i--;
				break;
			}
		}
		return puzzleArray;
	}
	
}