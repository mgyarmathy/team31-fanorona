import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

//TODO: fix bug with selecting pieces
//TODO: prevent moving to same spot (bug with chained_spots)
//TODO: correct movement checking at columns 1 and COLS-2
//TODO: Figure out how to select between pieces to eliminate
//TODO: Allow for moving to spaces without eliminating pieces

// GamePanel displays the current state of the board.
public class GamePanel extends JPanel{
	
	static final int ROWS = 5;
	static final int COLS = 9;
	
	String[] letters;
	
	int PlayerPieceCount, OppPieceCount, EmptyPieceCount;
	int TurnCount;
	int TurnPrior;
	
	public enum Piece {PLAYER, OPPONENT, EMPTY};
	private enum Direction {NEUTRAL, UPLEFT, UP, UPRIGHT, LEFT, RIGHT, DOWNLEFT, DOWN, DOWNRIGHT, DUMMY};
	private Color playerColor = Color.WHITE;
	private Color opponentColor = Color.BLACK;
	private String playerName = "Player 1";
	
	private Piece[][] board = new Piece[ROWS][COLS];
	private Rectangle[][] buttons = new Rectangle[ROWS][COLS];
	private Point selected_piece = null;
	private ArrayList<Point> chained_spots = new ArrayList<Point>();
	private boolean chain_piece = false;
	private Direction previous_direction = Direction.DUMMY;
	
	public InfoPanel info;
	

