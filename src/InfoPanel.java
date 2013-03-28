import javax.swing.*;

import java.awt.*;

//InfoPanel displays information about the current game
public class InfoPanel extends JPanel{
	int i=0;
	public boolean initial = false;
	
	Container Q;
	
	public InfoPanel(){
		setPreferredSize(new Dimension(300, 0));
		setBackground(new Color(240,240,240));
		setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
		setVisible(true);
		RepaintManager.currentManager(this).markCompletelyClean(this);
	}
	
	public void write(String s){
		Graphics g = this.getGraphics();
		
		if(i> getHeight() - 30) {
			g.setColor(new Color(240,240,240));
			g.fillRect(0, 0, getWidth(), getHeight());  
			i=0;
		}
		g.setColor(Color.BLACK);
		g.drawString(s,20,20 + i);
		i += 20;
		RepaintManager.currentManager(this).markCompletelyClean(this);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		i=0;
		super.paintComponent(g);
		if(initial == false){
			g.drawString("Player 1's turn", 20, 20);
			i+=20;
		}
		RepaintManager.currentManager(this).markCompletelyClean(this);
		//repaint();
	}
}
