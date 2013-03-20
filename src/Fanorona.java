import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Fanorona extends JFrame{
	
	public GamePanel board;
	
	
	public Fanorona(){
		super("Team 31 - Fanorona");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMenuBar();
		Container content = getContentPane();
		InfoPanel info = new InfoPanel();
		board = new GamePanel(info);
		content.add(board, BorderLayout.WEST);
		content.add(info, BorderLayout.EAST);
		pack();
		setVisible(true);
	}
	
	public void addMenuBar(){
		JMenuBar menuBar = 	new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem newGame = new JMenuItem("New Game");
		newGame.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
                board.newGame();
            }
        });
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
