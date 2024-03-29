import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


// GamePanel displays the current state of the board.
public class GamePanel extends JPanel{
	
	static int ROWS = 5;
	static int COLS = 9;
	static int BLACK = 1;
	static int WHITE = 0;
	
	
	boolean servermode=false;
	int Player=0;
	
	
	int AImode = 0;
	
	int PlayerPieceCount, OppPieceCount, EmptyPieceCount;
	int TurnCount;
	
	static int Diag = 0;
	
	String Player1move="",Player2move="";
	boolean Player1newmove=false,Player2newmove=false; 
	
	boolean P1AI=false,P2AI=false;
	
	boolean win,draw;
	
	static enum Piece {PLAYER, OPPONENT, EMPTY, SACRIFICE};
	static enum Direction {NEUTRAL, UPLEFT, UP, UPRIGHT, LEFT, RIGHT, DOWNLEFT, DOWN, DOWNRIGHT, DUMMY};
	static enum Type {ADVANCE, WITHDRAW, PAIKA};
	private Color playerColor = Color.WHITE;
	private Color opponentColor = Color.BLACK;
	private String playerName = "Player 1";
	
	Piece[][] board = new Piece[ROWS][COLS];
	private Rectangle[][] buttons = new Rectangle[ROWS][COLS];
	private Rectangle SacButton = new Rectangle();
	Point selected_piece = null;
	private Point choice1 = null;
	private Point choice2 = null;
	private Point sacrificeP = null;
	private Point sacrificeO = null;
	private ArrayList<Point> chained_spots = new ArrayList<Point>();
	private boolean chain_piece = false;
	private Direction previous_direction = Direction.DUMMY;
	private boolean overrideMode = false;
	private Direction overrideDir = Direction.DUMMY;
	
	static InfoPanel info;
	public Stopwatch stopw;
	