	public GamePanel(InfoPanel i){
		setPreferredSize(new Dimension(800,500));
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setVisible(true);
		info = i;
		newGame();
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				String[] letters= new String[5];
				letters[0]= "A";
				letters[1]= "B";
				letters[2]= "C";
				letters[3]= "D";
				letters[4]= "E";
				
				if(TurnPrior!=TurnCount) printTurn();
				
				
				boolean emptyClick=true;
				
				Point p = e.getPoint();
				for(int row = 0; row < buttons.length; row++){
					for(int col = 0; col < buttons[0].length; col++){
						if(buttons[row][col].contains(p)){
							emptyClick=false;
							//select piece
							if(selected_piece == null && board[row][col]!=Piece.EMPTY){ //no piece selected
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
							else if(selected_piece != null && (board[row][col]==Piece.EMPTY || (selected_piece.x == col && selected_piece.y == row))){ 
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
										if(chained_spots.get(k) != null){
											if(chained_spots.get(k).y == row && chained_spots.get(k).x == col){
												repeat = true;
												break;
											}
										}
									}
								}
								
								if(repeat){
									info.write("Cannot move to the same place during a chain.");
									break;
								}
								//Move detection goes here
								Direction dir = Direction.NEUTRAL;
								
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
									info.write("Cannot move in same direction twice");
									break;
								}
								
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
								
								boolean innate = false;
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
													valid_move = false;
													break;
												} else if(before && after){
													//HANDLE CHOICE
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
								
								if(innate){
									selected_piece = null;
									break;
								}
								
								if(valid_move){
									info.write(letters[selected_piece.y]+Integer.toString(selected_piece.x+1)+" moved to "+letters[row]+Integer.toString(col+1));
									board[row][col] = color;
									board[selected_piece.y][selected_piece.x]= Piece.EMPTY;
									chained_spots.add(new Point(selected_piece.y, selected_piece.x));
							//WHY DOES THIS NOT WORK?
									//info.write(letters[chained_spots[marker].y]+Integer.toString(chained_spots[marker].x+1));
									//remove pieces in line
									while(cur_x < COLS && cur_x > -1 && cur_y < ROWS && cur_y > -1){
										if(board[cur_y][cur_x] != opposite){
											break;
										}
										board[cur_y][cur_x] = Piece.EMPTY;
										cur_y += y_inc;
										cur_x += x_inc;
									}
									selected_piece.x = col;
									selected_piece.y = row;
									
									//CHECK FOR OTHER MOVES
									boolean next_move = false;
									Point[] checkParams = new Point[24];
									checkParams = makeMoveArray(col, row, checkParams, dir);
									next_move = checkFor(opposite, checkParams);
									if(next_move){
										chain_piece = true;
										previous_direction = dir;
									} else {
										chain_piece = false;
										chained_spots.clear();
										previous_direction = Direction.DUMMY;
									}
								}
								// if move is valid and no more moves to make - countPieces
								if(chain_piece == false){
									countPieces();
									selected_piece = null;
								}
								break;
							}
							else if(board[row][col]!=Piece.EMPTY) { info.write("There is a piece There!"); break;} //Spot taken
							else { info.write("This spot is empty!");  break; } //Spot empty
						}
					}
				}
				if(emptyClick) {
					if(!chain_piece)
						selected_piece = null; 
				} // deselect piece
				TurnPrior = TurnCount;
			}
		});
	}
	
	public Point[] makeMoveArray(int col, int row, Point[] checkParams, Direction dir){
		if (col == 0){
			if (row == 0){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[3] = new Point(col,row+1);
					checkParams[4] = new Point(col,row+2);
					checkParams[5] = null;
				}
				//downright
				//(row+col)%2 == 0 checks if the sum of the x and y position is even.
				//Only pieces with an even sum can move diagonally
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					checkParams[6] = new Point(col+1,row+1);
					checkParams[7] = new Point(col+2,row+2);
					checkParams[8] = null;
				}
			} else if (row == ROWS-1){
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-2);
					checkParams[2] = null;
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					checkParams[3] = new Point(col+1,row-1);
					checkParams[4] = new Point(col+2,row-2);
					checkParams[5] = null;
				}
				//right
				if(dir != Direction.RIGHT){
					checkParams[6] = new Point(col+1,row);
					checkParams[7] = new Point(col+2,row);
					checkParams[8] = null;
				}
			} else  if (row == 1){
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-1); //dummy value
					checkParams[2] = new Point(col,row+1);
				}
				//right
				if(dir != Direction.RIGHT){
					checkParams[6] = new Point(col+1,row);
					checkParams[7] = new Point(col+2,row);
					checkParams[8] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[9] = new Point(col,row+1);
					checkParams[10] = new Point(col,row+2);
					checkParams[11] = new Point(col,row-1);
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					checkParams[12] = new Point(col+1,row+1);
					checkParams[13] = new Point(col+2,row+2);
					checkParams[14] = null;
				}
			} else if (row == ROWS - 2){
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-2);
					checkParams[2] = new Point(col,row+1);
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					checkParams[3] = new Point(col+1,row-1);
					checkParams[4] = new Point(col+2,row-2);
					checkParams[5] = null;
				}
				//right
				if(dir != Direction.RIGHT){
					checkParams[6] = new Point(col+1,row);
					checkParams[7] = new Point(col+2,row);
					checkParams[8] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[9] = new Point(col,row+1);
					checkParams[10] = new Point(col,row+1); //dummy value
					checkParams[11] = new Point(col,row-1);
				}
			} else {
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-2);
					checkParams[2] = new Point(col,row+1);
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					checkParams[3] = new Point(col+1,row-1);
					checkParams[4] = new Point(col+2,row-2);
					checkParams[5] = null;
				}
				//right
				if(dir != Direction.RIGHT){
					checkParams[6] = new Point(col+1,row);
					checkParams[7] = new Point(col+2,row);
					checkParams[8] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[9] = new Point(col,row+1);
					checkParams[10] = new Point(col,row+2);
					checkParams[11] = new Point(col,row-1);
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					checkParams[12] = new Point(col+1,row+1);
					checkParams[13] = new Point(col+2,row+2);
					checkParams[14] = null;
				}
			}
		} else if (col == COLS-1){
			if (row == 0){
				//left
				if(dir != Direction.LEFT){
					checkParams[0] = new Point(col-1,row);
					checkParams[1] = new Point(col-2,row);
					checkParams[2] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[3] = new Point(col,row+1);
					checkParams[4] = new Point(col,row+2);
					checkParams[5] = null;
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					checkParams[6] = new Point(col-1,row+1);
					checkParams[7] = new Point(col-2,row+2);
					checkParams[8] = null;
				}
			} else if (row == ROWS-1){
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-2);
					checkParams[2] = null;
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					checkParams[3] = new Point(col-1,row-1);
					checkParams[4] = new Point(col-2,row-2);
					checkParams[5] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[6] = new Point(col-1,row);
					checkParams[7] = new Point(col-2,row);
					checkParams[8] = null;
				}
			} else  if (row == 1){
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-1); //dummy value
					checkParams[2] = new Point(col,row+1);
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[6] = new Point(col-1,row);
					checkParams[7] = new Point(col-2,row);
					checkParams[8] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[9] = new Point(col,row+1);
					checkParams[10] = new Point(col,row+2);
					checkParams[11] = new Point(col,row-1);
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					checkParams[12] = new Point(col-1,row+1);
					checkParams[13] = new Point(col-2,row+2);
					checkParams[14] = null;
				}
			} else if (row == ROWS - 2){
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-2);
					checkParams[2] = new Point(col,row+1);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					checkParams[3] = new Point(col-1,row-1);
					checkParams[4] = new Point(col-2,row-2);
					checkParams[5] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[6] = new Point(col-1,row);
					checkParams[7] = new Point(col-2,row);
					checkParams[8] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[9] = new Point(col,row+1);
					checkParams[10] = new Point(col,row+1); //dummy value
					checkParams[11] = new Point(col,row-1);
				}
			} else {
				//up
				if(dir != Direction.UP){
					checkParams[0] = new Point(col,row-1);
					checkParams[1] = new Point(col,row-2);
					checkParams[2] = new Point(col,row+1);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					checkParams[3] = new Point(col-1,row-1);
					checkParams[4] = new Point(col-2,row-2);
					checkParams[5] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[6] = new Point(col-1,row);
					checkParams[7] = new Point(col-2,row);
					checkParams[8] = null;
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[9] = new Point(col,row+1);
					checkParams[10] = new Point(col,row+2);
					checkParams[11] = new Point(col,row-1);
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					checkParams[12] = new Point(col-1,row+1);
					checkParams[13] = new Point(col-2,row+2);
					checkParams[14] = null;
				}
			}
		} else if (col == 1){
			if (row == 0){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = new Point(col-1,row);
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[3] = new Point(col,row+1);
					checkParams[4] = new Point(col,row+2);
					checkParams[5] = null;
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					checkParams[6] = new Point(col+1,row+1);
					checkParams[7] = new Point(col+2,row+2);
					checkParams[8] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-1,row); //dummy value
					checkParams[11] = new Point(col+1,row);
				}
			} else if (row == ROWS-1){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = new Point(col-1,row);
				}
				//up
				if(dir != Direction.UP){
					checkParams[3] = new Point(col,row-1);
					checkParams[4] = new Point(col,row-2);
					checkParams[5] = null;
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					checkParams[6] = new Point(col+1,row-1);
					checkParams[7] = new Point(col+2,row-2);
					checkParams[8] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-1,row); //dummy value
					checkParams[11] = new Point(col+1,row);
				}
			} else {
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = new Point(col-1,row);
				}
				//up
				if(dir != Direction.UP){
					if(row > 1){
						checkParams[3] = new Point(col,row-1);
						checkParams[4] = new Point(col,row-2);
						checkParams[5] = new Point(col,row+1);
					} else {
						checkParams[3] = new Point(col,row-1);
						checkParams[4] = new Point(col,row-1); //dummy value, should pass through safely
						checkParams[5] = new Point(col,row+1);
					}
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					if(row > 1){
						checkParams[6] = new Point(col+1,row-1);
						checkParams[7] = new Point(col+2,row-2);
						checkParams[8] = new Point(col-1,row+1);
					} else {
						checkParams[6] = new Point(col+1,row-1);
						checkParams[7] = new Point(col+1,row-1); //dummy value
						checkParams[8] = new Point(col-1,row+1);
					}
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-1,row); //dummy value
					checkParams[11] = new Point(col+1,row);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					if(row > 1){
						checkParams[12] = new Point(col-1,row-1);
						checkParams[13] = new Point(col-1,row-1); //dummy value
						checkParams[14] = new Point(col+1,row+1);
					} else {
						checkParams[12] = new Point(col-1,row-1);
						checkParams[13] = new Point(col-1,row-1); //dummy value
						checkParams[14] = new Point(col+1,row+1);
					}
				}
				//down
				if(dir != Direction.DOWN){
					if(row < ROWS - 2){
						checkParams[15] = new Point(col,row+1);
						checkParams[16] = new Point(col,row+2);
						checkParams[17] = new Point(col,row-1);
					} else {
						checkParams[15] = new Point(col,row+1);
						checkParams[16] = new Point(col,row+1); //dummy value
						checkParams[17] = new Point(col,row-1);
					}
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					if(row < ROWS - 2){
						checkParams[18] = new Point(col+1,row+1);
						checkParams[19] = new Point(col+2,row+2);
						checkParams[20] = new Point(col-1,row-1);
					} else {
						checkParams[18] = new Point(col+1,row+1);
						checkParams[19] = new Point(col+1,row+1); //dummy value
						checkParams[20] = new Point(col-1,row-1);
					}
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					if (row < ROWS - 2){
						checkParams[21] = new Point(col-1,row+1);
						checkParams[22] = new Point(col-1,row+1); //dummy value
						checkParams[23] = new Point(col+1,row-1);
					} else {
						checkParams[21] = new Point(col-1,row+1);
						checkParams[22] = new Point(col-1,row+1); //dummy value
						checkParams[23] = new Point(col+1,row-1);
					}
				}
			}
		} else  if (col == COLS - 2){
			if (row == 0){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+1,row); //dummy value
					checkParams[2] = new Point(col-1,row);
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[3] = new Point(col,row+1);
					checkParams[4] = new Point(col,row+2);
					checkParams[5] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-2,row);
					checkParams[11] = new Point(col+1,row);
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					checkParams[12] = new Point(col-1,row+1);
					checkParams[13] = new Point(col-2,row+2);
					checkParams[14] = null;
				}
			} else if (row == ROWS-1){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+1,row); //dummy value
					checkParams[2] = new Point(col-1,row);
				}
				//up
				if(dir != Direction.UP){
					checkParams[3] = new Point(col,row-1);
					checkParams[4] = new Point(col,row-2);
					checkParams[5] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-2,row);
					checkParams[11] = new Point(col+1,row);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					checkParams[12] = new Point(col-1,row-1);
					checkParams[13] = new Point(col-2,row-2);
					checkParams[14] = null;
				}
			} else {
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+1,row); //dummy value
					checkParams[2] = new Point(col-1,row);
				}
				//up
				if(dir != Direction.UP){
					if(row > 1){
						checkParams[3] = new Point(col,row-1);
						checkParams[4] = new Point(col,row-2);
						checkParams[5] = new Point(col,row+1);
					} else {
						checkParams[3] = new Point(col,row-1);
						checkParams[4] = new Point(col,row-1); //dummy value, should pass through safely
						checkParams[5] = new Point(col,row+1);
					}
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					if(row > 1){
						checkParams[6] = new Point(col+1,row-1);
						checkParams[7] = new Point(col+1,row-1); //dummy value
						checkParams[8] = new Point(col-1,row+1);
					} else {
						checkParams[6] = new Point(col+1,row-1);
						checkParams[7] = new Point(col+1,row-1); //dummy value
						checkParams[8] = new Point(col-1,row+1);
					}
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-2,row);
					checkParams[11] = new Point(col+1,row);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					if(row > 1){
						checkParams[12] = new Point(col-1,row-1);
						checkParams[13] = new Point(col-2,row-2);
						checkParams[14] = new Point(col+1,row+1);
					} else {
						checkParams[12] = new Point(col-1,row-1);
						checkParams[13] = new Point(col-1,row-1); //dummy value
						checkParams[14] = new Point(col+1,row+1);
					}
				}
				//down
				if(dir != Direction.DOWN){
					if(row < ROWS - 2){
						checkParams[15] = new Point(col,row+1);
						checkParams[16] = new Point(col,row+2);
						checkParams[17] = new Point(col,row-1);
					} else {
						checkParams[15] = new Point(col,row+1);
						checkParams[16] = new Point(col,row+1); //dummy value
						checkParams[17] = new Point(col,row-1);
					}
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					if(row < ROWS - 2){
						checkParams[18] = new Point(col+1,row+1);
						checkParams[19] = new Point(col+1,row+1); //dummy value
						checkParams[20] = new Point(col-1,row-1);
					} else {
						checkParams[18] = new Point(col+1,row+1);
						checkParams[19] = new Point(col+1,row+1); //dummy value
						checkParams[20] = new Point(col-1,row-1);
					}
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					if (row < ROWS - 2){
						checkParams[21] = new Point(col-1,row+1);
						checkParams[22] = new Point(col-2,row+2);
						checkParams[23] = new Point(col+1,row-1);
					} else {
						checkParams[21] = new Point(col-1,row+1);
						checkParams[22] = new Point(col-1,row+1); //dummy value
						checkParams[23] = new Point(col+1,row-1);
					}
				}
			}
		} else {
			if (row == 0){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = new Point(col-1,row);
				}
				//down
				if(dir != Direction.DOWN){
					checkParams[3] = new Point(col,row+1);
					checkParams[4] = new Point(col,row+2);
					checkParams[5] = null;
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					checkParams[6] = new Point(col+1,row+1);
					checkParams[7] = new Point(col+2,row+2);
					checkParams[8] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-2,row);
					checkParams[11] = new Point(col+1,row);
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					checkParams[12] = new Point(col-1,row+1);
					checkParams[13] = new Point(col-2,row+2);
					checkParams[14] = null;
				}
			} else if (row == ROWS-1){
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = new Point(col-1,row);
				}
				//up
				if(dir != Direction.UP){
					checkParams[3] = new Point(col,row-1);
					checkParams[4] = new Point(col,row-2);
					checkParams[5] = null;
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					checkParams[6] = new Point(col+1,row-1);
					checkParams[7] = new Point(col+2,row-2);
					checkParams[8] = null;
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-2,row);
					checkParams[11] = new Point(col+1,row);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					checkParams[12] = new Point(col-1,row-1);
					checkParams[13] = new Point(col-2,row-2);
					checkParams[14] = null;
				}
			} else {
				//right
				if(dir != Direction.RIGHT){
					checkParams[0] = new Point(col+1,row);
					checkParams[1] = new Point(col+2,row);
					checkParams[2] = new Point(col-1,row);
				}
				//up
				if(dir != Direction.UP){
					if(row > 1){
						checkParams[3] = new Point(col,row-1);
						checkParams[4] = new Point(col,row-2);
						checkParams[5] = new Point(col,row+1);
					} else {
						checkParams[3] = new Point(col,row-1);
						checkParams[4] = new Point(col,row-1); //dummy value, should pass through safely
						checkParams[5] = new Point(col,row+1);
					}
				}
				//upright
				if(dir != Direction.UPRIGHT && (row+col)%2 == 0){
					if(row > 1){
						checkParams[6] = new Point(col+1,row-1);
						checkParams[7] = new Point(col+2,row-2);
						checkParams[8] = new Point(col-1,row+1);
					} else {
						checkParams[6] = new Point(col+1,row-1);
						checkParams[7] = new Point(col+1,row-1); //dummy value
						checkParams[8] = new Point(col-1,row+1);
					}
				}
				//left
				if(dir != Direction.LEFT){
					checkParams[9] = new Point(col-1,row);
					checkParams[10] = new Point(col-2,row);
					checkParams[11] = new Point(col+1,row);
				}
				//upleft
				if(dir != Direction.UPLEFT && (row+col)%2 == 0){
					if(row > 1){
						checkParams[12] = new Point(col-1,row-1);
						checkParams[13] = new Point(col-2,row-2);
						checkParams[14] = new Point(col+1,row+1);
					} else {
						checkParams[12] = new Point(col-1,row-1);
						checkParams[13] = new Point(col-1,row-1); //dummy value
						checkParams[14] = new Point(col+1,row+1);
					}
				}
				//down
				if(dir != Direction.DOWN){
					if(row < ROWS - 2){
						checkParams[15] = new Point(col,row+1);
						checkParams[16] = new Point(col,row+2);
						checkParams[17] = new Point(col,row-1);
					} else {
						checkParams[15] = new Point(col,row+1);
						checkParams[16] = new Point(col,row+1); //dummy value
						checkParams[17] = new Point(col,row-1);
					}
				}
				//downright
				if(dir != Direction.DOWNRIGHT && (row+col)%2 == 0){
					if(row < ROWS - 2){
						checkParams[18] = new Point(col+1,row+1);
						checkParams[19] = new Point(col+2,row+2);
						checkParams[20] = new Point(col-1,row-1);
					} else {
						checkParams[18] = new Point(col+1,row+1);
						checkParams[19] = new Point(col+1,row+1); //dummy value
						checkParams[20] = new Point(col-1,row-1);
					}
				}
				//downleft
				if(dir != Direction.DOWNLEFT && (row+col)%2 == 0){
					if (row < ROWS - 2){
						checkParams[21] = new Point(col-1,row+1);
						checkParams[22] = new Point(col-2,row+2);
						checkParams[23] = new Point(col+1,row-1);
					} else {
						checkParams[21] = new Point(col-1,row+1);
						checkParams[22] = new Point(col-1,row+1); //dummy value
						checkParams[23] = new Point(col+1,row-1);
					}
				}
			}
		}
		return checkParams;
	}
	
	public boolean checkFor(Piece color, Point[] points) {
		//Array needs points in sets of 3: an space that might be moved to,
		//the space after that, and the space before the starting point.
		//Checks if the move will allow player to take out an enemy piece.
		for(int i = 0;i < points.length; i = i+3){
			if (points[i] != null){
				boolean newspot = true;
				//check if the spot has been used during a chain move.
				for(int j = 0; j < chained_spots.size(); j++){
					if(chained_spots.get(j) != null){
						if(chained_spots.get(j).y == points[i].y && chained_spots.get(j).x == points[i].x){
							newspot = false;
						}
					}
				}
				if(newspot){
					//Check if moving eliminates a forward piece
					if (board[points[i].y][points[i].x] == Piece.EMPTY){
						if (board[points[i+1].y][points[i+1].x] == color){
							return true;
						}
						if(points[i+2] != null){
							//Check if moving eliminates a preceding piece
							if (board[points[i+2].y][points[i+2].x] == color){
								
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean checkMoves(Point start){
		return true;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//draw ze game here
		//super.paintComponent(g);
		drawBoard(g);
		drawPieces(g);
		drawButtons(g);
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
		int y1= ystartp+ yheight/10, y2= ystartp+ yheight*9/10;
		int x1,x2;
		
		
		String[] letters= new String[5];
		letters[0]= "A";
		letters[1]= "B";
		letters[2]= "C";
		letters[3]= "D";
		letters[4]= "E";
		
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (getHeight()+getWidth())/50));
		
		for(int i=1;i<10;i++){
			x1= xstartp+ xwidth*i/10; 
			g.drawLine(x1, y1 , x1, y2);
			g.drawLine(x1+1, y1 , x1+1, y2);
			g.drawLine(x1+2, y1 , x1+2, y2);
			
			g.drawString(Integer.toString(i), x1-getWidth()/70, y1/2);
		}
		
		// draw horizontal lines
		x1= xstartp+ xwidth/10; 
		x2= xstartp+ xwidth*9/10;
		for(int i=1;i<10;i+=2){
			y1= ystartp+ yheight*i/10 ;
			g.drawLine(x1, y1 , x2, y1);
			g.drawLine(x1, y1+1 , x2, y1+1);
			g.drawLine(x1, y1+2 , x2, y1+2);
			
			g.drawString(letters[(int)(Math.ceil(i/2))],xwidth/10/2-getWidth()/40 , y1+getHeight()/50);
		}
		
		
		// draw diagonal lines
		g.setColor(Color.GRAY);
		y1= ystartp +yheight/10;
		for(int i=0;i<6;i++){
			if(i%2==0){
				x1= xstartp+ (i+1)*xwidth/10;
				x2= xstartp+ (i+5)*xwidth/10;
			}
			else{
				x1= xstartp+ (i+4)*xwidth/10;
				x2= xstartp+ (i)*xwidth/10;
			}
			
			g.drawLine(x1, y1 , x2, y2);
			g.drawLine(x1+1, y1 , x2+1, y2);
			g.drawLine(x1+2, y1 , x2+2, y2);
		}
		
		x1= xstartp+ xwidth*3/10;
		x2= xstartp+ xwidth/10;
		
		g.drawLine(x1, y1 , x2, y1+ yheight*4/10);
		g.drawLine(x1+1, y1 , x2+1, y1+ yheight*4/10);
		g.drawLine(x1+2, y1 , x2+2, y1+ yheight*4/10);
		
		g.drawLine(x2, y1+ yheight*4/10 , x1, y2);
		g.drawLine(x2+1, y1+ yheight*4/10 , x1+1, y2);
		g.drawLine(x2+2, y1+ yheight*4/10 , x1+2, y2);
		
		x1= xstartp+ xwidth*7/10; 
		x2= xstartp+ xwidth*9/10;
		
		g.drawLine(x1, y1 , x2, y1+ yheight*4/10);
		g.drawLine(x1+1, y1 , x2+1, y1+ yheight*4/10);
		g.drawLine(x1+2, y1 , x2+2, y1+ yheight*4/10);
		
		g.drawLine(x2, y1+ yheight*4/10 , x1, y2);
		g.drawLine(x2+1, y1+ yheight*4/10 , x1+1, y2);
		g.drawLine(x2+2, y1+ yheight*4/10 , x1+2, y2);
	}

	public void drawPieces(Graphics g){
		
		int xwidth = getWidth();
		int yheight= getHeight();
		
		int xoff = getWidth()/29;
		int yoff = getHeight()/15;
		
		int piecesize=getWidth()/24;
		int hilightsize=getWidth()/19;
		
		if(selected_piece != null){
			g.setColor(Color.YELLOW);
			g.fillOval((selected_piece.x+1)*(xwidth/(COLS+1))-hilightsize/2, (2*(selected_piece.y+1)-1)*(yheight/(2*ROWS))-hilightsize/2, hilightsize, hilightsize);
		}
		
		for(int row = 1; row<board.length+1; row++){
			for(int col = 1; col<board[0].length+1; col++){
				if(board[row-1][col-1] == Piece.EMPTY) continue;
				if(board[row-1][col-1] == Piece.OPPONENT){
					g.setColor(opponentColor);
					g.fillOval(col*(xwidth/(COLS+1))-piecesize/2, (2*row-1)*(yheight/(2*ROWS))-piecesize/2, piecesize, piecesize);
				}
				if(board[row-1][col-1] == Piece.PLAYER){
					g.setColor(playerColor);
					g.fillOval(col*(xwidth/(COLS+1))-piecesize/2, (2*row-1)*(yheight/(2*ROWS))-piecesize/2, piecesize, piecesize);
				}
			}
		}
	}

	public void newGame(){
		board = new Piece[ROWS][COLS];
		//fill top two rows with OPPONENT pieces
		for(int i = 0; i<2; i++){
			for(int j = 0; j<9; j++){
				board[i][j] = Piece.OPPONENT;
			}
		}
		//alternate colors in middle row, except middle space
		board[2][0] = Piece.OPPONENT;
		board[2][1] = Piece.PLAYER;
		board[2][2] = Piece.OPPONENT;
		board[2][3] = Piece.PLAYER;
		board[2][4] = Piece.EMPTY;
		board[2][5] = Piece.OPPONENT;
		board[2][6] = Piece.PLAYER;
		board[2][7] = Piece.OPPONENT;
		board[2][8] = Piece.PLAYER;
		//fill bottom two rows with PLAYER pieces
		for(int i = 3; i<5; i++){
			for(int j = 0; j<9; j++){
				board[i][j] = Piece.PLAYER;
			}
		}
		
		TurnCount = TurnPrior = 0;
		selected_piece = null;
	}
	

	public void drawButtons(Graphics g){
		buttons = new Rectangle[ROWS][COLS];
		int xwidth = getWidth();
		int yheight= getHeight();
		
		int xoff = getWidth()/29;
		int yoff = getHeight()/15;
		
		int piecesize=getWidth()/24;
		
		for(int row = 1; row < buttons.length+1; row++){
			for(int col = 1; col < buttons[0].length+1; col++){
				g.setColor(new Color(0,0,0,0));
				g.drawRect(col*(xwidth/(COLS+1))-piecesize/2, (2*row-1)*(yheight/(2*ROWS))-piecesize/2,piecesize, piecesize);
				buttons[row-1][col-1] = new Rectangle(col*(xwidth/(COLS+1))-piecesize/2, (2*row-1)*(yheight/(2*ROWS))-piecesize/2, piecesize, piecesize);
			}
		}
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
		PlayerPieceCount = OppPieceCount = EmptyPieceCount = 0;
		for(int row = 0; row<board.length; row++){
			for(int col = 0; col<board[0].length; col++){
				if(board[row][col]==Piece.OPPONENT) OppPieceCount++;
				else if(board[row][col]==Piece.PLAYER) PlayerPieceCount++;
				else EmptyPieceCount++;
			}
		}
		TurnCount++;
		if(TurnCount == 50); // Draw
		if(OppPieceCount == 0 || PlayerPieceCount == 0); //  Win/Lose
		info.write(printTurn());
	}
	
	
}
