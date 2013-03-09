import javax.swing.*;
import java.awt.*;

//InfoPanel displays information about the current game
public class InfoPanel extends JPanel{
	public InfoPanel(){
		setPreferredSize(new Dimension(200, 0));
		setBackground(Color.RED);
		setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
		setVisible(true);
	}
	
	public void paintComponent(Graphics g) {
		//draw the information here
		g.drawString("This is the info panel", 20, 20);
	 }
}
