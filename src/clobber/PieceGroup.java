package clobber;

import java.util.ArrayList;
import java.awt.Point;

public class PieceGroup {
	
	//static char homeSym;
	//static char awaySym;
	//static char emptySym;
	protected ArrayList<Point> pieces;
	protected char groupType;
	protected ClobberState cs;
	protected char[][] board;
	//ArrayList<PieceGroup> otherGroups;
	
	public PieceGroup(ClobberState cs, char[][] board, int x, int y){
		Point p = new Point(x,y);
		groupType = board[x][y];
		this.cs = cs;
		pieces = new ArrayList<Point>();
		this.board = board;
		recursiveAdd(x,y);
	}
	
	private boolean checkSame(int x, int y){
		if(x >= cs.ROWS || x < 0 || y >= cs.COLS || y < 0 || board[x][y] == cs.emptySym || board[x][y] != groupType){
			return false;
		}
		return true;
	}
	
	private void recursiveAdd(int x,int y){
		if(checkSame(x+1,y) && !checkContains(x+1,y)){
			Point p = new Point(x+1,y);
			pieces.add(p);
			recursiveAdd(x+1,y);
		}
		if(checkSame(x-1,y) && !checkContains(x-1,y)){
			Point p = new Point(x-1,y);
			pieces.add(p);
			recursiveAdd(x-1,y);
		}
		if(checkSame(x,y+1) && !checkContains(x,y+1)){
			Point p = new Point(x,y+1);
			pieces.add(p);
			recursiveAdd(x,y+1);
		}
		if(checkSame(x,y-1) && !checkContains(x,y-1)){
			Point p = new Point(x,y-1);
			pieces.add(p);
			recursiveAdd(x,y-1);
		}
	}
	
	public boolean checkContains(int x,int y){
		return pieces.contains(new Point(x,y));
	}
	
	public char getSide(){
		return groupType;
	}
	
	private boolean withinOne(Point p){
		for(Point myP: pieces){
			if(p.getX() == myP.getX()){
				if(Math.abs(p.getY() - p.getY()) < 1.5f){
					return true;
				}
			}
			if(p.getY() == myP.getY()){
				if(Math.abs(p.getX() - p.getX()) < 1.5f){
					return true;
				}
			}
		}
		return false;
	}
	
	public double eval(ArrayList<Point> livePieces){
		double value = pieces.size();
		double counter = 0;
		for(Point p: livePieces){
			if(withinOne(p)){
				counter++;
			}
		}
		if(counter == 0){
			return 0;
		}
		value = value/counter;
		if(value < 1f || (value == 1 && pieces.size() == 1)){
			value = 0f;
		}
		return value;
	}
	
	public double evalTwo(ArrayList<Point> livePieces){
		double value = pieces.size();
		double counter = 0;
		Point temp = null;
		for(Point p: livePieces){
			if(withinOne(p)){
				counter++;
				temp = p;
			}
		}
		if(counter == 0){
			return 0;
		}else if(counter == 1){
			int counter2 = 0;
			if(checkSame(temp.x,temp.y + 1)){
				counter2++;
			}
			if(checkSame(temp.x,temp.y - 1)){
				counter2++;
			}
			if(checkSame(temp.x + 1,temp.y)){
				counter2++;
			}
			if(checkSame(temp.x - 1,temp.y)){
				counter2++;
			}
			if(counter2 != 0){
				value = value/counter2;
			}else{
				return 0;
			}
		}else{
			value = value/(counter + 1);
		}
		if(value < 1.1f || (value == 1 && pieces.size() == 1)){
			value = 0f;
		}
		return value;
	}
}
	
	
	
	
