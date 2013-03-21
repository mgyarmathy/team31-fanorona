import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Fanorona extends JFrame{
	
	GamePanel board;
	
	
	public Fanorona(){
		super("Team 31 - Fanorona");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMenuBar();
		Container content = getContentPane();
		InfoPanel info = new InfoPanel();
		board = new GamePanel(info);
		content.add(board, BorderLayout.CENTER);
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
		
		JMenuItem help = new JMenuItem("How To Play");
		help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
            	JOptionPane.showMessageDialog(null,
            			"How to Play Fanarona" +
            			"\n- Perform moves by selecting a piece and selecting an open adjacent space to move to." +
            			"\n- There are two kinds of moves, non-capturing and capturing moves. " +
            			"\n- A non-capturing move involves moving a piece along a line to an adjacent space." +
            			"\n- Capturing moves can be made in two different ways to remove one or more of the opponent's pieces:" +
            			"\n   1. By approach -- moving the capturing piece to a point adjacent to an opponent piece." +
            			"\n   2. By withdrawal -- moving in the opposite direction of an adjacent piece." +
            			"\n- When an opponent stone is captured, all opponent pieces in line behind that stone are captured as well." +
            			"\n- A capturing piece is allowed to continue making successive captures, with the following restrictions:" +
            			"\n   1. The piece is not allowed to arrive at the same position twice." +
            			"\n   2. It is not allowed to move a piece in the same direction as directly before in the capturing sequence." +
            			"\n- A capturing move must be made if possible, otherwise the player can make a non-capturing move." +
            			"\n- The game ends when one player captures all stones of the opponent. " +
            			"\n- If neither player can achieve this within 50 turns, the game is a draw", 
            			"Team 31 - Fanarona",
            			JOptionPane.QUESTION_MESSAGE);
            }
        });
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
            	JOptionPane.showMessageDialog(null,
            			"Fanarona" +
            			"\nCreated By: David Harrison, Jeffrey Foss, and Michael Gyarmathy" +
            			"\nCSCE 315 - Dr. John Keyser - Spring 2013" +
            			"\nTexas A&M University", 
            			"Team 31 - Fanarona",
            			JOptionPane.INFORMATION_MESSAGE);
            }
        });
		fileMenu.add(newGame);
		fileMenu.add(help);
		fileMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setJMenuBar(menuBar);
	}
	
	
}