	public GamePanel(InfoPanel i ,Stopwatch s){
		setPreferredSize(new Dimension(700,500));
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setVisible(true);
		info = i;
		stopw = s;
		newGame();
		
		
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				
				/*if(servermode && P1AI){
					info.write("Computer is playing");
					return;
				}*/
				
				/*if(servermode && P2AI){
					info.write("Computer is playing");
					return;
				}*/
				
				
				if(servermode && TurnCount%2+1!=Player){
					info.write("Not your turn");
					return;
				}
				
				if(P1AI && TurnCount%2 == WHITE){
					return;
				} else if (P2AI && TurnCount%2 == BLACK ){
					return;
				}
				info.initial=true;// initial print for info panel stop
				if(!stopw.running() && !win && !draw) stopw.timeStart();
				boolean emptyClick=true;
				
				Point p = e.getPoint();
				for(int row = 0; row < buttons.length; row++){
					for(int col = 0; col < buttons[0].length; col++){
						if(buttons[row][col].contains(p) && !win && !draw){
							emptyClick=false;
							
							if(overrideMode){
								if((choice1.x != col || choice1.y != row) &&
									(choice2.x != col || choice2.y != row)){
										info.write("Must choose which direction to eliminate pieces");
								}
							}
							//select piece
							if((selected_piece == null && (board[row][col]!=Piece.EMPTY && board[row][col]!=Piece.SACRIFICE)) && !overrideMode){ //no piece selected
								selected_piece = new Point(col, row);
								Piece color = board[selected_piece.y][selected_piece.x];
								if(TurnCount%2==WHITE && color != Piece.PLAYER) {
									info.write("It's "+printTurn());
									if(!chain_piece)
										selected_piece =null; 
								}
								if(TurnCount%2==BLACK && color != Piece.OPPONENT) {
									info.write("It's "+printTurn());
									if(!chain_piece)
										selected_piece =null; 
								}
								break;
							}
							//move selected piece
							else if((selected_piece != null && (board[row][col]==Piece.EMPTY || (selected_piece.x == col && selected_piece.y == row)))
										|| overrideMode){ 

								movePiece(col,row);

							}
							
							else if(board[row][col]!=Piece.EMPTY) { info.write("There is a piece There!"); break;} //Spot taken
							else { info.write("This spot is empty!");  break; } //Spot empty
						}
					}
				}
				if(SacButton.contains(p) && !win && !draw){
					emptyClick = false;
					if(overrideMode || chain_piece){
						info.write("Cannot sacrifice a piece now.");
					} else if (selected_piece == null){
						info.write("Must choose a piece to sacrifice.");
					} else {
						board[selected_piece.y][selected_piece.x] = Piece.SACRIFICE;
						if(TurnCount%2 == WHITE){
							sacrificeP = new Point(selected_piece.x,selected_piece.y);
						} else {
							sacrificeO = new Point(selected_piece.x,selected_piece.y);
						}
						
						
						// sacrifice move string
						if(TurnCount%2==WHITE){ 
							Player1move+="S "+Integer.toString(selected_piece.x+1)+" "+Integer.toString(ROWS-selected_piece.y);
							Player1newmove=true;
							Player2move="";
						}
						else{
							Player2move+="S "+Integer.toString(selected_piece.x+1)+" "+Integer.toString(ROWS-selected_piece.y);
							Player2newmove=true;
							Player1move="";
						}
						// send move to server **************************************
						
						selected_piece = null;
						countPieces();
						info.write("Piece has been sacrificed to block moves.");
					}
				}
				if(emptyClick) {
					if(!chain_piece && !overrideMode)
						selected_piece = null; 
				} // deselect piece
			}
		});
	}
	
	
	public int serverMovePiece(Point sel_piece,int moveToCol, int moveToRow,String movedirection){
		// row logic must be moveToRow = ROWS - row  from
		// server end to match our board 
		
		// if movedata is positive move was good
		// if +1 must selected piece(s) to take off
		// if negative move not good or other valid move available
		
		selected_piece = sel_piece;
		
		int movedata = movePiece(moveToCol,moveToRow);
		
		if(movedata == +1){
			int choiceMoveCol,choiceMoveRow;
			if(movedirection.equals("A")){
				
				if(overrideDir== Direction.DOWN) { choiceMoveCol= moveToCol; choiceMoveRow = moveToRow+1; }
				else if(overrideDir== Direction.DOWNLEFT) { choiceMoveCol= moveToCol - 1; choiceMoveRow = moveToRow+1; }
				else if(overrideDir== Direction.DOWNRIGHT) { choiceMoveCol= moveToCol + 1; choiceMoveRow = moveToRow+1; }
				else if(overrideDir== Direction.LEFT) { choiceMoveCol= moveToCol - 1; choiceMoveRow = moveToRow; }
				else if(overrideDir== Direction.RIGHT) { choiceMoveCol= moveToCol + 1; choiceMoveRow = moveToRow; }
				else if(overrideDir== Direction.UPLEFT) { choiceMoveCol= moveToCol-1; choiceMoveRow = moveToRow-1; }
				else if(overrideDir== Direction.UPRIGHT) { choiceMoveCol= moveToCol + 1; choiceMoveRow = moveToRow-1; }
				else { choiceMoveCol= moveToCol; choiceMoveRow = moveToRow-1; }
				
				movedata = movePiece(choiceMoveCol,choiceMoveRow);
			}
			
			else if(movedirection.equals("W")){
				
				if(overrideDir== Direction.DOWN) { choiceMoveCol= moveToCol; choiceMoveRow = moveToRow+2; }
				else if(overrideDir== Direction.DOWNLEFT) { choiceMoveCol= moveToCol + 2; choiceMoveRow = moveToRow+2; }
				else if(overrideDir== Direction.DOWNRIGHT) { choiceMoveCol= moveToCol - 2; choiceMoveRow = moveToRow+2; }
				else if(overrideDir== Direction.LEFT) { choiceMoveCol= moveToCol + 2; choiceMoveRow = moveToRow; }
				else if(overrideDir== Direction.RIGHT) { choiceMoveCol= moveToCol - 2; choiceMoveRow = moveToRow; }
				else if(overrideDir== Direction.UPLEFT) { choiceMoveCol= moveToCol+2; choiceMoveRow = moveToRow-2; }
				else if(overrideDir== Direction.UPRIGHT) { choiceMoveCol= moveToCol - 2; choiceMoveRow = moveToRow-2; }
				else { choiceMoveCol= moveToCol; choiceMoveRow = moveToRow-1; }
				
				movedata = movePiece(choiceMoveCol,choiceMoveRow);
			}
		}
		
		return movedata;
	}
	
	public int serverSacrificePiece(Point sel_piece){
		selected_piece = sel_piece;
		
		// if returning -1 invalid move
		// if returning -99 big error!
		
		if(overrideMode || chain_piece){
			info.write("Cannot sacrifice a piece now.");
			return -1;
		} else if (selected_piece == null){
			info.write("Must choose a piece to sacrifice.");
			return -99;
		} else {
			board[selected_piece.y][selected_piece.x] = Piece.SACRIFICE;
			if(TurnCount%2 == WHITE){
				sacrificeP = new Point(selected_piece.x,selected_piece.y);
			} else {
				sacrificeO = new Point(selected_piece.x,selected_piece.y);
			}
			
			
			// sacrifice move string
			if(TurnCount%2==WHITE){ 
				Player1move+="S "+Integer.toString(selected_piece.x+1)+" "+Integer.toString(ROWS-selected_piece.y);
				Player1newmove=true;
				Player2move="";
			}
			else{
				Player2move+="S "+Integer.toString(selected_piece.x+1)+" "+Integer.toString(ROWS-selected_piece.y);
				Player2newmove=true;
				Player1move="";
			}
			// send move to server **************************************
			
			selected_piece = null;
			countPieces();
			info.write("Piece has been sacrificed to block moves.");
		}
		
		return 0;
	}
	
	public int movePiece(int col, int row){
		
		// 0 successful move
		// +1 must select piece to remove
		// -1 can't move there
		// -2 already moved there
		// -4 valid move with other piece
		
		boolean valid_move = true;
		boolean before = false;
		boolean after = false;
		Piece color = board[selected_piece.y][selected_piece.x];
		Piece opposite;
		if(color == Piece.PLAYER){
			opposite = Piece.OPPONENT;
		} else {
			opposite = Piece.PLAYER;
		}
		Direction dir = Direction.NEUTRAL;
		
		if(!overrideMode){
			//Check if not moving to adjacent piece
			if(Math.abs(selected_piece.y - row) > 1 || Math.abs(selected_piece.x - col) > 1){
				info.write("Can only move to adjacent pieces.");
				if(!chain_piece){
					selected_piece = null;
				}
				return -1;
			}
			//Check if the piece cannot move diagonally
			if((row + col)%2 != Diag){
				int x_dif = Math.abs(selected_piece.x - col);
				int y_dif = Math.abs(selected_piece.y - row);
				if(x_dif != 0 && y_dif != 0){
					info.write("This piece cannot move diagonally.");
					if(!chain_piece){
						selected_piece = null;
					}
					return -1;
				}
			}
			
			boolean repeat = false;
			if(chain_piece == true){
				for (int k = 0; k < chained_spots.size(); k++){
						if(chained_spots.get(k).y == row && chained_spots.get(k).x == col){
							repeat = true;
							break;
					}
				}
			}
			
			if(repeat){
				info.write("Can't move to the same place during a chain.");
				return -2;
			}
			//Move detection goes here
			
			
			if(col - selected_piece.x == -1){
				switch(row - selected_piece.y){
				case -1: dir = Direction.UPLEFT;
						 break;
				case 0:	 dir = Direction.LEFT;
						 break;
				case 1:  dir = Direction.DOWNLEFT;
						 break;
				default: break;
				}
			} else if (col - selected_piece.x == 0){
				switch(row - selected_piece.y){
				case -1: dir = Direction.UP;
						 break;
				case 0:	 dir = Direction.NEUTRAL;
						 break;
				case 1:  dir = Direction.DOWN;
						 break;
				default: break;
				}
			} else if (col - selected_piece.x == 1){
				switch(row - selected_piece.y){
				case -1: dir = Direction.UPRIGHT;
						 break;
				case 0:	 dir = Direction.RIGHT;
						 break;
				case 1:  dir = Direction.DOWNRIGHT;
						 break;
				default: break;
				}
			}
			
			if(chain_piece && dir == previous_direction){
				info.write("Can't move in same direction twice");
				return -2;
			}
			
			
			
			boolean innate = false;
			boolean blank = false;
			switch(dir){
			case NEUTRAL:		innate = true;
								if(chain_piece == true){
								info.write("Chain ended.");
								countPieces();
								chain_piece = false;
								chained_spots.clear();
							}
							break;
			case UPLEFT: 	if(selected_piece.y > 1 && selected_piece.x > 1){
								if(board[selected_piece.y-2][selected_piece.x-2] == opposite){
									after = true;
								}
							}
							if(selected_piece.y < ROWS - 1 && selected_piece.x < COLS - 1){
								if(board[selected_piece.y+1][selected_piece.x+1] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x + 1, selected_piece.y + 1);
								choice2 = new Point(selected_piece.x - 2, selected_piece.y - 2);
							}
							break;
			case UP:		if(selected_piece.y > 1){
								if(board[selected_piece.y-2][selected_piece.x] == opposite){
									after = true;
								}
							}
							if(selected_piece.y < ROWS - 1){
								if(board[selected_piece.y+1][selected_piece.x] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x, selected_piece.y + 1);
								choice2 = new Point(selected_piece.x, selected_piece.y - 2);
							}
							break;
			case UPRIGHT:	if(selected_piece.y > 1 && selected_piece.x < COLS - 2){
								if(board[selected_piece.y-2][selected_piece.x+2] == opposite){
									after = true;
								}
							}
							if(selected_piece.y < ROWS - 1 && selected_piece.x > 0){
								if(board[selected_piece.y+1][selected_piece.x-1] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x - 1, selected_piece.y + 1);
								choice2 = new Point(selected_piece.x + 2, selected_piece.y - 2);
							}
							break;
			case LEFT:		if(selected_piece.x > 1){
								if(board[selected_piece.y][selected_piece.x-2] == opposite){
									after = true;
								}
							}
							if(selected_piece.x < COLS - 1){
								if(board[selected_piece.y][selected_piece.x+1] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x + 1, selected_piece.y);
								choice2 = new Point(selected_piece.x - 2, selected_piece.y);
							}
							break;
			case RIGHT:		if(selected_piece.x < COLS - 2){
								if(board[selected_piece.y][selected_piece.x+2] == opposite){
									after = true;
								}
							}
							if(selected_piece.x > 0){
								if(board[selected_piece.y][selected_piece.x-1] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x - 1, selected_piece.y);
								choice2 = new Point(selected_piece.x + 2, selected_piece.y);
							}
							break;
			case DOWNLEFT:	if(selected_piece.y < ROWS - 2 && selected_piece.x > 1){
								if(board[selected_piece.y+2][selected_piece.x-2] == opposite){
									after = true;
								}
							}
							if(selected_piece.y > 0 && selected_piece.x < COLS - 1){
								if(board[selected_piece.y-1][selected_piece.x+1] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x + 1, selected_piece.y - 1);
								choice2 = new Point(selected_piece.x - 2, selected_piece.y + 2);
							}
							break;
			case DOWN:		if(selected_piece.y < ROWS - 2){
								if(board[selected_piece.y+2][selected_piece.x] == opposite){
									after = true;
								}
							}
							if(selected_piece.y > 0){
								if(board[selected_piece.y-1][selected_piece.x] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x, selected_piece.y - 1);
								choice2 = new Point(selected_piece.x, selected_piece.y + 2);
							}
							break;
			case DOWNRIGHT:	if(selected_piece.y < ROWS - 2 && selected_piece.x < COLS - 2){
								if(board[selected_piece.y+2][selected_piece.x+2] == opposite){
									after = true;
								}
							}
							if(selected_piece.y > 0 && selected_piece.x > 0){
								if(board[selected_piece.y-1][selected_piece.x-1] == opposite){
									before = true;
								}
							}	
							if(!(before) && !(after)){
								if(chain_piece){
									innate = true;
									blank = true;
								} else {
									valid_move = checkForBlank(opposite,dir,selected_piece);
									if(!valid_move){
										blank = true;
										innate = true;
									}
								}
								break;
							} else if(before && after){
								overrideMode = true;
								overrideDir = dir;
								choice1 = new Point(selected_piece.x - 1, selected_piece.y - 1);
								choice2 = new Point(selected_piece.x + 2, selected_piece.y + 2);
							}
							break;
			default:		break;
			}
			
			if(overrideMode){
				info.write("Selecte the piece(s) you want to eliminate");
				return +1;
			}
			if(innate){
				if(!chain_piece){
					selected_piece = null;
				}
				if(blank){
					info.write("Must take opponent piece off board.");
				}
				return -4;
			}
		} else {
			
			if(choice1.x == col && choice1.y == row){
				before = true;
				after = false;
			} else if(choice2.x == col && choice2.y == row){
				before = false;
				after = true;
			} else if (selected_piece.x == col && selected_piece.y == row){
				selected_piece = null;
				choice1 = null;
				choice2 = null;
				overrideMode = false;
				overrideDir = Direction.DUMMY;
				if(chain_piece){
					info.write("Chain Ended.");
					chain_piece = false;
					chained_spots.clear();
					countPieces();
				}
				return 0;
			} else {
				info.write("Must select one of two pieces.");
				return +1;
			}
			overrideMode = false;
			dir = overrideDir;
			switch(dir){
			case UPLEFT:	row = selected_piece.y - 1;
							col = selected_piece.x - 1;
							break;
			case UP:		row = selected_piece.y - 1;
							col = selected_piece.x;
							break;
			case UPRIGHT:	row = selected_piece.y - 1;
							col = selected_piece.x + 1;
							break;				
			case LEFT:		row = selected_piece.y;
							col = selected_piece.x - 1;
							break;
			case RIGHT:		row = selected_piece.y;
							col = selected_piece.x + 1;
							break;
			case DOWNLEFT:	row = selected_piece.y + 1;
							col = selected_piece.x - 1;
							break;				
			case DOWN:		row = selected_piece.y + 1;
							col = selected_piece.x;
							break;				
			case DOWNRIGHT:	row = selected_piece.y + 1;
							col = selected_piece.x + 1;
							break;		
			default:		break;
			}
			choice1 = null;
			choice2 = null;
			overrideDir = Direction.DUMMY;
		}
		
		if(valid_move){
			char c=(char) (selected_piece.y+65);
			char c2=(char) (row+65);
			info.write(Character.toString(c)+Integer.toString(selected_piece.x+1)+" moved to "+Character.toString(c2)+Integer.toString(col+1));
			
			
			
			printMove(selected_piece, new Point(col,row), after, before);
				
			//board[row][col] = color;
			//board[selected_piece.y][selected_piece.x]= Piece.EMPTY;
			int countBefore = count();
			interpretMove(selected_piece, new Point(col, row), after);

			chained_spots.add(new Point(selected_piece.x, selected_piece.y));
			
			boolean thingsEliminated = false;
			if (countBefore - count() != 0){
				thingsEliminated = true;
			}
			selected_piece.x = col;
			selected_piece.y = row;
			
			//CHECK FOR OTHER MOVES
			boolean next_move = false;
			if(thingsEliminated){
				next_move = detectMove(selected_piece, dir, opposite);
			}
			if(next_move){
					chain_piece = true;
					
					// server chain move string stuff ****************************************
					if(TurnCount%2==WHITE){ 
						Player1move+=" + ";
					}
					else{
						Player2move+=" + ";
					}
					//************************************************************************

					
				previous_direction = dir;
				stopw.timeReset();
				stopw.timeStart();
			} else {
				chain_piece = false;
				chained_spots.clear();
				previous_direction = Direction.DUMMY;
			}
			// if move is valid and no more moves to make - countPieces
			if(chain_piece == false){
				
				// set move new and clear other player move string ****************************************************
				if(TurnCount%2==WHITE){
					Player1newmove=true;
					Player2move="";
				}
				else{
					Player2newmove=true;
					Player1move="";
				}
				//send move to other player function ******************************************************************
			
				
				countPieces();
				selected_piece = null;
			}
		}
		return 0;
	}
	
	public void printMove(Point start, Point end, boolean after, boolean before){
		//TODO:detect advance/withdraw/paika ********************************************************
		if(!after && !before){	
			if(TurnCount%2==WHITE){ 
				Player1move+="P "+Integer.toString(start.x+1)+" "+Integer.toString(ROWS-start.y)+" "+Integer.toString(end.x+1)+" "+Integer.toString(ROWS-end.y);
			}
			else{
				Player2move+="P "+Integer.toString(start.x+1)+" "+Integer.toString(ROWS-start.y)+" "+Integer.toString(end.x+1)+" "+Integer.toString(ROWS-end.y);
			}
		}
		else if(after){
			if(TurnCount%2==WHITE){ 
				Player1move+="A "+Integer.toString(start.x+1)+" "+Integer.toString(ROWS-start.y)+" "+Integer.toString(end.x+1)+" "+Integer.toString(ROWS-end.y);
			}
			else{
				Player2move+="A "+Integer.toString(start.x+1)+" "+Integer.toString(ROWS-start.y)+" "+Integer.toString(end.x+1)+" "+Integer.toString(ROWS-end.y);
			}
		}
		else {
			if(TurnCount%2==WHITE){ 
				Player1move+="W "+Integer.toString(start.x+1)+" "+Integer.toString(ROWS-start.y)+" "+Integer.toString(end.x+1)+" "+Integer.toString(ROWS-end.y);
			}
			else{
				Player2move+="W "+Integer.toString(start.x+1)+" "+Integer.toString(ROWS-start.y)+" "+Integer.toString(end.x+1)+" "+Integer.toString(ROWS-end.y);
			}
		}
		//******************************************************************************************
	}
	
	public boolean checkForBlank(Piece color, Direction dir,Point p){
		
		if(chain_piece){
			return detectMove(p,dir,color);
		}
		for(int col = 0; col < COLS; col++){
			for(int row = 0; row < ROWS; row++){
				if(detectMove(new Point(col,row),Direction.DUMMY, color)){
					char c=(char) (row+65);
					info.write(Character.toString(c)+(col+1)+" has a valid move");
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean detectMove(Point start, Direction dir, Piece color){

		if(board[start.y][start.x] == Piece.EMPTY || board[start.y][start.x] == color || board[start.y][start.x] == Piece.SACRIFICE){
			return false;
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
		
		if ((start.y+start.x)%2 == Diag){
			if(start.y > 1 && start.x > 1)					ULafter = true;
			if(start.y > 0 && start.x > 0 && 
				start.y < ROWS - 1 && start.x < COLS - 1)	ULbefore = true;
			if(start.y > 1 && start.x < COLS - 2) 			URafter = true;
			if(start.y > 0 && start.x < COLS - 1 &&
				start.y < ROWS - 1 && start.x > 0) 			URbefore = true;
			if(start.y < ROWS - 2 && start.x > 1) 		  	DLafter = true;
			if(start.y < ROWS - 1 && start.x > 0 &&
				start.y > 0 && start.x < COLS - 1) 			DLbefore = true;
			if(start.y < ROWS - 2 && start.x < COLS - 2)	DRafter = true;
			if(start.y < ROWS - 1 && start.x < COLS - 1 &&
				start.y > 0 && start.x > 0)					DRbefore = true;
		}
		if(start.y > 1)										Uafter = true;
		if(start.y > 0 && start.y < ROWS - 1)				Ubefore = true; 
		
		if(start.x > 1)										Lafter = true;
		if(start.x > 0 && start.x < COLS - 1)				Lbefore = true;
		if(start.x < COLS - 2)								Rafter = true;
		if(start.x < COLS - 1 && start.x > 0)				Rbefore = true;
		
		if(start.y < ROWS - 2)								Dafter = true;
		if(start.y < ROWS - 1 && start.y > 0)				Dbefore = true;
		
		
		//For each direction, test if the next piece is empty, if the appropriate
		//piece is the opponent color, if the direction from the last move was
		//not the same as this move, and if this space has been traveled to during
		//the current chain.
		if(ULafter){
			boolean valid = true;
			if(board[start.y-1][start.x-1] == Piece.EMPTY){
				if(board[start.y-2][start.x-2] == color){
					if (dir != Direction.UPLEFT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y-1){
								if(chained_spots.get(i).x == start.x-1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(ULbefore){
			boolean valid = true;
			if(board[start.y-1][start.x-1] == Piece.EMPTY){
				if(board[start.y+1][start.x+1] == color){
					if (dir != Direction.UPLEFT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y-1){
								if(chained_spots.get(i).x == start.x-1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Uafter){
			boolean valid = true;
			if(board[start.y-1][start.x] == Piece.EMPTY){
				if(board[start.y-2][start.x] == color){
					if (dir != Direction.UP){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y-1){
								if(chained_spots.get(i).x == start.x){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Ubefore){
			boolean valid = true;
			if(board[start.y-1][start.x] == Piece.EMPTY){
				if(board[start.y+1][start.x] == color){
					if (dir != Direction.UP){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y-1){
								if(chained_spots.get(i).x == start.x){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(URafter){
			boolean valid = true;
			if(board[start.y-1][start.x+1] == Piece.EMPTY){
				if(board[start.y-2][start.x+2] == color){
					if (dir != Direction.UPRIGHT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y-1){
								if(chained_spots.get(i).x == start.x+1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(URbefore){
			boolean valid = true;
			if(board[start.y-1][start.x+1] == Piece.EMPTY){
				if(board[start.y+1][start.x-1] == color){
					if (dir != Direction.UPRIGHT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y-1){
								if(chained_spots.get(i).x == start.x+1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Lafter){
			boolean valid = true;
			if(board[start.y][start.x-1] == Piece.EMPTY){
				if(board[start.y][start.x-2] == color){
					if (dir != Direction.LEFT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y){
								if(chained_spots.get(i).x == start.x-1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Lbefore){
			boolean valid = true;
			if(board[start.y][start.x-1] == Piece.EMPTY){
				if(board[start.y][start.x+1] == color){
					if (dir != Direction.LEFT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y){
								if(chained_spots.get(i).x == start.x-1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Rafter){
			boolean valid = true;
			if(board[start.y][start.x+1] == Piece.EMPTY){
				if(board[start.y][start.x+2] == color){
					if (dir != Direction.RIGHT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y){
								if(chained_spots.get(i).x == start.x+1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Rbefore){
			boolean valid = true;
			if(board[start.y][start.x+1] == Piece.EMPTY){
				if(board[start.y][start.x-1] == color){
					if (dir != Direction.RIGHT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y){
								if(chained_spots.get(i).x == start.x+1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(DLafter){
			boolean valid = true;
			if(board[start.y+1][start.x-1] == Piece.EMPTY){
				if(board[start.y+2][start.x-2] == color){
					if (dir != Direction.DOWNLEFT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y+1){
								if(chained_spots.get(i).x == start.x-1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(DLbefore){
			boolean valid = true;
			if(board[start.y+1][start.x-1] == Piece.EMPTY){
				if(board[start.y-1][start.x+1] == color){
					if (dir != Direction.DOWNLEFT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y+1){
								if(chained_spots.get(i).x == start.x-1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Dafter){
			boolean valid = true;
			if(board[start.y+1][start.x] == Piece.EMPTY){
				if(board[start.y+2][start.x] == color){
					if (dir != Direction.DOWN){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y+1){
								if(chained_spots.get(i).x == start.x){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(Dbefore){
			boolean valid = true;
			if(board[start.y+1][start.x] == Piece.EMPTY){
				if(board[start.y-1][start.x] == color){
					if (dir != Direction.DOWN){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y+1){
								if(chained_spots.get(i).x == start.x){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(DRafter){
			boolean valid = true;
			if(board[start.y+1][start.x+1] == Piece.EMPTY){
				if(board[start.y+2][start.x+2] == color){
					if (dir != Direction.DOWNRIGHT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y+1){
								if(chained_spots.get(i).x == start.x+1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		if(DRbefore){
			boolean valid = true;
			if(board[start.y+1][start.x+1] == Piece.EMPTY){
				if(board[start.y-1][start.x-1] == color){
					if (dir != Direction.DOWNRIGHT){
						for (int i = 0; i < chained_spots.size(); i++){
							if(chained_spots.get(i).y == start.y+1){
								if(chained_spots.get(i).x == start.x+1){
									valid = false;
								}
							}
						}
						if(valid) return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
	public boolean interpretMove(Point from, Point to, boolean after){
		Piece color = Piece.EMPTY;
		if(TurnCount%2 == WHITE){
			color = Piece.PLAYER;
		} else {
			color = Piece.OPPONENT;
		}
		Direction dir = Direction.DUMMY;
		if(to.x - from.x == -1){
			switch(to.y - from.y){
			case -1: dir = Direction.UPLEFT;
					 break;
			case 0:	 dir = Direction.LEFT;
					 break;
			case 1:  dir = Direction.DOWNLEFT;
					 break;
			default: return false;
			}
		} else if (to.x - from.x == 0){
			switch(to.y - from.y){
			case -1: dir = Direction.UP;
					 break;
			case 0:	 return false;
			case 1:  dir = Direction.DOWN;
					 break;
			default: return false;
			}
		} else if (to.x - from.x == 1){
			switch(to.y - from.y){
			case -1: dir = Direction.UPRIGHT;
					 break;
			case 0:	 dir = Direction.RIGHT;
					 break;
			case 1:  dir = Direction.DOWNRIGHT;
					 break;
			default: return false;
			}
		}
		
		if(board[from.y][from.x] != color || board[to.y][to.x]!= Piece.EMPTY){
			return false;
		}
		
		int curX = 0;
		int curY = 0;
		int xInc = 0;
		int yInc = 0;
		if(previous_direction == dir){
			return false;
		}
		
		switch(dir){
		case UPLEFT:	previous_direction = Direction.UPLEFT;
						if(after){
							curX = from.x-2;
							curY = from.y-2;
							xInc = -1;
							yInc = -1;
						} else {
							curX = from.x+1;
							curY = from.y+1;
							xInc = 1;
							yInc = 1;
						}
						break;
		case UP:		previous_direction = Direction.UP;
						if(after){
							curX = from.x;
							curY = from.y-2;
							yInc = -1;
						} else {
							curX = from.x;
							curY = from.y+1;
							yInc = 1;
						}
						break;
		case UPRIGHT:	previous_direction = Direction.UPRIGHT;
						if(after){
							curX = from.x+2;
							curY = from.y-2;
							xInc = +1;
							yInc = -1;
						} else {
							curX = from.x-1;
							curY = from.y+1;
							xInc = -1;
							yInc = 1;
						}
						break;
		case LEFT:		previous_direction = Direction.LEFT;
						if(after) {
							curX = from.x-2;
							curY = from.y;
							xInc = -1;
						} else {
							curX = from.x+1;
							curY = from.y;
							xInc = 1;
						}
						break;
		case RIGHT:		previous_direction = Direction.RIGHT;
						if(after) {
							curX = from.x+2;
							curY = from.y;
							xInc = +1;
						} else {
							curX = from.x-1;
							curY = from.y;
							xInc = -1;
						}
						break;
		case DOWNLEFT:	previous_direction = Direction.DOWNLEFT;
						if(after) {
							curX = from.x-2;
							curY = from.y+2;
							xInc = -1;
							yInc = 1;
						} else {
							curX = from.x+1;
							curY = from.y-1;
							xInc = 1;
							yInc = -1;
						}
						break;
		case DOWN:		previous_direction = Direction.DOWN;
						if(after) {
							curX = from.x;
							curY = from.y+2;
							yInc = 1;
						} else {
							curX = from.x;
							curY = from.y-1;
							yInc = -1;
						}
						break;
		case DOWNRIGHT:	previous_direction = Direction.DOWNRIGHT;
						if(after){
							curX = from.x+2;
							curY = from.y+2;
							xInc = 1;
							yInc = 1;
						} else {
							curX = from.x-1;
							curY = from.y-1;
							xInc = -1;
							yInc = -1;
						}
						break;
		default:		break;
		}
		
		Piece color2 = Piece.PLAYER;
		if(color == Piece.PLAYER){
			color2 = Piece.OPPONENT;
		}
		if(curY >= 0 && curY < ROWS && curX >= 0 && curX < COLS){
			if(board[curY][curX] != color2 && detectMove(from, dir, color2)){
				//info.write("TEST");
				return false;
			}
		}
		
		board[from.y][from.x] = Piece.EMPTY;
		board[to.y][to.x] = color;
		
		while(curX >= 0 && curX < COLS && curY >= 0 && curY < ROWS){
			if(board[curY][curX] == color2){
				board[curY][curX] = Piece.EMPTY;
				curX += xInc;
				curY += yInc;
			} else {
				break;
			}
		}
		
		return true;
	}
	
	public int count(){
		int playerC = 0;
		int opponentC = 0;
		for(int i = 0; i < board.length; i++){
			for (int j = 0; j < board[0].length; j++){
				if(board[i][j] == GamePanel.Piece.PLAYER){
					playerC++;
				}
				if(board[i][j] == GamePanel.Piece.OPPONENT){
					opponentC++;
				}
			}
		}
		return playerC - opponentC;
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		//draw ze game here
		super.paintComponent(g);
		drawBoard(g);
		drawPieces(g);
		drawButtons(g);
		
		//validate();
		repaint();
	 }	
	
	public void drawBoard(Graphics g){
		int xstartp= 0;
		int ystartp= 0;
		
		int xwidth = getWidth();
		int yheight= getHeight();
		
		//draw background
		g.setColor(new Color(210,180,140));
		g.fillRect(xstartp, ystartp, xwidth, yheight);
		
		//draw vertical lines
		g.setColor(new Color(139,69,19));
		int y1= ystartp+ yheight/(ROWS+1), y2= ystartp+ yheight*ROWS/(ROWS+1);
		int x1,x2;
		
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (getHeight()+getWidth())/60));
		
		for(int i=1;i<(COLS+1);i++){
			x1= xstartp+ xwidth*i/(COLS+1); 
			g.drawLine(x1, y1 , x1, y2);
			g.drawLine(x1+1, y1 , x1+1, y2);
			g.drawLine(x1+2, y1 , x1+2, y2);
			
			g.drawString(Integer.toString(i), x1-getWidth()/70, y1/2+getHeight()/70);
		}
		
		// draw horizontal lines
		x1= xstartp+ xwidth/(COLS+1); 
		x2= xstartp+ xwidth*COLS/(COLS+1);
		
		for(int i=1;i<ROWS+1;i++){
			y1= ystartp+ yheight*i/(ROWS+1);
			
			g.drawLine(x1, y1 , x2, y1);
			g.drawLine(x1, y1+1 , x2, y1+1);
			g.drawLine(x1, y1+2 , x2, y1+2);
			char c= (char)(i+64);
			g.drawString(Character.toString(c),x1/2-getWidth()/50 , y1+getHeight()/50);
		}
		boolean flip=false; 
		
		// draw diagonal lines
		g.setColor(Color.GRAY);
		for(int i=ROWS/2+1; i<=ROWS-1;i++){
			y1= ystartp +i*yheight/(ROWS+1);
			y2= ystartp +(i+1)*yheight/(ROWS+1);
			for(int j=1; j<=COLS-1;j++){
					if(flip){
						if((j+(COLS/2)%2)%2==0){
							x1= xstartp+ j*xwidth/(COLS+1);
							x2= xstartp+ (j+1)*xwidth/(COLS+1);
						}
						else{
							x2= xstartp+ j*xwidth/(COLS+1);
							x1= xstartp+ (j+1)*xwidth/(COLS+1);
						}
					}
					else{
						if((j+(COLS/2)%2)%2==0){
							x2= xstartp+ j*xwidth/(COLS+1);
							x1= xstartp+ (j+1)*xwidth/(COLS+1);
						}
						else{
							x1= xstartp+ j*xwidth/(COLS+1);
							x2= xstartp+ (j+1)*xwidth/(COLS+1);
						}
					}
				g.drawLine(x1, y1 , x2, y2);
				g.drawLine(x1+1, y1 , x2+1, y2);
				g.drawLine(x1+2, y1 , x2+2, y2);
			}
			flip = !flip;
		}
		
		flip =true;
		for(int i=ROWS/2; i>=1;i--){
			y1= ystartp +i*yheight/(ROWS+1);
			y2= ystartp +(i+1)*yheight/(ROWS+1);
			for(int j=1; j<=COLS-1;j++){
					if(flip){
						if((j+(COLS/2)%2)%2==0){
							x1= xstartp+ j*xwidth/(COLS+1);
							x2= xstartp+ (j+1)*xwidth/(COLS+1);
						}
						else{
							x2= xstartp+ j*xwidth/(COLS+1);
							x1= xstartp+ (j+1)*xwidth/(COLS+1);
						}
					}
					else{
						if((j+(COLS/2)%2)%2==0){
							x2= xstartp+ j*xwidth/(COLS+1);
							x1= xstartp+ (j+1)*xwidth/(COLS+1);
						}
						else{
							x1= xstartp+ j*xwidth/(COLS+1);
							x2= xstartp+ (j+1)*xwidth/(COLS+1);
						}
					}
				g.drawLine(x1, y1 , x2, y2);
				g.drawLine(x1+1, y1 , x2+1, y2);
				g.drawLine(x1+2, y1 , x2+2, y2);
			}
			flip = !flip;
		}
	}

	public void drawPieces(Graphics g){
		
		int xwidth = getWidth();
		int yheight= getHeight();
		
		int piecesize=(getWidth()+getHeight())/50;
		int hilightsize=(getWidth()+getHeight())/40;
		int sacrificesize=(getWidth()+getHeight())/80;
		
		if(selected_piece != null){
			g.setColor(Color.YELLOW);
			g.fillOval((selected_piece.x+1)*(xwidth/(COLS+1))-(hilightsize)/2, (selected_piece.y+1)*yheight/(ROWS+1)-hilightsize/2, hilightsize, hilightsize);
		}
		if(choice1 != null){
			g.setColor(Color.CYAN);
			g.fillOval((choice1.x+1)*(xwidth/(COLS+1))-(hilightsize)/2, (choice1.y+1)*yheight/(ROWS+1)-hilightsize/2, hilightsize, hilightsize);
		}
		if(choice2 != null){
			g.setColor(Color.CYAN);
			g.fillOval((choice2.x+1)*(xwidth/(COLS+1))-(hilightsize)/2, (choice2.y+1)*yheight/(ROWS+1)-hilightsize/2, hilightsize, hilightsize);
		}
		
		for(int row = 1; row<board.length+1; row++){
			for(int col = 1; col<board[0].length+1; col++){
				if(board[row-1][col-1] == Piece.EMPTY) continue;
				if(board[row-1][col-1] == Piece.SACRIFICE){
					g.setColor(Color.YELLOW);
					g.fillOval(col*(xwidth/(COLS+1))-(hilightsize)/2, row*yheight/(ROWS+1)-hilightsize/2, hilightsize, hilightsize);
					g.setColor(Color.BLACK);
					g.fillOval(col*(xwidth/(COLS+1))-(piecesize)/2, row*yheight/(ROWS+1)-piecesize/2, piecesize, piecesize);
					g.setColor(Color.YELLOW);
					g.fillOval(col*(xwidth/(COLS+1))-(sacrificesize)/2, row*yheight/(ROWS+1)-sacrificesize/2, sacrificesize, sacrificesize);
				}
				if(board[row-1][col-1] == Piece.OPPONENT){
					g.setColor(opponentColor);
					g.fillOval(col*(xwidth/(COLS+1))-(piecesize)/2, row*yheight/(ROWS+1)-piecesize/2, piecesize, piecesize);
				}
				if(board[row-1][col-1] == Piece.PLAYER){
					g.setColor(playerColor);
					g.fillOval(col*(xwidth/(COLS+1))-(piecesize)/2, row*yheight/(ROWS+1)-piecesize/2, piecesize, piecesize);
				}
			}
		}
		
		int psize = getWidth()/24;
		if(selected_piece != null && !chain_piece && !overrideMode){
			g.setColor(Color.BLACK);
			g.drawString("SACRIFICE", xwidth - (6*psize), yheight-4);
		} else {
			g.setColor(Color.GRAY);
			g.drawString("SACRIFICE", xwidth - (6*psize), yheight-4);
		}
	}

	public void newGame(){//**********************************
		
		board = new Piece[ROWS][COLS];
		//fill top rows with OPPONENT pieces
		for(int i = 0; i<ROWS/2; i++){
			for(int j = 0; j<COLS; j++){
				board[i][j] = Piece.OPPONENT;
			}
		}
		//alternate colors in middle row, except middle space
		Piece prev = Piece.PLAYER, p= Piece.OPPONENT;
		for(int i=0;i <COLS;i++){
			if(i == COLS/2) board[ROWS/2][i]  = Piece.EMPTY;
			else {
				board[ROWS/2][i] = p;
				Piece tmp = prev;
				prev=p;
				p=tmp;
			}
			
		}
		//fill bottom rows with PLAYER pieces
		for(int i = ROWS/2+1; i<ROWS; i++){
			for(int j = 0; j<COLS; j++){
				board[i][j] = Piece.PLAYER;
			}
		}
		
		TurnCount = 0;
		
		selected_piece = null;
		choice1 = null;
		choice2 = null;
		sacrificeP = null;
		sacrificeO = null;
		chained_spots = new ArrayList<Point>();
		chain_piece = false;
		previous_direction = Direction.DUMMY;
		
		win = false;
		draw = false;
		
		if(TurnCount == 0 && info.getGraphics()!= null) info.write(printTurn());
		if(stopw.getGraphics()!=null) { stopw.timeReset(); }
		if(AImode == 2){
			AI computer = new AI(board, Piece.PLAYER);
			AIBoard AImoves = computer.getMove();
			
			if(AImoves.chained_spots.size() != 0){
				selected_piece = AImoves.chained_spots.get(0);
				animate();
			}
			
			for(int moving = 0; moving < AImoves.chained_spots.size()-1; moving++){
				if(moving > 0){
					if(TurnCount%2==WHITE){ 
						Player1move+=" + ";
					}
					else{
						Player2move+=" + ";
					}
				}
				//selected_piece = AImoves.chained_spots.get(moving+1);
				//animate();
				boolean after = true;
				boolean before = true;
				if(AImoves.moves.get(moving) == Type.WITHDRAW){
					after = false;
				} else if(AImoves.moves.get(moving) == Type.ADVANCE){
					before = false;
				} else {
					before = false;
					after = false;
				}
				
				interpretMove(AImoves.chained_spots.get(moving),AImoves.chained_spots.get(moving+1), after);
				printMove(AImoves.chained_spots.get(moving), AImoves.chained_spots.get(moving+1), after, before);
				selected_piece = AImoves.chained_spots.get(moving+1);
				animate();
			}
			
			selected_piece = null;
			paintComponent(this.getGraphics());
			countPieces();
		}
	}
	
	public void drawButtons(Graphics g){
		buttons = new Rectangle[ROWS][COLS];
		int xwidth = getWidth();
		int yheight= getHeight();
		
		int piecesize=getWidth()/24;
		
		for(int row = 1; row < buttons.length+1; row++){
			for(int col = 1; col < buttons[0].length+1; col++){
				g.setColor(new Color(0,0,0,0));
				g.drawRect(col*(xwidth/(COLS+1))-piecesize/2, row*(yheight/(ROWS+1))-piecesize/2,piecesize, piecesize);
				buttons[row-1][col-1] = new Rectangle(col*(xwidth/(COLS+1))-piecesize/2, row*(yheight/(ROWS+1))-piecesize/2, piecesize, piecesize);
			}
		}
		
		g.setColor(Color.BLACK);
		g.fillRect(xwidth - (2*piecesize), yheight - (piecesize)+4, piecesize*2, piecesize-4);
		g.setColor(Color.GREEN);
		g.fillRect(xwidth - (2*piecesize)+4, yheight - (piecesize)+8, piecesize*2-8, piecesize-12);		
		SacButton = new Rectangle(xwidth - (2*piecesize), yheight - (piecesize)+4, piecesize*2, piecesize+4);
	}

	public void setBoardSize(int numberOfRows, int numberofColumns){
		ROWS = numberOfRows;
		COLS = numberofColumns;
		if((ROWS + COLS)%4 == 0){
			Diag = 1;
		} else {
			Diag = 0;
		}
	}
	
	public int getNumberOfRows(){
		return ROWS;
	}
	
	public int getNumberofCols(){
		return COLS;
	}
	
	public void setPlayerName(String s){
		playerName = s;
		info.write("Name changed to: "+ s);
	}
	
	public void setPlayerColors(Color player, Color opponent){
		playerColor = player;
		opponentColor = opponent;
	}
	
	public Color getPlayerColor(){
		return playerColor;
	}
	
	public Color getOpponentColor(){
		return opponentColor;
	}
	
	public String printTurn(){
		if(TurnCount%2==WHITE) {
			if(playerName!=null) {return (playerName+"'s Turn");}
			else return("Player 1's Turn");
		}
		else return("Player 2's Turn");
	}
	
	public void setP1Humans(){
		P1AI=false;
		//AImode = 0;
	}
	
	public void setP1HumanAI(){
		P1AI=true;
		//AImode = 1;
	}
	
	public void setP2Humans(){
		P2AI=false;
		//AImode = 0;
	}
	
	public void setP2HumanAI(){
		P2AI=true;
		//AImode = 1;
	}
	
	public void setAIs(){
		AImode = 2;
	}
	
	public void blackFirst(){
		BLACK = 0;
		WHITE = 1;
	}
	
	public void whiteFirst(){
		BLACK = 1;
		WHITE = 0;
	}
	
	public void animate(){
		paintComponent(this.getGraphics());
		try{
			Thread.sleep(300);
		} catch(InterruptedException ex){
			Thread.currentThread().interrupt();
		}
	}
	
	public void Player2AImove(){
			AI computer = new AI(board, Piece.OPPONENT);
			AIBoard AImoves = computer.getMove();
			
			if(AImoves.chained_spots.size() != 0){
				selected_piece = AImoves.chained_spots.get(0);
				animate();

			}
			
			for(int moving = 0; moving < AImoves.chained_spots.size()-1; moving++){
				if(moving > 0){
					if(TurnCount%2==WHITE){ 
						Player1move+=" + ";
					}
					else{
						Player2move+=" + ";
					}
				}
				//selected_piece = AImoves.chained_spots.get(moving+1);
				//animate();
				boolean after = true;
				boolean before = true;
				if(AImoves.moves.get(moving) == Type.WITHDRAW){
					after = false;
				} else if(AImoves.moves.get(moving) == Type.ADVANCE){
					before = false;
				} else {
					before = false;
					after = false;
				}
				
				interpretMove(AImoves.chained_spots.get(moving),AImoves.chained_spots.get(moving+1), after);
				printMove(AImoves.chained_spots.get(moving), AImoves.chained_spots.get(moving+1), after, before);
				selected_piece = AImoves.chained_spots.get(moving+1);
				animate();
				
			}
			
			if(TurnCount%2==WHITE){
				Player1newmove=true;
				Player2move="";
			}
			else{
				Player2newmove=true;
				Player1move="";
			}
			countPieces();
			selected_piece = null;
			paintComponent(this.getGraphics());
			
		
	}
		
	public void Player1AImove(){
			AI computer = new AI(board, Piece.PLAYER);
			AIBoard AImoves = computer.getMove();
			
			if(AImoves.chained_spots.size() != 0){
				selected_piece = AImoves.chained_spots.get(0);
				animate();
			}
			
			for(int moving = 0; moving < AImoves.chained_spots.size()-1; moving++){
				if(moving > 0){
					if(TurnCount%2==WHITE){ 
						Player1move+=" + ";
					}
					else{
						Player2move+=" + ";
					}
				}
				//selected_piece = AImoves.chained_spots.get(moving+1);
				//animate();
				boolean after = true;
				boolean before = true;
				if(AImoves.moves.get(moving) == Type.WITHDRAW){
					after = false;
				} else if(AImoves.moves.get(moving) == Type.ADVANCE){
					before = false;
				} else {
					before = false;
					after = false;
				}
				interpretMove(AImoves.chained_spots.get(moving),AImoves.chained_spots.get(moving+1), after);
				printMove(AImoves.chained_spots.get(moving), AImoves.chained_spots.get(moving+1), after, before);
				selected_piece = AImoves.chained_spots.get(moving+1);
				animate();
			}
			
			
			if(TurnCount%2==WHITE){
				Player1newmove=true;
				Player2move="";
			}
			else{
				Player2newmove=true;
				Player1move="";
			}
			countPieces();
			selected_piece = null;
			paintComponent(this.getGraphics());
			
	}
	
	public void countPieces(){
		stopw.timeStop();
		previous_direction = Direction.DUMMY;
	
		PlayerPieceCount = OppPieceCount = EmptyPieceCount = 0;
		for(int row = 0; row<board.length; row++){
			for(int col = 0; col<board[0].length; col++){
				if(board[row][col]==Piece.OPPONENT) OppPieceCount++;
				else if(board[row][col]==Piece.PLAYER) PlayerPieceCount++;
				else EmptyPieceCount++;
			}
		}
		
		if(stopw.isTimeUp()){
			if(TurnCount%2 == WHITE) PlayerPieceCount = 0;
			else OppPieceCount = 0;
			win = true;
		}
		if(!stopw.isTimeUp()) stopw.timeReset();	
			
		
		if(TurnCount == 10*ROWS-1) draw = true;
		if(OppPieceCount == 0 || PlayerPieceCount == 0) win = true;
		if(!draw && !win) TurnCount++;
		
		if(!draw && !win){
			info.write(printTurn());
			
			//**************************************************************************************
			if(TurnCount%2 == WHITE && sacrificeP != null){
				board[sacrificeP.y][sacrificeP.x] = Piece.EMPTY;
				sacrificeP = null;
			}
			if(TurnCount%2 == BLACK && sacrificeO != null){
				board[sacrificeO.y][sacrificeO.x] = Piece.EMPTY;
				sacrificeO = null;
			}
			
			if(!servermode && P1AI && TurnCount%2 == WHITE){
				Player1AImove();
			}
			else if( !servermode &&P2AI && TurnCount%2 == BLACK){
				Player2AImove();
			} 
			
			//*************************************************************************
			stopw.timeStart();
		}
		else if(draw) { info.write("Draw"); stopw.timeStop(); }
		else if(OppPieceCount == 0) { info.write("Player 1 Wins!"); stopw.timeStop(); }
		else { info.write("Player 2 Wins!"); stopw.timeStop(); }
	}
	
	public boolean writeMove(OutputStream output){
		if(Player1newmove){
		
			byte[] buf = new byte[1024];
		    char[] charArray = Player1move.toCharArray();
		    for(int i = 0; i<charArray.length; i++){
		    	buf[i] = (byte)charArray[i];
		    }
		    buf[charArray.length] = (byte)' ';
		    try {
				output.write(buf, 0, buf.length);
			} catch (IOException e1) {
				System.err.println("server: unable to write to output stream");
				return false;
				//System.exit(1);
			}
		    return true;
		}
		else if(Player2newmove){
			
			byte[] buf = new byte[1024];
		    char[] charArray = Player2move.toCharArray();
		    for(int i = 0; i<charArray.length; i++){
		    	buf[i] = (byte)charArray[i];
		    }
		    buf[charArray.length] = (byte)' ';
		    try {
				output.write(buf, 0, buf.length);
			} catch (IOException e1) {
				System.err.println("server: unable to write to output stream");
				return false;
				//System.exit(1);
			}
		    return true;
		}
		
	    return false;
	}
	
	
}
