import javax.swing.*;
import java.awt.*;
import java.util.*;

public class AI{
	public Tree<AIBoard> tree;
	public GamePanel.Piece color;
	
	public AI(GamePanel.Piece[][] board, GamePanel.Piece col){
		AIBoard rootB = new AIBoard(board);
		tree = new Tree<AIBoard>(rootB);
		color = col;
	}
	
	public void create(){
		AIBoard base = tree.getRoot().getData();
		GamePanel.Piece[][] baseBoard = copyBoard(base.getBoard());
		ArrayList<AIBoard> moves = new ArrayList<AIBoard>();
		ArrayList<Point> places = new ArrayList<Point>();
		for(int i = 0; i < baseBoard.length; i++){
			for (int j = 0; j < baseBoard[0].length; j++){
				if(baseBoard[i][j] == color){
					findMoves(j,i,color,base,moves,places);
					
				}
			}
		}
		for(int k = 0; k < moves.size(); k++){
			tree.getRoot().add(moves.get(k));
			findMoves(places.get(k).x,places.get(k).y,color,moves.get(k),moves,places);
		}
	}
	
	public GamePanel.Piece[][] getMove(){
		create();
		int index = -1;
		int value = 1000;
		if(color == GamePanel.Piece.PLAYER)
			value = -1000;
		for(int i = 0; i < tree.getRoot().getChildren().size(); i++){
			AIBoard base = tree.getRoot().getChildren().get(i).getData();
			if(color == GamePanel.Piece.OPPONENT){
				if(base.rank < value){
					value = base.rank;
					index = i;
				}
			} else {
				if(base.rank > value){
					value = base.rank;
					index = i;
				}
			}
		}
		if(index == -1){
			return makeEmpty();
		} else {
			for(int x = 0; x < tree.getRoot().getChildren().get(index).getData().messages.size(); x++){
				GamePanel.info.write(tree.getRoot().getChildren().get(index).getData().messages.get(x));
			}
			return tree.getRoot().getChildren().get(index).getData().getBoard();
		}
	}
	
	public GamePanel.Piece[][] makeEmpty(){
		Point pick = new Point(0,0);
		Point go = new Point(0,0);
		boolean first = true;
		Random ran = new Random();
		int test = ran.nextInt();
		
		GamePanel.Piece[][] base = tree.getRoot().getData().getBoard();
		for (int i = 0; i < base.length; i++){
			for (int j = 0; j < base[0].length; j++){
				if(base[i][j] == color){
					Point p = findSpace(j,i,base);
					if(p.x != -1){
						if (first){
							first = false;
							pick = new Point(j,i);
							go = p;
						} else {
							int test2 = ran.nextInt();
							if(test2 > test){
								pick = new Point(j,i);
								go = p;
							}
						}
					}
				}
			}
		}
		
		base[pick.y][pick.x] = GamePanel.Piece.EMPTY;
		base[go.y][go.x] = color;
		return base;
	}
	
	public Point findSpace(int x, int y, GamePanel.Piece[][] board){
		boolean UL = false;
		boolean U = false;
		boolean UR = false;
		boolean L = false;
		boolean R = false;
		boolean DL = false;
		boolean D = false;
		boolean DR = false;
		
		if ((y+x)%2 == GamePanel.Diag){
			if(y > 1 && x > 1)										UL = true;
			if(y > 1 && x < GamePanel.COLS - 2) 					UR = true;
			if(y < GamePanel.ROWS - 2 && x > 1) 		  			DL = true;
			if(y < GamePanel.ROWS - 2 && x < GamePanel.COLS - 2)	DR = true;
		}
		if(y > 1)										U = true;
		if(x > 1)										L = true;
		if(x < GamePanel.COLS - 2)						R = true;
		if(y < GamePanel.ROWS - 2)						D = true;
		
		if(UL){
			if (board[y-1][x-1] == GamePanel.Piece.EMPTY){
				return new Point(x-1,y-1);
			}
		}
		if(U){
			if (board[y-1][x] == GamePanel.Piece.EMPTY){
				return new Point(x,y-1);
			}
		}
		if(UR){
			if (board[y-1][x+1] == GamePanel.Piece.EMPTY){
				return new Point(x+1,y-1);
			}
		}
		if(L){
			if (board[y][x-1] == GamePanel.Piece.EMPTY){
				return new Point(x-1,y);
			}
		}
		if(R){
			if (board[y][x+1] == GamePanel.Piece.EMPTY){
				return new Point(x+1,y);
			}
		}
		if(DL){
			if (board[y+1][x-1] == GamePanel.Piece.EMPTY){
				return new Point(x-1,y+1);
			}
		}
		if(D){
			if (board[y+1][x] == GamePanel.Piece.EMPTY){
				return new Point(x,y+1);
			}
		}
		if(DR){
			if (board[y+1][x+1] == GamePanel.Piece.EMPTY){
				return new Point(x+1,y+1);
			}
		}
		
		return new Point(-1,-1);
	}
	
