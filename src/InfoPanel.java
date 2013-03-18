import javax.swing.*;
import java.awt.*;

//InfoPanel displays information about the current game
public class InfoPanel extends JPanel{
	public InfoPanel(){
		setPreferredSize(new Dimension(200, 0));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
		setVisible(true);
	}
	
	public void write(String s){
		
		Graphics g = this.getGraphics();
		super.paintComponent(g);
		g.drawString(s,20,20);
		
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		//draw the information here
		g.drawString("This is the info panel", 20, 20);
	 }
}
