import javax.swing.*;

import java.awt.*;


public class Fanorona extends JFrame{
	public Fanorona(){
		super("Team 31 - Fanorona");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMenuBar();
		Container content = getContentPane();
		content.add(new GamePanel(), BorderLayout.WEST);
		content.add(new InfoPanel(), BorderLayout.EAST);
		pack();
		setVisible(true);
	}
	
	public void addMenuBar(){
		JMenuBar menuBar = 	new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem newGame = new JMenuItem("New Game");
		fileMenu.add(newGame);
		menuBar.add(fileMenu);
		menuBar.add(Box.createHorizontalGlue());
		JMenu help = new JMenu("Help");
		menuBar.add(help);
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setJMenuBar(menuBar);
	}
}
