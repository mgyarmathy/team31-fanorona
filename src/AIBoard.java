import javax.swing.*;
import java.util.*;
import java.awt.*;

public class AIBoard {
	
	public GamePanel.Piece[][] Boardstate = new GamePanel.Piece[GamePanel.ROWS][GamePanel.COLS];
	public ArrayList<GamePanel.Type> moves = new ArrayList<GamePanel.Type>();
	public int rank = 0;
	public GamePanel.Direction prevDir = GamePanel.Direction.DUMMY;
	public ArrayList<Point> chained_spots = new ArrayList<Point>();
	public ArrayList<String> messages = new ArrayList<String>();
	
	public AIBoard(GamePanel.Piece[][] board){
		Boardstate = copyBoard(board);
		Count();
	}
	
	public void Count(){
		int playerC = 0;
		int opponentC = 0;
		for(int i = 0; i < Boardstate.length; i++){
			for (int j = 0; j < Boardstate[0].length; j++){
				if(Boardstate[i][j] == GamePanel.Piece.PLAYER){
					playerC++;
				}
				if(Boardstate[i][j] == GamePanel.Piece.OPPONENT){
					opponentC++;
				}
			}
		}
		rank = playerC - opponentC;
	}
	
	public GamePanel.Piece[][] getBoard(){
		return Boardstate;
	}
	
