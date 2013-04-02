import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


public class Fanorona extends JFrame implements Runnable{
	
	private GamePanel board;
	private InfoPanel info;
	private Stopwatch stopw;
	
	private Socket c_socket = null;
	private InputStream c_sockInput = null;
	private OutputStream c_sockOutput = null;
	
	
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
		
		JMenuItem CvC = new JMenuItem("Computer vs Computer");
		CvC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
            	info.initial=false;
            	board.setAIs();
                board.newGame();
               
            }
        });
		
		playerMenu.add(HvH);
		playerMenu.add(HvC);
		playerMenu.add(CvC);
		
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

		//byte[] buf = new byte[1024];
		//char[] message;
		int ROWS = 5;
		int COLS = 9;
		
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
		String firstMove = "W";
		if(gameInfo.startsWith("INFO")) { //parse game INFO
			String[] tokens = gameInfo.split("\\s+");
			COLS = Integer.parseInt(tokens[1]);
			ROWS = Integer.parseInt(tokens[2]);
			firstMove = tokens[3]; //either W or B
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
			if(COLS % 2 == 1 && ROWS % 2 == 1){
				board.setBoardSize(ROWS, COLS);
			}
			else { System.err.println("error: invalid board size from server"); System.exit(1);}
			
		}
		else { System.err.println("error: server did not give INFO"); System.exit(1);}
		System.out.println(gameInfo);
		
		board.servermode=true;
		if(firstMove.equals("W")) board.Player=1;
		else board.Player=2;

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
		
		while(true){
			if(board.Player==1){
				// make white move by ai
				//board.serverMovePiece(new Point(fromCol-1, ROWS - fromRow), toCol-1, ROWS-toRow,"A");
				while(!board.Player1newmove){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
						
				sendMessage(c_sockOutput, board.Player1move);
				board.Player1newmove = false;
				String ok = receiveMessage(c_sockInput);
				System.out.println(ok);
					
				String playerMove = receiveMessage(c_sockInput);
				info.write(playerMove);
				//parse player move
				String[] tokens = playerMove.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					for(int i = 0; i<tokens.length; i=i+6){
						String dir = tokens[i];
						int fCol = Integer.parseInt(tokens[i+1]);
						int fRow = Integer.parseInt(tokens[i+2]);
						int tCol = Integer.parseInt(tokens[i+3]);
						int tRow = Integer.parseInt(tokens[i+4]);
						//tokens[i+5] = "+"
						board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
					}
					
					System.out.println(playerMove);
					sendMessage(c_sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("S")){
					int sacCol = Integer.parseInt(tokens[1]);
					int sacRow = Integer.parseInt(tokens[2]);
					System.out.println(playerMove);
					//perform sacrifice on that specific piece
					board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
					sendMessage(c_sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("ILLEGAL")){
				}
				else if(tokens[0].equals("TIME")){
				}
				else if(tokens[0].equals("LOSER")){
				}
				else if(tokens[0].equals("WINNER")){
				}
				else if(tokens[0].equals("TIE")){
					break;
				}
			}
			if(board.Player==2){
			
				String playerMove = receiveMessage(c_sockInput);
				info.write(playerMove);
				//parse player move
				String[] tokens = playerMove.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					for(int i = 0; i<tokens.length; i=i+6){
						String dir = tokens[i];
						int fCol = Integer.parseInt(tokens[i+1]);
						int fRow = Integer.parseInt(tokens[i+2]);
						int tCol = Integer.parseInt(tokens[i+3]);
						int tRow = Integer.parseInt(tokens[i+4]);
						//tokens[i+5] = "+"
						board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
					}
					
					System.out.println(playerMove);
					sendMessage(c_sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("S")){
					int sacCol = Integer.parseInt(tokens[1]);
					int sacRow = Integer.parseInt(tokens[2]);
					System.out.println(playerMove);
					//perform sacrifice on that specific piece
					board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
					sendMessage(c_sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("ILLEGAL")){
				}
				else if(tokens[0].equals("TIME")){
				}
				else if(tokens[0].equals("LOSER")){
				}
				else if(tokens[0].equals("WINNER")){
				}
				else if(tokens[0].equals("TIE")){
					break;
				}
				
				// make white move by ai
				//board.serverMovePiece(new Point(fromCol-1, ROWS - fromRow), toCol-1, ROWS-toRow,"A");
				while(!board.Player2newmove){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// Auto-generated catch block
						e.printStackTrace();
					}
				}
						
				sendMessage(c_sockOutput, board.Player2move);
				board.Player2newmove = false;
				String ok = receiveMessage(c_sockInput);
				System.out.println(ok);
			}
		}		
		
		//close socket at end of session
		try {
			c_socket.close();
		} catch (IOException e) {
			System.err.println("Client unable to close client socket"); 
			System.exit(1);
		}
		
	}
	
	
}

//snippet to handle multiple moves
//String dir = tokens[0];
//example string: A 5 4 5 3 + 5 3 4 3 +  4  3  3  3 
//indices         0 1 2 3 4 5 6 7 8 9 10 11 12 13 14
//and for one move: A 5 4 5 3
//indices			0 1 2 3 4
//for(int i = 1; i<tokens.length; i=i+5){
//	int fCol = Integer.parseInt(tokens[i]);
//	int fRow = Integer.parseInt(tokens[i+1]);
//	int tCol = Integer.parseInt(tokens[i+2]);
//	int tRow = Integer.parseInt(tokens[i+3]);
	//tokens[i+4] = "+"
//	board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
//}

