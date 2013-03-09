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
	
	public void paintComponent(Graphics g) {
		//draw ze game here
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		g.setColor(Color.RED);
		g.drawString("THIS IS THE GAME PANEL", 60, 250);
	 }
}
