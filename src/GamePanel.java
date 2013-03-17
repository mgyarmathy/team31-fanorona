import javax.swing.*;
import java.awt.*;

// GamePanel displays the current state of the board.
public class GamePanel extends JPanel{
	public GamePanel(){
		setPreferredSize(new Dimension(600, 500));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setVisible(true);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//draw ze game here
		drawBoard(g);
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
		
		for(int i=1;i<10;i++){
			x1= xstartp+ xwidth*i/10; 
			g.drawLine(x1, y1 , x1, y2);
			g.drawLine(x1+1, y1 , x1+1, y2);
			g.drawLine(x1+2, y1 , x1+2, y2);
		}
		
		// draw horizontal lines
		x1= xstartp+ xwidth/10; 
		x2= xstartp+ xwidth*9/10;
		for(int i=1;i<10;i+=2){
			y1= ystartp+ yheight*i/10 ;
			g.drawLine(x1, y1 , x2, y1);
			g.drawLine(x1, y1+1 , x2, y1+1);
			g.drawLine(x1, y1+2 , x2, y1+2);
		}
		
		
		// draw diagonal lines
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
	

	
	
}