	public Point makeMove(GamePanel.Direction dir, Point start, GamePanel.Piece color, boolean after){
		Point retP = new Point(0,0);
		int curX = 0;
		int curY = 0;
		int xInc = 0;
		int yInc = 0;
		switch(dir){
		case UPLEFT:	Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y-1][start.x-1] = color; 
						retP = new Point(start.x-1,start.y-1);
						prevDir = GamePanel.Direction.UPLEFT;
						if(after){
							//Boardstate[start.y-2][start.x-2] = GamePanel.Piece.EMPTY;
							curX = start.x-2;
							curY = start.y-2;
							xInc = -1;
							yInc = -1;
						} else {
							//Boardstate[start.y+1][start.x+1] = GamePanel.Piece.EMPTY;
							curX = start.x+1;
							curY = start.y+1;
							xInc = 1;
							yInc = 1;
						}
						break;
		case UP:		Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y-1][start.x] = color; 
						retP = new Point(start.x,start.y-1);
						prevDir = GamePanel.Direction.UP;
						if(after){
							//Boardstate[start.y-2][start.x] = GamePanel.Piece.EMPTY;
							curX = start.x;
							curY = start.y-2;
							yInc = -1;
						} else {
							//Boardstate[start.y+1][start.x] = GamePanel.Piece.EMPTY;
							curX = start.x;
							curY = start.y+1;
							yInc = 1;
						}
						break;
		case UPRIGHT:	Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y-1][start.x+1] = color; 
						retP = new Point(start.x+1,start.y-1);
						prevDir = GamePanel.Direction.UPRIGHT;
						if(after){
							//Boardstate[start.y-2][start.x+2] = GamePanel.Piece.EMPTY;
							curX = start.x+2;
							curY = start.y-2;
							xInc = +1;
							yInc = -1;
						} else {
							//Boardstate[start.y+1][start.x-1] = GamePanel.Piece.EMPTY;
							curX = start.x-1;
							curY = start.y+1;
							xInc = -1;
							yInc = 1;
						}
						break;
		case LEFT:		Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y][start.x-1] = color;
						retP = new Point(start.x-1,start.y);
						prevDir = GamePanel.Direction.LEFT;
						if(after) {
							//Boardstate[start.y][start.x-2] = GamePanel.Piece.EMPTY;
							curX = start.x-2;
							curY = start.y;
							xInc = -1;
						} else {
							//Boardstate[start.y][start.x+1] = GamePanel.Piece.EMPTY;
							curX = start.x+1;
							curY = start.y;
							xInc = 1;
						}
						break;
		case RIGHT:		Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y][start.x+1] = color; 
						retP = new Point(start.x+1,start.y);
						prevDir = GamePanel.Direction.RIGHT;
						if(after) {
							//Boardstate[start.y][start.x+2] = GamePanel.Piece.EMPTY;
							curX = start.x+2;
							curY = start.y;
							xInc = +1;
						} else {
							//Boardstate[start.y][start.x-1] = GamePanel.Piece.EMPTY;
							curX = start.x-1;
							curY = start.y;
							xInc = -1;
						}
						break;
		case DOWNLEFT:	Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y+1][start.x-1] = color; 
						retP = new Point(start.x-1,start.y+1);
						prevDir = GamePanel.Direction.DOWNLEFT;
						if(after) {
							//Boardstate[start.y+2][start.x-2] = GamePanel.Piece.EMPTY;
							curX = start.x-2;
							curY = start.y+2;
							xInc = -1;
							yInc = 1;
						} else {
							//Boardstate[start.y-1][start.x+1] = GamePanel.Piece.EMPTY;
							curX = start.x+1;
							curY = start.y-1;
							xInc = 1;
							yInc = -1;
						}
						break;
		case DOWN:		Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y+1][start.x] = color; 
						retP = new Point(start.x,start.y+1);
						prevDir = GamePanel.Direction.DOWN;
						if(after) {
							//Boardstate[start.y+2][start.x] = GamePanel.Piece.EMPTY;
							curX = start.x;
							curY = start.y+2;
							yInc = 1;
						} else {
							//Boardstate[start.y-1][start.x] = GamePanel.Piece.EMPTY;
							curX = start.x;
							curY = start.y-1;
							yInc = -1;
						}
						break;
		case DOWNRIGHT:	Boardstate[start.y][start.x] = GamePanel.Piece.EMPTY;
						Boardstate[start.y+1][start.x+1] = color; 
						retP = new Point(start.x+1,start.y+1);
						prevDir = GamePanel.Direction.DOWNRIGHT;
						if(after){
							//Boardstate[start.y+2][start.x+2] = GamePanel.Piece.EMPTY;
							curX = start.x+2;
							curY = start.y+2;
							xInc = 1;
							yInc = 1;
						} else {
							//Boardstate[start.y-1][start.x-1] = GamePanel.Piece.EMPTY;
							curX = start.x-1;
							curY = start.y-1;
							xInc = -1;
							yInc = -1;
						}
						break;
		default:		break;
		}
		
		GamePanel.Piece myColor = GamePanel.Piece.PLAYER;
		if (color == GamePanel.Piece.PLAYER){
			myColor = GamePanel.Piece.OPPONENT;
		}
		if(chained_spots.isEmpty()){
			chained_spots.add(new Point(start.x,start.y));
		}
		chained_spots.add(retP);
		//curX += xInc;
		//curY += yInc;
		GamePanel.Type move = GamePanel.Type.PAIKA;
		if(curX >= 0 && curX < GamePanel.COLS && curY >= 0 && curY < GamePanel.ROWS){
			if(Boardstate[curY][curX] == myColor){
				if(after){
					move = GamePanel.Type.ADVANCE;
				} else {
					move = GamePanel.Type.WITHDRAW;
				}
			}
		}
		
		while(curX >= 0 && curX < GamePanel.COLS && curY >= 0 && curY < GamePanel.ROWS){
			if(Boardstate[curY][curX] == myColor){
				Boardstate[curY][curX] = GamePanel.Piece.EMPTY;
				curX += xInc;
				curY += yInc;
			} else {
				break;
			}
		}
		Count();
		char c=(char) (start.y+65);
		char c2=(char) (retP.y+65);
		messages.add((Character.toString(c)+Integer.toString(start.x+1)+" moved to "+Character.toString(c2)+Integer.toString(retP.x+1)));
		
		
		moves.add(move);
		return retP;
	}
	
	public GamePanel.Piece[][] copyBoard(GamePanel.Piece[][] base){
		GamePanel.Piece[][] board = new GamePanel.Piece[base.length][base[0].length];
		for(int i = 0; i < base.length; i++){
			for (int j = 0; j < base[0].length; j++){
				if(base[i][j] == GamePanel.Piece.EMPTY){
					board[i][j] = GamePanel.Piece.EMPTY;
				} else if(base[i][j] == GamePanel.Piece.PLAYER){
					board[i][j] = GamePanel.Piece.PLAYER;
				} else if(base[i][j] == GamePanel.Piece.OPPONENT){
					board[i][j] = GamePanel.Piece.OPPONENT;
				} else {
					board[i][j] = GamePanel.Piece.SACRIFICE;
				}
			}
		}
		return board;
	}
}