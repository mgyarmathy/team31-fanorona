import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//TODO: fix bug with selecting pieces

// GamePanel displays the current state of the board.
public class GamePanel extends JPanel{
	
	static final int ROWS = 5;
	static final int COLS = 9;
	
	public enum Piece {PLAYER, OPPONENT, EMPTY};
	private Color player_color = Color.WHITE;
	private Color opponent_color = Color.BLACK;
	
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
				Point p = e.getPoint();
				for(int row = 0; row < buttons.length; row++){
					for(int col = 0; col < buttons[0].length; col++){
						if(buttons[row][col].contains(p)){
							if(selected_piece == null){ //no piece selected
								selected_piece = new Point(col, row);
								info.write("piece selected");
								break;
							}
							else{
								Piece color = board[selected_piece.y][selected_piece.x];
								board[row][col] = color;
								selected_piece = null;
							}
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
			x1= xstartp+ xwidth*i/9 -xoff; 
			g.drawLine(x1, y1 , x1, y2);
			g.drawLine(x1+1, y1 , x1+1, y2);
			g.drawLine(x1+2, y1 , x1+2, y2);
			
			g.drawString(Integer.toString(i), x1, y1 - 15);
		}
		
		// draw horizontal lines
		x1= xstartp+ xwidth/9 -xoff; 
		x2= xstartp+ xwidth -xoff;
		for(int i=1;i<10;i+=2){
			y1= ystartp+ yheight*i/10 ;
			g.drawLine(x1, y1 , x2, y1);
			g.drawLine(x1, y1+1 , x2, y1+1);
			g.drawLine(x1, y1+2 , x2, y1+2);
			
			g.drawString(letters[(int)(Math.ceil(i/2))], 10, y1+5);
		}
		
		
		// draw diagonal lines
		y1= ystartp +yheight/10;
		for(int i=0;i<6;i++){
			if(i%2==0){
				x1= xstartp+ (i+1)*xwidth/9 -xoff; 
				x2= xstartp+ (i+5)*xwidth/9 -xoff;
			}
			else{
				x1= xstartp+ (i+4)*xwidth/9 -xoff; 
				x2= xstartp+ (i)*xwidth/9 -xoff;
			}
			
			g.drawLine(x1, y1 , x2, y2);
			g.drawLine(x1+1, y1 , x2+1, y2);
			g.drawLine(x1+2, y1 , x2+2, y2);
		}
		
		x1= xstartp+ xwidth*3/9 -xoff; 
		x2= xstartp+ xwidth/9 -xoff;
		
		g.drawLine(x1, y1 , x2, y1+ yheight*4/10);
		g.drawLine(x1+1, y1 , x2+1, y1+ yheight*4/10);
		g.drawLine(x1+2, y1 , x2+2, y1+ yheight*4/10);
		
		g.drawLine(x2, y1+ yheight*4/10 , x1, y2);
		g.drawLine(x2+1, y1+ yheight*4/10 , x1+1, y2);
		g.drawLine(x2+2, y1+ yheight*4/10 , x1+2, y2);
		
		x1= xstartp+ xwidth*7/9 -xoff; 
		x2= xstartp+ xwidth -xoff;
		
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
		
		if(selected_piece != null){
			g.setColor(Color.YELLOW);
			//suspected line causing draw bug:
			g.fillOval(selected_piece.x*(xwidth/COLS)+xoff-3, selected_piece.y*(yheight/ROWS)+yoff-3, 30, 30);
		}
		
		for(int row = 0; row<board.length; row++){
			for(int col = 0; col<board[0].length; col++){
				if(board[row][col] == Piece.EMPTY) continue;
				if(board[row][col] == Piece.OPPONENT){
					g.setColor(opponent_color);
					g.fillOval(col*(xwidth/COLS)+xoff, row*(yheight/ROWS)+yoff, 25, 25);
				}
				if(board[row][col] == Piece.PLAYER){
					g.setColor(player_color);
					g.fillOval(col*(xwidth/COLS)+xoff, row*(yheight/ROWS)+yoff, 25, 25);
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
		//debug: add Graphics g and drawRect to find button positions
		buttons = new Rectangle[ROWS][COLS];
		int xwidth = getWidth();
		int yheight= getHeight();
		
		int xoff = getWidth()/29;
		int yoff = getHeight()/15;
		
		for(int row = 0; row < buttons.length; row++){
			for(int col = 0; col < buttons[0].length; col++){
				g.drawRect(col*(xwidth/COLS)+xoff, row*(yheight/ROWS)+yoff, 25, 25);
				buttons[row][col] = new Rectangle(col*(xwidth/COLS)+xoff, row*(yheight/ROWS)+yoff, 25, 25);
			}
		}
	}
	
}
