import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


public class Fanorona extends JFrame{
	
	int ROWS;
	int COLS;
	
	FanoronaServer fserver;
	Client fclient;
	String SocketString = "localhost";
	int SocketPort = 4555;
	
	int clockTime=0;
	
	private GamePanel board;
	private InfoPanel info;
	private Stopwatch stopw;
	
	
	
	boolean AIControl = false;
	
	
	public Fanorona(){
		super("Team 31 - Fanorona");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMenuBar();
		
		Container content = getContentPane();
		info = new InfoPanel();
		stopw = new Stopwatch(clockTime); new Thread(stopw).start();
		board = new GamePanel(info,stopw);
		stopw.addboard(board);
		content.add(board, BorderLayout.CENTER);	
		content.add(info, BorderLayout.EAST);
		content.add(stopw, BorderLayout.SOUTH);
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
            	
            	if(!board.servermode){
            		info.initial=false;
            		board.newGame();
            	}
               
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
		JMenu options = new JMenu("Options");
		JMenuItem boardSize = new JMenuItem("Set Board Size");
		boardSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				int rows = board.getNumberOfRows(); 
				int cols = board.getNumberofCols();
				
				JTextField rowsField = new JTextField(""+rows,2);
			    JTextField colsField = new JTextField(""+cols,2);

			    JPanel panel = new JPanel();
			    panel.add(new JLabel("rows:"));
			    panel.add(rowsField);
			    panel.add(Box.createHorizontalStrut(15)); // a spacer
			    panel.add(new JLabel("columns:"));
			    panel.add(colsField);
			    
			    JOptionPane.showMessageDialog(null, "Note: Rows and Columns must be odd and between 1-13 for valid game." +
			    		"\nChanging the board size will start a new game.", "Team 31 - Fanorona", JOptionPane.WARNING_MESSAGE);
			    int result = JOptionPane.showConfirmDialog(null, panel, 
			             "Please Enter Row and Column Size", JOptionPane.OK_CANCEL_OPTION);
			    if (result == JOptionPane.OK_OPTION) {
			       rows = Integer.parseInt(rowsField.getText());
			       cols = Integer.parseInt(colsField.getText());
			       if((rows % 2 == 1) && (cols % 2 == 1)){
			    	   board.setBoardSize(rows, cols);
				       board.newGame();
			       }
			       else{
			    	   info.write("Invalid Board Size");
			       }
			    }
			      
				
			}
		});
		JMenuItem playerName = new JMenuItem("Set Player Name");
		playerName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
            	String playername = JOptionPane.showInputDialog(null,
            						"Input Player Name: ", 
            						"Team 31 - Fanarona",
            						JOptionPane.INFORMATION_MESSAGE);
            	board.setPlayerName(playername);
            }
        });
		
		JMenu pieceColors = new JMenu("Set Piece Colors");
		ButtonGroup colorGroup = new ButtonGroup();
		JRadioButtonMenuItem whiteBlack = new JRadioButtonMenuItem("Default", true);
		whiteBlack.setActionCommand("whiteBlack");
		whiteBlack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("whiteBlack")){
					board.setPlayerColors(Color.WHITE, Color.BLACK);
				}
				
			}
		});
		colorGroup.add(whiteBlack);
		pieceColors.add(whiteBlack);
		JRadioButtonMenuItem blackWhite = new JRadioButtonMenuItem("Black/White");
		blackWhite.setActionCommand("blackWhite");
		blackWhite.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("blackWhite")){
					board.setPlayerColors(Color.BLACK, Color.WHITE);
				}
				
			}
		});
		colorGroup.add(blackWhite);
		pieceColors.add(blackWhite);
		JRadioButtonMenuItem blueRed = new JRadioButtonMenuItem("Blue/Red");
		blueRed.setActionCommand("blueRed");
		blueRed.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("blueRed")){
					board.setPlayerColors(Color.BLUE, Color.RED);
				}
				
			}
		});
		colorGroup.add(blueRed);
		pieceColors.add(blueRed);
		JRadioButtonMenuItem custom = new JRadioButtonMenuItem("Custom");
		custom.setActionCommand("custom");
		custom.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("custom")){
					Color playerColor = JColorChooser.showDialog(
		                     new JColorChooser(),
		                     "Choose Player Color",
		                     board.getPlayerColor());
					Color opponentColor = JColorChooser.showDialog(
		                     new JColorChooser(),
		                     "Choose Opponent Color",
		                     board.getOpponentColor());
					board.setPlayerColors(playerColor, opponentColor);
				}
				
			}
		});
		colorGroup.add(custom);
		pieceColors.add(custom);
		
		JCheckBoxMenuItem mute = new JCheckBoxMenuItem("Mute Sound");
		mute.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					board.setPlayerName("mute");
				}
			}
		});
		
		JMenu player1Menu = new JMenu("Player1");
		JMenuItem H1 = new JMenuItem("Human");
		H1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setP1Humans();
                board.newGame();
               
            }
        });
		
		JMenuItem C1 = new JMenuItem("Computer");
		C1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setP1HumanAI();
                board.newGame();
               
            }
        });
		JMenu player2Menu = new JMenu("Player2");
		JMenuItem H2 = new JMenuItem("Human");
		H2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setP2Humans();
                board.newGame();
               
            }
        });
		
		JMenuItem C2 = new JMenuItem("Computer");
		C2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setP2HumanAI();
                board.newGame();
               
            }
        });
		
		JMenu connection = new JMenu("Connection");
		JMenuItem none = new JMenuItem("None");
		none.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	if(fserver!=null) fserver.terminate();
            	
            	if(fclient!=null) fclient.terminate();
            	
            	board.servermode=false;
               
            }
        });
		JMenuItem server = new JMenuItem("Server");
		server.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	fserver = new FanoronaServer(SocketPort,board,info);
        		Thread server = new Thread(fserver);
        		server.start();
                //board.newGame();
               
            }
        });
		
		JMenuItem client = new JMenuItem("Client");
		client.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	fclient = new Client(SocketString,SocketPort,board,info);
            	Thread client = new Thread(fclient);
        		client.start();
                //board.newGame();
               
            }
        });
		
		
		connection.add(none);
		connection.add(server);
		connection.add(client);
		
		
		player1Menu.add(H1);
		player1Menu.add(C1);
		player2Menu.add(H2);
		player2Menu.add(C2);
		
		options.add(boardSize);
		options.add(playerName);
		options.add(pieceColors);
		options.add(mute);
		menuBar.add(fileMenu);
		menuBar.add(options);
		menuBar.add(player1Menu);
		menuBar.add(player2Menu);
		menuBar.add(connection);
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setJMenuBar(menuBar);
	}
	
}