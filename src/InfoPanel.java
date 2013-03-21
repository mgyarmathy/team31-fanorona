import javax.swing.*;
import java.awt.*;

//InfoPanel displays information about the current game
public class InfoPanel extends JPanel{
	int i=0;
	public InfoPanel(){
		setPreferredSize(new Dimension(400, 0));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
		setVisible(true);
	}
	
	public void write(String s){
		Graphics g = this.getGraphics();
		if(i> getHeight() - 30) { super.paintComponent(g); i=0;}
		g.drawString(s,20,20 + i);
		i += 20;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		//draw the information here
		i=0;
		super.paintComponent(g);
		g.drawString("This is the info panel", 20, 20);
		i+=20;
	 }
}
