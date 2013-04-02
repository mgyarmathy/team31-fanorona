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
	
	public void create(GamePanel.Piece mColor, Tree<AIBoard>.Node<AIBoard> t){
		AIBoard base = t.getData();
		GamePanel.Piece[][] baseBoard = copyBoard(base.getBoard());
		ArrayList<AIBoard> moves = new ArrayList<AIBoard>();
		ArrayList<Point> places = new ArrayList<Point>();
		for(int i = 0; i < baseBoard.length; i++){
			for (int j = 0; j < baseBoard[0].length; j++){
				if(baseBoard[i][j] == mColor){
					findMoves(j,i,mColor,base,moves,places);
					
				}
			}
		}
		for(int k = 0; k < moves.size(); k++){
			t.add(moves.get(k));
			findMoves(places.get(k).x,places.get(k).y,mColor,moves.get(k),moves,places);
		}
	}
	
	public AIBoard getMove(){
		create(color, tree.getRoot());
		int index = -1;
		int i = 0;
		int value = 1000;
		if(color == GamePanel.Piece.PLAYER)
			value = -1000;
		for(; i < tree.getRoot().getChildren().size(); i++){
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
			index = 0;
			tree.erase();
			ArrayList<AIBoard> boards = makeEmpty(color); //add empty moves
			for(int z = 0; z < boards.size(); z++){
				tree.getRoot().add(boards.get(z));
			}
			
		}
		Random ran = new Random();	
		int ranCase = ran.nextInt();
		GamePanel.Piece color2 = GamePanel.Piece.PLAYER;
		int negarank = 1000;
		if(color == GamePanel.Piece.PLAYER){
			color2 = GamePanel.Piece.OPPONENT;
			negarank = -1000;
		}
		
		for(int y = 0; y < tree.getRoot().getChildren().size(); y++){
			create(color2, tree.getRoot().getChildren().get(y)); //add opponent moves
			if(tree.getRoot().getChildren().get(y).getChildren().size() == 0){
				ArrayList<AIBoard> opponentEmpty = makeEmpty(color2);
				for(int q = 0; q < opponentEmpty.size(); q++){
					tree.getRoot().getChildren().get(y).add(opponentEmpty.get(q));
				}
			}
		}
		int worstCase = 1000;
		int bestCase = 1000;
		if(color == GamePanel.Piece.PLAYER){
			worstCase = -1000;
			bestCase = -1000;
		}
		
		for(i = 0; i < tree.getRoot().getChildren().size(); i++){ //for every move
			if(tree.getRoot().getChildren().get(i) != null){
				Tree<AIBoard>.Node<AIBoard> base2 = tree.getRoot().getChildren().get(i);
				create(color, base2);
				if(base2.getChildren().size() == 0){
					ArrayList<AIBoard> futureEmpty = makeEmpty(color);
					for(int w = 0; w < futureEmpty.size(); w++){
						base2.add(futureEmpty.get(w));
					}
				}
				if(color == GamePanel.Piece.PLAYER){
					worstCase = 1000;
				} else {
					worstCase = -1000;
				}
				for(int r = 0; r < base2.getChildren().size(); r++){ //for every opponent response
					if(color == GamePanel.Piece.OPPONENT){
						for(int s = 0; s < base2.getChildren().get(r).getChildren().size(); s++){ //for every future move
							if(base2.getChildren().get(r).getChildren().get(s).getData().rank > worstCase){
								worstCase = base2.getChildren().get(r).getChildren().get(s).getData().rank;
							}
						}
					}else {	
						for(int s = 0; s < base2.getChildren().get(r).getChildren().size(); s++){ //for every future move
							if(base2.getChildren().get(r).getChildren().get(s).getData().rank < worstCase){
								worstCase = base2.getChildren().get(r).getChildren().get(s).getData().rank;
							}
						}
					}
					if(color == GamePanel.Piece.OPPONENT){
						if(worstCase < bestCase){
							bestCase = worstCase;
							index = i;
						} else if (worstCase == bestCase){
							if(ran.nextInt() > ranCase){
								ranCase = ran.nextInt();
								index = i;
							}
						}
					} else {
						if(worstCase > bestCase){
							bestCase = worstCase;
							index = i;
						} else if (worstCase == bestCase){
							if(ran.nextInt() > ranCase){
								ranCase = ran.nextInt();
								index = i;
							}
						}
					}
				}
			}
		}
		for(int x = 0; x < tree.getRoot().getChildren().get(index).getData().messages.size(); x++){
			GamePanel.info.write(tree.getRoot().getChildren().get(index).getData().messages.get(x));
		}
		return tree.getRoot().getChildren().get(index).getData();
	}
	
	public ArrayList<AIBoard> makeEmpty(GamePanel.Piece mColor){
		ArrayList<AIBoard> retList = new ArrayList<AIBoard>();
		
		GamePanel.Piece[][] base = copyBoard(tree.getRoot().getData().getBoard());
		for (int i = 0; i < base.length; i++){
			for (int j = 0; j < base[0].length; j++){
				if(base[i][j] == mColor){
					ArrayList<Point> p = findSpace(j,i,base);
					for(int k = 0; k < p.size(); k++){
						GamePanel.Piece[][] base2 = copyBoard(base);
						GamePanel.Direction dir = GamePanel.Direction.DUMMY;
						if(p.get(k).x - j == -1){
							switch(p.get(k).y - i){
							case -1: dir = GamePanel.Direction.UPLEFT;
									 break;
							case 0:	 dir = GamePanel.Direction.LEFT;
									 break;
							case 1:  dir = GamePanel.Direction.DOWNLEFT;
									 break;
							default: break;
							}
						} else if (p.get(k).x - j == 0){
							switch(p.get(k).y - i){
							case -1: dir = GamePanel.Direction.UP;
									 break;
							case 0:	 dir = GamePanel.Direction.NEUTRAL;
									 break;
							case 1:  dir = GamePanel.Direction.DOWN;
									 break;
							default: break;
							}
						} else if (p.get(k).x - j == 1){
							switch(p.get(k).y - i){
							case -1: dir = GamePanel.Direction.UPRIGHT;
									 break;
							case 0:	 dir = GamePanel.Direction.RIGHT;
									 break;
							case 1:  dir = GamePanel.Direction.DOWNRIGHT;
									 break;
							default: break;
							}
						}
						AIBoard curBoard = new AIBoard(base2);
						curBoard.makeMove(dir, new Point(j,i),mColor,true);
						retList.add(curBoard);
					}
				}
			}
		}

		return retList;
	}
	
	public ArrayList<Point> findSpace(int x, int y, GamePanel.Piece[][] board){
		ArrayList<Point> retList = new ArrayList<Point>();
		boolean UL = false;
		boolean U = false;
		boolean UR = false;
		boolean L = false;
		boolean R = false;
		boolean DL = false;
		boolean D = false;
		boolean DR = false;
		
		if ((y+x)%2 == GamePanel.Diag){
			if(y > 0 && x > 0)										UL = true;
			if(y > 0 && x < GamePanel.COLS - 1) 					UR = true;
			if(y < GamePanel.ROWS - 1 && x > 0) 		  			DL = true;
			if(y < GamePanel.ROWS - 1 && x < GamePanel.COLS - 1)	DR = true;
		}
		if(y > 0)										U = true;
		if(x > 0)										L = true;
		if(x < GamePanel.COLS - 1)						R = true;
		if(y < GamePanel.ROWS - 1)						D = true;
		
		if(UL){
			if (board[y-1][x-1] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x-1,y-1));
			}
		}
		if(U){
			if (board[y-1][x] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x,y-1));
			}
		}
		if(UR){
			if (board[y-1][x+1] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x+1,y-1));
			}
		}
		if(L){
			if (board[y][x-1] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x-1,y));
			}
		}
		if(R){
			if (board[y][x+1] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x+1,y));
			}
		}
		if(DL){
			if (board[y+1][x-1] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x-1,y+1));
			}
		}
		if(D){
			if (board[y+1][x] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x,y+1));
			}
		}
		if(DR){
			if (board[y+1][x+1] == GamePanel.Piece.EMPTY){
				retList.add(new Point(x+1,y+1));
			}
		}
		
		return retList;
	}
	
	public void findMoves(int x, int y, GamePanel.Piece myColor, AIBoard base, ArrayList<AIBoard> moves, ArrayList<Point> places){
		AIBoard base2 = copyAI(base);
		
		GamePanel.Piece[][] baseBoard = base2.getBoard();
		GamePanel.Direction dir = base2.prevDir;
		
		GamePanel.Piece color2 = myColor;
		if(myColor == GamePanel.Piece.PLAYER){
			color2 = GamePanel.Piece.OPPONENT;
		} else {
			color2 = GamePanel.Piece.PLAYER;
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
				if(baseBoard[y-2][x-2] == color2){
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
				if(baseBoard[y+1][x+1] == color2){
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
				if(baseBoard[y-2][x] == color2){
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
				if(baseBoard[y+1][x] == color2){
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
				if(baseBoard[y-2][x+2] == color2){
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
				if(baseBoard[y+1][x-1] == color2){
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
				if(baseBoard[y][x-2] == color2){
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
				if(baseBoard[y][x+1] == color2){
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
				if(baseBoard[y][x+2] == color2){
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
				if(baseBoard[y][x-1] == color2){
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
				if(baseBoard[y+2][x-2] == color2){
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
				if(baseBoard[y-1][x+1] == color2){
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
				if(baseBoard[y+2][x] == color2){
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
				if(baseBoard[y-1][x] == color2){
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
				if(baseBoard[y+2][x+2] == color2){
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
				if(baseBoard[y-1][x-1] == color2){
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
		base2.moves = new ArrayList<Boolean>(base.moves);
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