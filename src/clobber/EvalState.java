package clobber;
import game.GameState;
import game.Util;

import java.awt.Point;
import java.util.ArrayList;

public class EvalState {
	
	public static double possMoves(ClobberState cs){
		int val = 0;
		char board[][] = cs.board;
		//Util.copy(cs.board, board);
		ClobberState temp = (ClobberState) cs.clone();
		ArrayList<Point> homeLivePieces = new ArrayList<Point>();
		ArrayList<Point> awayLivePieces = new ArrayList<Point>();
		ArrayList<PieceGroup> groups = new ArrayList<PieceGroup>();
		int x;
		int y;
		for(x = 0; x<cs.ROWS; x++){
			for(y = 0; y<cs.COLS; y++){
				if(checkLive(cs,board,x,y)){
					handleLivePiece(cs,board,x,y,groups,homeLivePieces,awayLivePieces);
					PieceGroup pg = new PieceGroup(cs,board,x,y);
				}
			}
		}
		double value = 0;
		for(PieceGroup pg : groups){
			if(pg.getSide() == cs.awaySym){
				value -= pg.eval(homeLivePieces);
			}else{
				value += pg.eval(awayLivePieces);
			}
		}
		if(cs.who == GameState.Who.AWAY){
			//value = -value;
		}
		return value;
	}
	
	private static void handleLivePiece(ClobberState cs, char[][] board, int x,int y, 
			ArrayList<PieceGroup> groups, ArrayList<Point> homeLivePieces, ArrayList<Point> awayLivePieces){
		Point p = new Point(x,y);
		boolean containedInList = false;
		for(PieceGroup pg : groups){
			if(pg.checkContains(x, y)){
				containedInList = true;
			}
		}
		if(!containedInList){
			PieceGroup pg = new PieceGroup(cs,board,x,y);
			groups.add(pg);
		}
		if(board[x][y] == cs.homeSym){
			homeLivePieces.add(p);
		}else{
			awayLivePieces.add(p);
		}
	}
	
	private static boolean checkLive(ClobberState cs, char[][] board, int x, int y){
		if(x >= cs.ROWS || x < 0 || y >= cs.COLS || y < 0 || board[x][y] == cs.emptySym){
			return false;
		}
		if(checkOpposite(cs, board, x + 1, y, board[x][y]) || 
				checkOpposite(cs, board, x, y + 1, board[x][y]) ||
				checkOpposite(cs, board, x - 1, y, board[x][y]) || 
				checkOpposite(cs, board, x, y - 1, board[x][y]) ){
			return true;
		}else{
			return false;
		}
	}
	
	private static boolean checkOpposite(ClobberState cs, char[][] board, int x, int y, char lastState){
		if(x >= cs.ROWS || x < 0 || y >= cs.COLS || y < 0 || board[x][y] == cs.emptySym || board[x][y] == lastState){
			return false;
		}
		return true;
	}

}
