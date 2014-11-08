package clobber;

import java.util.ArrayList;
import java.util.Collections;
import game.*;

public class AlphaBetaClobberPlayer extends GamePlayer {
	private final int MAX_DEPTH = 50;
	private final int MAX_SCORE = Integer.MAX_VALUE;
	private int depthLimit;
	protected ScoredClobberMove[] mvStack;
	
	/**
	 * Constructor
	 * @param nname String name of player
	 * @param deterministic
	 */
	public AlphaBetaClobberPlayer(String nname, boolean deterministic) {
		super(nname, new ClobberState(), deterministic);
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
			mvStack[currDepth].set(0, 0, 0, 0, evalBoard(brd));
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

				// TODO: Undo move
				//brd.numInCol[c]--;
				//int row = brd.numInCol[c]; 
				//brd.board[row][c] = ClobberState.emptySym;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;

				// Check out the results, relative to what we've seen before
				if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.set(move.row1, move.col1, move.row2, move.col2, nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.set(move.row1, move.col1, move.row2, move.col2, nextMove.score);
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
	protected int eval(ClobberState brd, char who) {
		// TODO: Develop evaluation function
		int cnt = 0;
		return cnt;
	}
	
	/**
	 * Evaluate the current board state
	 * @param brd Current game state
	 * @return Score of current game state
	 */
	protected int evalBoard(ClobberState brd) { 
		int score = eval(brd, ClobberState.homeSym) - eval(brd, ClobberState.awaySym);
		if (Math.abs(score) > MAX_SCORE) {
			System.err.println("Problem with eval");
			System.exit(0);
		}
		return score;
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
		alphaBeta((ClobberState)brd, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);
		return mvStack[0];
	}
	
	/**
	 * Get all possible moves for the current player
	 * @param brd Current game state
	 * @param who TODO: Allow for getting moves of either home or away player
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
	
	public static void main(String [] args) {
		int depth = 8;
		//GamePlayer p = new AlphaBetaClobberPlayer("C4 A-B F1 " + depth, depth);
		//p.compete(args);
	}
}
