package clobber;

import game.GameMove;
import game.GamePlayer;
import game.GameState;
import game.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AlphaBetaClobberPlayer extends GamePlayer /*implements Runnable*/ {
	private final int MAX_DEPTH = ClobberState.COLS*ClobberState.ROWS;
	private final int MAX_SCORE = Integer.MAX_VALUE;
	protected ScoredClobberMove[] mvStack;
	private static int depthLimit;
	private static String difficulty;
	private static boolean insults;
	private static ArrayList<String> col1;
	private static ArrayList<String> col2;
	private static ArrayList<String> col3;
	private static String message;
	
	/**
	 * Constructor
	 * @param nname String name of player
	 * @param deterministic Deterministic player
	 * @param d Depth limit for alpha-beta search
	 * @throws IOException 
	 */
	public AlphaBetaClobberPlayer(String nname, boolean deterministic, int d) throws IOException {
		super(nname, new ClobberState(), deterministic);
		depthLimit = d;
		
		// Reads insult files into arraylists
		if (insults) {
			col1 = new ArrayList<String>();
			BufferedReader col1Reader = new BufferedReader(new FileReader("col1.txt"));
			String line = null;
			while ((line = col1Reader.readLine()) != null) {
			    col1.add(line);
			}
			col1Reader.close();
			
			col2 = new ArrayList<String>();
			BufferedReader col2Reader = new BufferedReader(new FileReader("col2.txt"));
			line = null;
			while ((line = col2Reader.readLine()) != null) {
			    col2.add(line);
			}
			col2Reader.close();
			
			col3 = new ArrayList<String>();
			BufferedReader col3Reader = new BufferedReader(new FileReader("col3.txt"));
			line = null;
			while ((line = col3Reader.readLine()) != null) {
			    col3.add(line);
			}
			col3Reader.close();
		}
	}
	
	/**
	 * Initializes move stack
	 */
	public void init() {
		mvStack = new ScoredClobberMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredClobberMove(0, 0, 0, 0, 0);
		}
	}
	
	/**
	 * Performs an alpha-beta search
	 * @param brd Current game state
	 * @param currDepth Current depth in search
	 * @param alpha Alpha
	 * @param beta Beta
	 */
	private void alphaBeta(ClobberState brd, int currDepth, double alpha, double beta) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;

		boolean isTerminal = terminalValue(brd, mvStack[currDepth]);

		if (isTerminal) {
			;
		} else if (currDepth == depthLimit) {
			mvStack[currDepth].set(0, 0, 0, 0,evalBoard(brd)); //TODO
		} else {
			ScoredClobberMove tempMv = new ScoredClobberMove(0, 0, 0, 0, 0.0);

			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			ScoredClobberMove bestMove = mvStack[currDepth];
			ScoredClobberMove nextMove = mvStack[currDepth+1];

			bestMove.set(0, 0, 0, 0, bestScore);
			GameState.Who currTurn = brd.getWho();

			// Get list of all possible moves and shuffle list
			ArrayList<ClobberMove> moves = getPossibleMoves(brd, ' ');
			Collections.shuffle(moves);
			
			// Loop through each possible move
			for (int i = 0; i < moves.size(); i++) {
				ClobberMove move = moves.get(i);
				tempMv.set(move.row1, move.col1, move.row2, move.col2, 0);
				brd.makeMove(tempMv);

				alphaBeta(brd, currDepth+1, alpha, beta);

				// Undo move
				brd.board[tempMv.row2][tempMv.col2] = (currTurn == GameState.Who.HOME ? ClobberState.awaySym : ClobberState.homeSym);
				brd.board[tempMv.row1][tempMv.col1] = (currTurn == GameState.Who.HOME ? ClobberState.homeSym : ClobberState.awaySym);
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;

				// Check out the results, relative to what we've seen before
				if (toMaximize && nextMove.score > bestMove.score) {
					if (difficultyDecision()) {
						bestMove.set(move.row1, move.col1, move.row2, move.col2, nextMove.score);
					} else {
						ClobberMove moveMade = moves.get((int)(Math.random() * moves.size()));
						bestMove.set(moveMade.row1, moveMade.col1, moveMade.row2, moveMade.col2, 0);
					}
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					if (difficultyDecision()) {
						bestMove.set(move.row1, move.col1, move.row2, move.col2, nextMove.score);
					} else {
						ClobberMove moveMade = moves.get((int)(Math.random() * moves.size()));
						bestMove.set(moveMade.row1, moveMade.col1, moveMade.row2, moveMade.col2, 0);
					}
				}

				// Update alpha and beta. Perform pruning, if possible.
				if (toMinimize) {
					beta = Math.min(bestMove.score, beta);
					if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
						return;
					}
				} else {
					alpha = Math.max(bestMove.score, alpha);
					if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
						return;
					}
				}
			}
		}
	}
	
	/**
	 * Evaluate the current board state for either home or away
	 * @param brd Current game state
	 * @param who Player to evaluate board for
	 * @return Score of who's board
	 */
	protected int eval(ClobberState brd, char player) {
		int cnt = 0;
		ClobberMove mv = new ClobberMove();
		for (int r = 0; r < ClobberState.ROWS; r++) {
			for (int c = 0; c < ClobberState.COLS; c++) {
				mv.row1 = r;
				mv.col1 = c;
				mv.row2 = r-1; mv.col2 = c;
				if (valid(brd, mv, player)) {
					cnt++;
				}
				mv.row2 = r+1; mv.col2 = c;
				if (valid(brd, mv, player)) {
					cnt++;
				}
				mv.row2 = r; mv.col2 = c-1;
				if (valid(brd, mv, player)) {
					cnt++;
				}
				mv.row2 = r; mv.col2 = c+1;
				if (valid(brd, mv, player)) {
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	/**
	 * Evaluate the current board state
	 * @param brd Current game state
	 * @return Score of current game state
	 */
	protected int evalBoard(ClobberState brd) {
		int score = 0;
		if (side == GameState.Who.HOME) {
			score = eval(brd, ClobberState.homeSym) - eval(brd, ClobberState.awaySym);
		}
		else {
			score = eval(brd, ClobberState.awaySym) - eval(brd, ClobberState.homeSym);
		}
		if (Math.abs(score) > MAX_SCORE) {
			System.err.println("Problem with eval");
			System.exit(0);
		}
		return score;
	}
	
	/**
	 * Determines whether a move is valid for a given player
	 * @param brd Current game state
	 * @param mv Clobber move to decide validity of
	 * @param player Player making move
	 * @return True if valid move
	 */
	public boolean valid(ClobberState brd, ClobberMove mv, char player) {
		boolean OK = false;
		char opp = (player == ClobberState.homeSym ? ClobberState.awaySym : ClobberState.homeSym);
		int rowDiff = mv.row1 - mv.row2;
		int colDiff = mv.col1 - mv.col2;
		if (brd.status == GameState.Status.GAME_ON && mv != null &&
			Util.inrange(mv.row1, 0, ClobberState.ROWS-1) && Util.inrange(mv.row2, 0, ClobberState.ROWS-1) &&
			Util.inrange(mv.col1, 0, ClobberState.COLS-1) && Util.inrange(mv.col2, 0, ClobberState.COLS-1) &&
			((Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 0) || 
			 (Math.abs(rowDiff) == 0 && Math.abs(colDiff) == 1)) && 
			brd.board[mv.row1][mv.col1] == player &&
			brd.board[mv.row2][mv.col2] == opp) {
					OK = true;
		}
		return OK;
	}
	
	/**
	 * Determines if a board represents a completed game. If it is, the
	 * evaluation values for these boards is recorded (i.e., 0 for a draw
	 * +X, for a HOME win and -X for an AWAY win.
	 * @param brd Current game state
	 * @param mv Where to place the score information; only score is relevant
	 * @return True if the brd is a terminal state
	 */
	protected boolean terminalValue(GameState brd, ScoredClobberMove mv) {
		GameState.Status status = brd.getStatus();
		boolean isTerminal = true;
		
		if (status == GameState.Status.HOME_WIN) {
			mv.set(0, 0, 0, 0, MAX_SCORE);
		} else if (status == GameState.Status.AWAY_WIN) {
			mv.set(0, 0, 0, 0, -MAX_SCORE);
		} else if (status == GameState.Status.DRAW) {
			mv.set(0, 0, 0, 0, 0);
		} else {
			isTerminal = false;
		}
		return isTerminal;
	}
	
	/**
	 * Get the best evaluated move by performing an alpha-beta search
	 */
	public GameMove getMove(GameState brd, String lastMove) {
		if (brd.numMoves <= 1) { depthLimit = 5; }
		
		alphaBeta((ClobberState)brd, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);		

		if (!(difficulty.equalsIgnoreCase("easy") || difficulty.equalsIgnoreCase("medium") || difficulty.equalsIgnoreCase("hard"))) {
			depthLimit += 3;
		}
		
		return mvStack[0];
	}
	
	/**
	 * Get all possible moves for the current player
	 * @param brd Current game state
	 * @param who
	 * @return List of all possible moves for current player
	 */
	protected ArrayList<ClobberMove> getPossibleMoves(ClobberState brd, char who) {
		ArrayList<ClobberMove> list = new ArrayList<ClobberMove>();  
		ClobberMove mv = new ClobberMove();
		for (int r = 0; r < ClobberState.ROWS; r++) {
			for (int c = 0; c < ClobberState.COLS; c++) {
				mv.row1 = r;
				mv.col1 = c;
				mv.row2 = r-1; mv.col2 = c;
				if (brd.moveOK(mv)) {
					list.add((ClobberMove)mv.clone());
				}
				mv.row2 = r+1; mv.col2 = c;
				if (brd.moveOK(mv)) {
					list.add((ClobberMove)mv.clone());
				}
				mv.row2 = r; mv.col2 = c-1;
				if (brd.moveOK(mv)) {
					list.add((ClobberMove)mv.clone());
				}
				mv.row2 = r; mv.col2 = c+1;
				if (brd.moveOK(mv)) {
					list.add((ClobberMove)mv.clone());
				}
			}
		}
		return list;
	}
	
	/**
	 * A ClobberMove with a score (how well it evaluates)
	 * @author Adam
	 */
	protected class ScoredClobberMove extends ClobberMove {
		private double score;

		/**
		 * Constructor
		 * @param r1 Row of piece to move
		 * @param c1 Column of piece to move
		 * @param r2 Row of opponent piece to move to
		 * @param c2 Column of opponent piece to move to
		 * @param s Score of move
		 */
		public ScoredClobberMove(int r1, int c1, int r2, int c2, double s) {
			super();
			score = s;
		}

		/**
		 * Setter
		 * @param r1 Row of piece to move
		 * @param c1 Column of piece to move
		 * @param r2 Row of opponent piece to move to
		 * @param c2 Column of opponent piece to move to
		 * @param s Score of move
		 */
		public void set(int r1, int c1, int r2, int c2, double s) {
			row1 = r1;
			col1 = c1;
			row2 = r2;
			col2 = c2;
			score = s;
		}
	}
	
	/**
	 * Determines whether to make a good move based on player difficulty
	 * @return True if good move, False if random move
	 */
	private boolean difficultyDecision() {
		double random = Math.random();
		
		if (difficulty.equalsIgnoreCase("easy")) {
			if (random > 0.25) {
				return false;
			}
		} else if (difficulty.equalsIgnoreCase("medium")) {
			if (random > 0.50) {
				return false;
			}
		} else if (difficulty.equalsIgnoreCase("hard")) {
			if (random > 0.75) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Constructs a random insult
	 * @return String insult
	 */
	private static String constructInsult() {
		int random = (int)(Math.random() * 50);
		String insult = "Thou " + col1.get(random) + " ";
		random = (int)(Math.random() * 50);
		insult += col2.get(random) + " ";
		random = (int)(Math.random() * 50);
		insult += col3.get(random) + "!";
		return insult.toUpperCase();
	}
	
	/**
	 * This is called to obtain the string this player wants to send
	 * to its opponent.	This is called before getMessageFromOpponent
	 * @return The string to be sent to the opponent
	 * @param opponent Name of the opponent being played
	 */
	@Override
	public String messageForOpponent(String opponent) { 
		return message;
	}
	
	public static void main(String [] args) throws IOException {
		int depth = 10;
		
		// Hardcoded difficulty and insults values
		difficulty = "clobber";
		insults = true;
		
		/* Optionally let user manually enter difficulty and whether to insult
		Scanner reader = new Scanner(System.in);
		difficulty = "";
		while (!(difficulty.equalsIgnoreCase("easy") || difficulty.equalsIgnoreCase("medium") ||
				difficulty.equalsIgnoreCase("hard") || difficulty.equalsIgnoreCase("clobber"))) {
			System.out.print("Difficulty? ");
			difficulty = reader.next();
		}
		String insultOpponent = "";
		while (!(insultOpponent.equalsIgnoreCase("yes") || insultOpponent.equalsIgnoreCase("no"))) {
			System.out.print("Insults? ");
			insultOpponent = reader.next();
			
			if (insultOpponent.equalsIgnoreCase("yes")) {
				insults = true;
			} else {
				insults = false;
			}
		}
		reader.close();
		*/
		
		GamePlayer p = new AlphaBetaClobberPlayer("Cloborg " + depth, true, depth);
		if (insults) message = constructInsult();
		p.compete(args);
	}
}
