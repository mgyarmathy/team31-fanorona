import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;


// GamePanel displays the current state of the board.
public class GamePanel extends JPanel{
	
	static int ROWS = 5;
	static int COLS = 9;
	
	
	
	int PlayerPieceCount, OppPieceCount, EmptyPieceCount;
	int TurnCount;
	
	boolean win,draw;
	
	public enum Piece {PLAYER, OPPONENT, EMPTY, SACRIFICE};
	private enum Direction {NEUTRAL, UPLEFT, UP, UPRIGHT, LEFT, RIGHT, DOWNLEFT, DOWN, DOWNRIGHT, DUMMY};
	private Color playerColor = Color.WHITE;
	private Color opponentColor = Color.BLACK;
	private String playerName = "Player 1";
	
	private Piece[][] board = new Piece[ROWS][COLS];
	private Rectangle[][] buttons = new Rectangle[ROWS][COLS];
	private Rectangle SacButton = new Rectangle();
	private Point selected_piece = null;
	private Point choice1 = null;
	private Point choice2 = null;
	private Point sacrificeP = null;
	private Point sacrificeO = null;
	private ArrayList<Point> chained_spots = new ArrayList<Point>();
	private boolean chain_piece = false;
	private Direction previous_direction = Direction.DUMMY;
	private boolean overrideMode = false;
	private Direction overrideDir = Direction.DUMMY;
	