	public void findMoves(int x, int y, GamePanel.Piece myColor, AIBoard base, ArrayList<AIBoard> moves, ArrayList<Point> places){
		/*AIBoard base2 = new AIBoard(copyBoard(base.getBoard()));
		base2.messages = new ArrayList<String>(base.messages);
		base2.moves = new ArrayList<Point>(base.moves);
		switch(base.prevDir){
		case UPLEFT:	base2.prevDir = GamePanel.Direction.UPLEFT;
						break;
		case UP:		base2.prevDir = GamePanel.Direction.UP;
						break;
		case UPRIGHT:	base2.prevDir = GamePanel.Direction.UPRIGHT;
						break;
		case LEFT:		base2.prevDir = GamePanel.Direction.LEFT;
						break;
		case RIGHT:		base2.prevDir = GamePanel.Direction.RIGHT;
						break;
		case DOWNLEFT:	base2.prevDir = GamePanel.Direction.DOWNLEFT;
						break;
		case DOWN:		base2.prevDir = GamePanel.Direction.DOWN;
						break;
		case DOWNRIGHT:	base2.prevDir = GamePanel.Direction.DOWNRIGHT;
						break;
		default:		break;
		}
		base2.chained_spots = new ArrayList<Point>(base.chained_spots);*/
		AIBoard base2 = copyAI(base);
		
		GamePanel.Piece[][] baseBoard = base2.getBoard();
		GamePanel.Direction dir = base2.prevDir;
		
		GamePanel.Piece color = myColor;
		if(myColor == GamePanel.Piece.PLAYER){
			color = GamePanel.Piece.OPPONENT;
		} else {
			color = GamePanel.Piece.PLAYER;
		}
		
		boolean ULafter = false;
		boolean ULbefore = false;
		boolean Uafter = false;
		boolean Ubefore = false;
		boolean URafter = false;
		boolean URbefore = false;
		boolean Lafter = false;
		boolean Lbefore = false;
		boolean Rafter = false;
		boolean Rbefore = false;
		boolean DLafter = false;
		boolean DLbefore = false;
		boolean Dafter = false;
		boolean Dbefore = false;
		boolean DRafter = false;
		boolean DRbefore = false;
		
		//Determine which directions to take into account, based on position
		
		if ((y+x)%2 == GamePanel.Diag){
			if(y > 1 && x > 1)										ULafter = true;
			if(y > 0 && x > 0 && 
				y < GamePanel.ROWS - 1 && x < GamePanel.COLS - 1)	ULbefore = true;
			if(y > 1 && x < GamePanel.COLS - 2) 					URafter = true;
			if(y > 0 && x < GamePanel.COLS - 1 &&
				y < GamePanel.ROWS - 1 && x > 0) 					URbefore = true;
			if(y < GamePanel.ROWS - 2 && x > 1) 		  			DLafter = true;
			if(y < GamePanel.ROWS - 1 && x > 0 &&
				y > 0 && x < GamePanel.COLS - 1) 					DLbefore = true;
			if(y < GamePanel.ROWS - 2 && x < GamePanel.COLS - 2)	DRafter = true;
			if(y < GamePanel.ROWS - 1 && x < GamePanel.COLS - 1 &&
				y > 0 && x > 0)										DRbefore = true;
		}
		if(y > 1)										Uafter = true;
		if(y > 0 && y < GamePanel.ROWS - 1)				Ubefore = true; 
		
		if(x > 1)										Lafter = true;
		if(x > 0 && x < GamePanel.COLS - 1)				Lbefore = true;
		if(x < GamePanel.COLS - 2)						Rafter = true;
		if(x < GamePanel.COLS - 1 && x > 0)				Rbefore = true;
		
		if(y < GamePanel.ROWS - 2)						Dafter = true;
		if(y < GamePanel.ROWS - 1 && y > 0)				Dbefore = true;
		
		
		//For each direction, test if the next piece is empty, if the appropriate
		//piece is the opponent color, if the direction from the last move was
		//not the same as this move, and if this space has been traveled to during
		//the current chain.
		if(ULafter){
			boolean valid = true;
			if(baseBoard[y-1][x-1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y-2][x-2] == color){
					if (dir != GamePanel.Direction.UPLEFT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y-1){
								if(base2.chained_spots.get(i).x == x-1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.UPLEFT, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(ULbefore){
			boolean valid = true;
			if(baseBoard[y-1][x-1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y+1][x+1] == color){
					if (dir != GamePanel.Direction.UPLEFT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y-1){
								if(base2.chained_spots.get(i).x == x-1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.UPLEFT, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Uafter){
			boolean valid = true;
			if(baseBoard[y-1][x] == GamePanel.Piece.EMPTY){
				if(baseBoard[y-2][x] == color){
					if (dir != GamePanel.Direction.UP){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y-1){
								if(base2.chained_spots.get(i).x == x){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.UP, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Ubefore){
			boolean valid = true;
			if(baseBoard[y-1][x] == GamePanel.Piece.EMPTY){
				if(baseBoard[y+1][x] == color){
					if (dir != GamePanel.Direction.UP){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y-1){
								if(base2.chained_spots.get(i).x == x){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.UP, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(URafter){
			boolean valid = true;
			if(baseBoard[y-1][x+1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y-2][x+2] == color){
					if (dir != GamePanel.Direction.UPRIGHT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y-1){
								if(base2.chained_spots.get(i).x == x+1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.UPRIGHT, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(URbefore){
			boolean valid = true;
			if(baseBoard[y-1][x+1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y+1][x-1] == color){
					if (dir != GamePanel.Direction.UPRIGHT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y-1){
								if(base2.chained_spots.get(i).x == x+1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.UPRIGHT, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Lafter){
			boolean valid = true;
			if(baseBoard[y][x-1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y][x-2] == color){
					if (dir != GamePanel.Direction.LEFT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y){
								if(base2.chained_spots.get(i).x == x-1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.LEFT, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Lbefore){
			boolean valid = true;
			if(baseBoard[y][x-1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y][x+1] == color){
					if (dir != GamePanel.Direction.LEFT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y){
								if(base2.chained_spots.get(i).x == x-1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.LEFT, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Rafter){
			boolean valid = true;
			if(baseBoard[y][x+1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y][x+2] == color){
					if (dir != GamePanel.Direction.RIGHT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y){
								if(base2.chained_spots.get(i).x == x+1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.RIGHT, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Rbefore){
			boolean valid = true;
			if(baseBoard[y][x+1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y][x-1] == color){
					if (dir != GamePanel.Direction.RIGHT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y){
								if(base2.chained_spots.get(i).x == x+1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.RIGHT, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(DLafter){
			boolean valid = true;
			if(baseBoard[y+1][x-1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y+2][x-2] == color){
					if (dir != GamePanel.Direction.DOWNLEFT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y+1){
								if(base2.chained_spots.get(i).x == x-1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.DOWNLEFT, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(DLbefore){
			boolean valid = true;
			if(baseBoard[y+1][x-1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y-1][x+1] == color){
					if (dir != GamePanel.Direction.DOWNLEFT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y+1){
								if(base2.chained_spots.get(i).x == x-1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.DOWNLEFT, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Dafter){
			boolean valid = true;
			if(baseBoard[y+1][x] == GamePanel.Piece.EMPTY){
				if(baseBoard[y+2][x] == color){
					if (dir != GamePanel.Direction.DOWN){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y+1){
								if(base2.chained_spots.get(i).x == x){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.DOWN, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(Dbefore){
			boolean valid = true;
			if(baseBoard[y+1][x] == GamePanel.Piece.EMPTY){
				if(baseBoard[y-1][x] == color){
					if (dir != GamePanel.Direction.DOWN){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y+1){
								if(base2.chained_spots.get(i).x == x){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.DOWN, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(DRafter){
			boolean valid = true;
			if(baseBoard[y+1][x+1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y+2][x+2] == color){
					if (dir != GamePanel.Direction.DOWNRIGHT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y+1){
								if(base2.chained_spots.get(i).x == x+1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.DOWNRIGHT, new Point(x,y), myColor, true);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		if(DRbefore){
			boolean valid = true;
			if(baseBoard[y+1][x+1] == GamePanel.Piece.EMPTY){
				if(baseBoard[y-1][x-1] == color){
					if (dir != GamePanel.Direction.DOWNRIGHT){
						for (int i = 0; i < base2.chained_spots.size(); i++){
							if(base2.chained_spots.get(i).y == y+1){
								if(base2.chained_spots.get(i).x == x+1){
									valid = false;
								}
							}
						}
						if(valid) {
							AIBoard newbase = copyAI(base2);
							Point newP = newbase.makeMove(GamePanel.Direction.DOWNRIGHT, new Point(x,y), myColor, false);
							moves.add(newbase);
							places.add(newP);
						}
					}
				}
			}
		}
		
		return;
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
	
	public AIBoard copyAI(AIBoard base){
		AIBoard base2 = new AIBoard(copyBoard(base.getBoard()));
		base2.messages = new ArrayList<String>(base.messages);
		base2.moves = new ArrayList<Point>(base.moves);
		switch(base.prevDir){
		case UPLEFT:	base2.prevDir = GamePanel.Direction.UPLEFT;
						break;
		case UP:		base2.prevDir = GamePanel.Direction.UP;
						break;
		case UPRIGHT:	base2.prevDir = GamePanel.Direction.UPRIGHT;
						break;
		case LEFT:		base2.prevDir = GamePanel.Direction.LEFT;
						break;
		case RIGHT:		base2.prevDir = GamePanel.Direction.RIGHT;
						break;
		case DOWNLEFT:	base2.prevDir = GamePanel.Direction.DOWNLEFT;
						break;
		case DOWN:		base2.prevDir = GamePanel.Direction.DOWN;
						break;
		case DOWNRIGHT:	base2.prevDir = GamePanel.Direction.DOWNRIGHT;
						break;
		default:		break;
		}
		base2.chained_spots = new ArrayList<Point>(base.chained_spots);
		return base2;
	}
}