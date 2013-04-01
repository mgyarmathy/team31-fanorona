import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


public class Fanorona extends JFrame implements Runnable{
	
	GamePanel board;
	InfoPanel info;
	Stopwatch stopw;
	
	Socket c_socket = null;
	InputStream c_sockInput = null;
	OutputStream c_sockOutput = null;
	
	
	public Fanorona(){
		super("Team 31 - Fanorona");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMenuBar();
		setVisible(false);
	}
	
	public void addMenuBar(){
		JMenuBar menuBar = 	new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem newGame = new JMenuItem("New Game");
		newGame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
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
		
		JMenu playerMenu = new JMenu("Players");
		JMenuItem HvH = new JMenuItem("Human vs Human");
		HvH.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setHumans();
                board.newGame();
               
            }
        });
		
		JMenuItem HvC = new JMenuItem("Human vs Computer");
		HvC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setHumanAI();
                board.newGame();
               
            }
        });
		playerMenu.add(HvH);
		playerMenu.add(HvC);
		
		options.add(boardSize);
		options.add(playerName);
		options.add(pieceColors);
		options.add(mute);
		menuBar.add(fileMenu);
		menuBar.add(options);
		menuBar.add(playerMenu);
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setJMenuBar(menuBar);
	}
	
	public void sendMessage(OutputStream c_socketOutput, String message){
		byte[] buf = new byte[1024];
	    char[] charArray = message.toCharArray();
	    for(int i = 0; i<charArray.length; i++){
	    	buf[i] = (byte)charArray[i];
	    }
	    buf[charArray.length] = (byte)' ';
	    try {
			c_socketOutput.write(buf, 0, buf.length);
		} catch (IOException e1) {
			System.err.println("server: unable to write to output stream");
			System.exit(1);
		}
		
	}
	
	public String receiveMessage(InputStream c_socketInput){
		byte[] buf = new byte[1024];
		try {
			c_socketInput.read(buf, 0, buf.length);
		} catch (IOException e) {
			System.err.println("Client unable to read"); 
			System.exit(1);
		}
		String message = new String(buf);
		return message;
	}

	@Override
	public void run() {

		byte[] buf = new byte[1024];
		char[] message;
		
		//connect to server over localhost
		try {
			c_socket = new Socket("localhost", 4555);
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: FanoronaServer."); 
			System.exit(1);
		} catch(IOException e){
			System.err.println("Couldn't get I/O for the connection to: FanoronaServer.");
			System.exit(1);
		}
		
		//get input/output streams from socket
		try {
			c_sockInput = c_socket.getInputStream();
		} catch (IOException e) {
			System.err.println("Client unable to get input stream"); 
			System.exit(1);
		}
		try {
			c_sockOutput = c_socket.getOutputStream();
		} catch (IOException e) {
			System.err.println("Client unable to get output stream"); 
			System.exit(1);
		}
		
		//read WELCOME message from server
		String welcome = receiveMessage(c_sockInput);
		if(!welcome.startsWith("WELCOME")){ System.out.println("error: server did not WELCOME"); }
		System.out.println(welcome);
		
		//read INFO from server
		String gameInfo = receiveMessage(c_sockInput);
		if(gameInfo.startsWith("INFO")) { //parse game INFO
			String[] tokens = gameInfo.split("\\s+");
			int cols = Integer.parseInt(tokens[1]);
			int rows = Integer.parseInt(tokens[2]);
			String firstMove = tokens[3];
			int clockTime = Integer.parseInt(tokens[4]);
			Container content = getContentPane();
			info = new InfoPanel();
			stopw = new Stopwatch(clockTime); new Thread(stopw).start();
			board = new GamePanel(info,stopw);
			stopw.addboard(board);
			content.add(board, BorderLayout.CENTER);	
			content.add(info, BorderLayout.EAST);
			content.add(stopw, BorderLayout.SOUTH);
			pack();
			if(cols % 2 == 1 && rows % 2 == 1){
				board.setBoardSize(rows, cols);
			}
			else { System.err.println("error: invalid board size from server"); System.exit(1);}
			
		}
		else { System.err.println("error: server did not give INFO"); System.exit(1);}
		System.out.println(gameInfo);

		//send READY to server for game to begin
		sendMessage(c_sockOutput, "READY");
		
		//receive BEGIN, and game begins
		String begin = receiveMessage(c_sockInput);
		if(begin.startsWith("BEGIN")){
			board.newGame();
			setVisible(true);
		}
		else { System.err.println("error: server did not send BEGIN message"); System.exit(1);}
		System.out.println(begin);
		
		
		//capture_move   ::==  A position position | W position position
		//paika_move     ::==  P position position
		//sacrifice_move ::==  S position
		String move = receiveMessage(c_sockInput);
		String[] tokens = move.split("\\s+");
		if(tokens[0].equals("A") || tokens[0].equals("W")){
			int fromCol = Integer.parseInt(tokens[1]);
			int fromRow = Integer.parseInt(tokens[2]);
			int toCol = Integer.parseInt(tokens[3]);
			int toRow = Integer.parseInt(tokens[4]);
		}
		System.out.println(move);
		
		
		//close socket at end of session
		try {
			c_socket.close();
		} catch (IOException e) {
			System.err.println("Client unable to close client socket"); 
			System.exit(1);
		}
		
	}
	
	
}