	public InfoPanel info;
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
								if(TurnCount%2==0 && color != Piece.PLAYER) {
									info.write("It's "+printTurn());
									if(!chain_piece)
										selected_piece =null; 
								}
								if(TurnCount%2==1 && color != Piece.OPPONENT) {
									info.write("It's "+printTurn());
									if(!chain_piece)
										selected_piece =null; 
								}
								//info.write(letters[row]+Integer.toString(col+1)+" selected");
								break;
							}
							//move selected piece
							else if((selected_piece != null && (board[row][col]==Piece.EMPTY || (selected_piece.x == col && selected_piece.y == row)))
										|| overrideMode){ 
								
								boolean valid_move = true;
								boolean before = false;
								boolean after = false;
								int cur_x = col;
								int cur_y = row;
								int x_inc = 0;
								int y_inc = 0;
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
										break;
									}
									//Check if the piece cannot move diagonally
									if((row + col)%2 == 1){
										int x_dif = Math.abs(selected_piece.x - col);
										int y_dif = Math.abs(selected_piece.y - row);
										if(x_dif != 0 && y_dif != 0){
											info.write("This piece cannot move diagonally.");
											if(!chain_piece){
												selected_piece = null;
											}
											break;
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
										break;
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
										break;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x + 1, selected_piece.y + 1);
														choice2 = new Point(selected_piece.x - 2, selected_piece.y - 2);
													} else if(before){
														cur_x = selected_piece.x + 1;
														cur_y = selected_piece.y + 1;
														x_inc++;
														y_inc++;
													} else {
														cur_x = selected_piece.x - 2;
														cur_y = selected_piece.y - 2;
														x_inc--;
														y_inc--;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x, selected_piece.y + 1);
														choice2 = new Point(selected_piece.x, selected_piece.y - 2);
													} else if(before){
														cur_x = selected_piece.x;
														cur_y = selected_piece.y + 1;
														y_inc++;
													} else {
														cur_x = selected_piece.x;
														cur_y = selected_piece.y - 2;
														y_inc--;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x - 1, selected_piece.y + 1);
														choice2 = new Point(selected_piece.x + 2, selected_piece.y - 2);
													} else if(before){
														cur_x = selected_piece.x - 1;
														cur_y = selected_piece.y + 1;
														x_inc--;
														y_inc++;
													} else {
														cur_x = selected_piece.x + 2;
														cur_y = selected_piece.y - 2;
														x_inc++;
														y_inc--;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x + 1, selected_piece.y);
														choice2 = new Point(selected_piece.x - 2, selected_piece.y);
													} else if(before){
														cur_x = selected_piece.x + 1;
														cur_y = selected_piece.y;
														x_inc++;
													} else {
														cur_x = selected_piece.x - 2;
														cur_y = selected_piece.y;
														x_inc--;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x - 1, selected_piece.y);
														choice2 = new Point(selected_piece.x + 2, selected_piece.y);
													} else if(before){
														cur_x = selected_piece.x - 1;
														cur_y = selected_piece.y;
														x_inc--;
													} else {
														cur_x = selected_piece.x + 2;
														cur_y = selected_piece.y;
														x_inc++;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x + 1, selected_piece.y - 1);
														choice2 = new Point(selected_piece.x - 2, selected_piece.y + 2);
													} else if(before){
														cur_x = selected_piece.x + 1;
														cur_y = selected_piece.y - 1;
														x_inc++;
														y_inc--;
													} else {
														cur_x = selected_piece.x - 2;
														cur_y = selected_piece.y + 2;
														x_inc--;
														y_inc++;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x, selected_piece.y - 1);
														choice2 = new Point(selected_piece.x, selected_piece.y + 2);
													} else if(before){
														cur_x = selected_piece.x;
														cur_y = selected_piece.y - 1;
														y_inc--;
													} else {
														cur_x = selected_piece.x;
														cur_y = selected_piece.y + 2;
														y_inc++;
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
															} else {
																cur_x = selected_piece.x;
																cur_y = selected_piece.y;
															}
														}
														break;
													} else if(before && after){
														overrideMode = true;
														overrideDir = dir;
														choice1 = new Point(selected_piece.x - 1, selected_piece.y - 1);
														choice2 = new Point(selected_piece.x + 2, selected_piece.y + 2);
													} else if(before){
														cur_x = selected_piece.x - 1;
														cur_y = selected_piece.y - 1;
														x_inc--;
														y_inc--;
													} else {
														cur_x = selected_piece.x + 2;
														cur_y = selected_piece.y + 2;
														x_inc++;
														y_inc++;
													}
													break;
									default:		break;
									}
									
									if(overrideMode){
										info.write("Selecte the piece(s) you want to eliminate");
										break;
									}
									if(innate){
										if(!chain_piece){
											selected_piece = null;
										}
										if(blank){
											info.write("Must take opponent piece off board.");
										}
										break;
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
										break;
									} else {
										info.write("Must select one of two pieces.");
										break;
									}
									overrideMode = false;
									dir = overrideDir;
									switch(dir){
									case UPLEFT:		row = selected_piece.y - 1;
														col = selected_piece.x - 1;
														if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = 1;
														y_inc = 1;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = -1;
														y_inc = -1;
													}
													break;
									case UP:		row = selected_piece.y - 1;
													col = selected_piece.x;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = 0;
														y_inc = 1;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = 0;
														y_inc = -1;
													}
													break;
									case UPRIGHT:	row = selected_piece.y - 1;
													col = selected_piece.x + 1;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = -1;
														y_inc = 1;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = 1;
														y_inc = -1;
													}
													break;				
									case LEFT:		row = selected_piece.y;
													col = selected_piece.x - 1;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = 1;
														y_inc = 0;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = -1;
														y_inc = 0;
													}
													break;
									case RIGHT:		row = selected_piece.y;
													col = selected_piece.x + 1;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = -1;
														y_inc = 0;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = 1;
														y_inc = 0;
													}
													break;
									case DOWNLEFT:	row = selected_piece.y + 1;
													col = selected_piece.x - 1;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = 1;
														y_inc = -1;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = -1;
														y_inc = 1;
													}
													break;				
									case DOWN:		row = selected_piece.y + 1;
													col = selected_piece.x;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = 0;
														y_inc = -1;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = 0;
														y_inc = 1;
													}
													break;				
									case DOWNRIGHT:	row = selected_piece.y + 1;
													col = selected_piece.x + 1;
													if(before){
														cur_x = choice1.x;
														cur_y = choice1.y;
														x_inc = -1;
														y_inc = -1;
													} else {
														cur_x = choice2.x;
														cur_y = choice2.y;
														x_inc = 1;
														y_inc = 1;
													}
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
									board[row][col] = color;
									board[selected_piece.y][selected_piece.x]= Piece.EMPTY;
									chained_spots.add(new Point(selected_piece.x, selected_piece.y));
									
									boolean thingsEliminated = false;
									while(cur_x < COLS && cur_x > -1 && cur_y < ROWS && cur_y > -1){
										if(board[cur_y][cur_x] != opposite){
											break;
										}
										thingsEliminated = true;
										board[cur_y][cur_x] = Piece.EMPTY;
										cur_y += y_inc;
										cur_x += x_inc;
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
										countPieces();
										selected_piece = null;
									}
								}
								break;
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
						if(TurnCount%2 == 0){
							sacrificeP = new Point(selected_piece.x,selected_piece.y);
						} else {
							sacrificeO = new Point(selected_piece.x,selected_piece.y);
						}
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

		if(board[start.y][start.x] == Piece.EMPTY || board[start.y][start.x] == color){
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
		
		if ((start.y+start.x)%2 == 0){
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
		if(TurnCount%2==0) {
			if(playerName!=null) {return (playerName+"'s Turn");}
			else return("Player 1's Turn");
		}
		else return("Player 2's Turn");
	}
	
	public void countPieces(){
		stopw.timeStop();
	
		PlayerPieceCount = OppPieceCount = EmptyPieceCount = 0;
		for(int row = 0; row<board.length; row++){
			for(int col = 0; col<board[0].length; col++){
				if(board[row][col]==Piece.OPPONENT) OppPieceCount++;
				else if(board[row][col]==Piece.PLAYER) PlayerPieceCount++;
				else EmptyPieceCount++;
			}
		}
		
		if(stopw.isTimeUp()){
			if(TurnCount%2 == 0) PlayerPieceCount = 0;
			else OppPieceCount = 0;
			win = true;
		}
		if(!stopw.isTimeUp()) stopw.timeReset();	
			
		
		if(TurnCount == 10*ROWS-1) draw = true;
		if(OppPieceCount == 0 || PlayerPieceCount == 0) win = true;
		if(!draw && !win) TurnCount++;
		
		if(!draw && !win){
			info.write(printTurn());
			if(TurnCount%2 == 0 && sacrificeP != null){
				board[sacrificeP.y][sacrificeP.x] = Piece.EMPTY;
				sacrificeP = null;
			}
			if(TurnCount%2 == 1 && sacrificeO != null){
				board[sacrificeO.y][sacrificeO.x] = Piece.EMPTY;
				sacrificeO = null;
			}
			stopw.timeStart();
		}
		else if(draw) { info.write("Draw"); stopw.timeStop(); }
		else if(OppPieceCount == 0) { info.write("Player 1 Wins!"); stopw.timeStop(); }
		else { info.write("Player 2 Wins!"); stopw.timeStop(); }
	}
	
	
}
