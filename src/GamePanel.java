import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//TODO: fix bug with selecting pieces

// GamePanel displays the current state of the board.
public class GamePanel extends JPanel{
	
	static final int ROWS = 5;
	static final int COLS = 9;
	
	String[] letters;
	
	int PlayerPieceCount, OppPieceCount, EmptyPieceCount;
	int TurnCount=0;
	
	public enum Piece {PLAYER, OPPONENT, EMPTY};
	private Color playerColor = Color.WHITE;
	private Color opponentColor = Color.BLACK;
	private String playerName = "Player 1";
	
	private Piece[][] board = new Piece[ROWS][COLS];
	private Rectangle[][] buttons = new Rectangle[ROWS][COLS];
	private Point selected_piece = null;
	
	public InfoPanel info;
	

	public GamePanel(InfoPanel i){
		setPreferredSize(new Dimension(600,400));
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
				
				Point p = e.getPoint();
				for(int row = 0; row < buttons.length; row++){
					for(int col = 0; col < buttons[0].length; col++){
						if(buttons[row][col].contains(p)){
							if(selected_piece == null && board[row][col]!=Piece.EMPTY){ //no piece selected
								selected_piece = new Point(col, row);
								info.write(letters[row]+Integer.toString(col+1)+" selected");
								break;
							}
							else if(selected_piece != null && board[row][col]==Piece.EMPTY){
								Piece color = board[selected_piece.y][selected_piece.x];
								info.write(letters[selected_piece.y]+Integer.toString(selected_piece.x+1)+" moved to "+letters[row]+Integer.toString(col+1));
								board[row][col] = color;
								board[selected_piece.y][selected_piece.x]= Piece.EMPTY;
								selected_piece = null;
							}
							else if(board[row][col]!=Piece.EMPTY) info.write("There is a piece There!");
							else info.write("This spot is empty!");
						}
					}
				}
			}
		});
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//draw ze game here
		super.paintComponent(g);
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
		
		int xoff = 35;
		
		String[] letters= new String[5];
		letters[0]= "A";
		letters[1]= "B";
		letters[2]= "C";
		letters[3]= "D";
		letters[4]= "E";
		
		for(int i=1;i<10;i++){
			x1= xstartp+ xwidth*i/10;// -xoff; 
			g.drawLine(x1, y1 , x1, y2);
			g.drawLine(x1+1, y1 , x1+1, y2);
			g.drawLine(x1+2, y1 , x1+2, y2);
			
			g.drawString(Integer.toString(i), x1, y1/2);
		}
		
		// draw horizontal lines
		x1= xstartp+ xwidth/10;// -xoff; 
		x2= xstartp+ xwidth*9/10;// -xoff;
		for(int i=1;i<10;i+=2){
			y1= ystartp+ yheight*i/10 ;
			g.drawLine(x1, y1 , x2, y1);
			g.drawLine(x1, y1+1 , x2, y1+1);
			g.drawLine(x1, y1+2 , x2, y1+2);
			
			g.drawString(letters[(int)(Math.ceil(i/2))],xwidth/10/2-10 , y1+5);
		}
		
		
		// draw diagonal lines
		y1= ystartp +yheight/10;
		for(int i=0;i<6;i++){
			if(i%2==0){
				x1= xstartp+ (i+1)*xwidth/10;// -xoff; 
				x2= xstartp+ (i+5)*xwidth/10;// -xoff;
			}
			else{
				x1= xstartp+ (i+4)*xwidth/10;// -xoff; 
				x2= xstartp+ (i)*xwidth/10;// -xoff;
			}
			
			g.drawLine(x1, y1 , x2, y2);
			g.drawLine(x1+1, y1 , x2+1, y2);
			g.drawLine(x1+2, y1 , x2+2, y2);
		}
		
		x1= xstartp+ xwidth*3/10;// -xoff; 
		x2= xstartp+ xwidth/10;// -xoff;
		
		g.drawLine(x1, y1 , x2, y1+ yheight*4/10);
		g.drawLine(x1+1, y1 , x2+1, y1+ yheight*4/10);
		g.drawLine(x1+2, y1 , x2+2, y1+ yheight*4/10);
		
		g.drawLine(x2, y1+ yheight*4/10 , x1, y2);
		g.drawLine(x2+1, y1+ yheight*4/10 , x1+1, y2);
		g.drawLine(x2+2, y1+ yheight*4/10 , x1+2, y2);
		
		x1= xstartp+ xwidth*7/10;// -xoff; 
		x2= xstartp+ xwidth*9/10;// -xoff;
		
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
	}
	
	
}
