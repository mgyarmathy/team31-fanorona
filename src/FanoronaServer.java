import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.io.*;
import java.net.*;

import javax.swing.JFrame;


public class FanoronaServer extends JFrame implements Runnable{
	//establish server port
	private GamePanel board;
	private InfoPanel info;
	private Stopwatch stopw;
	
	public FanoronaServer(){
		super("Team 31 - Fanorona Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(false);
	}
	
	public void sendMessage(OutputStream s_socketOutput, String message){
		byte[] buf = new byte[1024];
	    char[] charArray = message.toCharArray();
	    for(int i = 0; i<charArray.length; i++){
	    	buf[i] = (byte)charArray[i];
	    }
	    buf[charArray.length] = (byte)' ';
	    try {
			s_socketOutput.write(buf, 0, buf.length);
		} catch (IOException e1) {
			System.err.println("server: unable to write to output stream");
			System.exit(1);
		}
		
	}
	
	public String receiveMessage(InputStream s_socketInput){
		byte[] buf = new byte[1024];
		try {
			s_socketInput.read(buf, 0, buf.length);
		} catch (IOException e) {
			System.err.println("Client unable to read"); 
			System.exit(1);
		}
		String message = new String(buf);
		return message;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		InputStream sockInput = null;
		OutputStream sockOutput = null;
		
		//create serverSocket
		try {
			serverSocket = new ServerSocket(4555);
		} catch (IOException ex) {
			System.out.println("Could not listen on port: 4555");
			System.exit(-1);
		}
		
		//wait for client to connect
		Socket clientSocket = null;
		try {
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		System.out.println("connection established");
		
		//set up client input/output streams
		try {
			sockInput = clientSocket.getInputStream();
		} catch (IOException e) {
			System.err.println("server: unable to get client input stream");
			System.exit(1);
		}
		try {
			sockOutput = clientSocket.getOutputStream();
		} catch (IOException e) {
			System.err.println("server: unable to get client output stream");
			System.exit(1);
		}
		int ROWS = 5;
		int COLS = 9;
		String firstMove = "B";
		int timer_msecs = 20000;
		
		sendMessage(sockOutput, "WELCOME");
	    
		sendMessage(sockOutput, "INFO " + COLS + " " + ROWS + " " + firstMove + " " + timer_msecs);
		
		String ready = receiveMessage(sockInput);
		System.out.println(ready);
		
		Container content = getContentPane();
		info = new InfoPanel();
		stopw = new Stopwatch(timer_msecs); new Thread(stopw).start();
		board = new GamePanel(info,stopw);
		stopw.addboard(board);
		content.add(board, BorderLayout.CENTER);	
		content.add(info, BorderLayout.EAST);
		content.add(stopw, BorderLayout.SOUTH);
		pack();
		
		
		
		sendMessage(sockOutput, "BEGIN");
		board.newGame();
		setVisible(true);
		//capture_move   ::==  A position position | W position position
				//paika_move     ::==  P position position
				//sacrifice_move ::==  S position
		int fromCol = 5;
		int fromRow = 2;
		int toCol = 5;
		int toRow = 3;
		
		
		while(true){
			if(firstMove== "B"){
				// make white move by ai
				//board.serverMovePiece(new Point(fromCol-1, ROWS - fromRow), toCol-1, ROWS-toRow,"A");
				while(!board.Player1newmove){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
						
				sendMessage(sockOutput, board.Player1move);
				board.Player1newmove = false;
				String ok = receiveMessage(sockInput);
				System.out.println(ok);
					
				String playerMove = receiveMessage(sockInput);
				info.write(playerMove);
				//parse player move
				String[] tokens = playerMove.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					int fCol = Integer.parseInt(tokens[1]);
					int fRow = Integer.parseInt(tokens[2]);
					int tCol = Integer.parseInt(tokens[3]);
					int tRow = Integer.parseInt(tokens[4]);
					System.out.println(playerMove);
					//TODO: perform piece movement and update client board appropriately
					board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow,tokens[0]);
					sendMessage(sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("S")){
					int sacCol = Integer.parseInt(tokens[1]);
					int sacRow = Integer.parseInt(tokens[2]);
					System.out.println(playerMove);
					//perform sacrifice on that specific piece
					board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
					sendMessage(sockOutput, "OK"); //confirm move
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
			if(firstMove== "W"){
			
				String playerMove = receiveMessage(sockInput);
				info.write(playerMove);
				//parse player move
				String[] tokens = playerMove.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					int fCol = Integer.parseInt(tokens[1]);
					int fRow = Integer.parseInt(tokens[2]);
					int tCol = Integer.parseInt(tokens[3]);
					int tRow = Integer.parseInt(tokens[4]);
					System.out.println(playerMove);
					//TODO: perform piece movement and update client board appropriately
					board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow,tokens[0]);
					sendMessage(sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("S")){
					int sacCol = Integer.parseInt(tokens[1]);
					int sacRow = Integer.parseInt(tokens[2]);
					System.out.println(playerMove);
					//perform sacrifice on that specific piece
					board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
					sendMessage(sockOutput, "OK"); //confirm move
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
				while(!board.Player1newmove){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
						
				sendMessage(sockOutput, board.Player1move);
				board.Player1newmove = false;
				String ok = receiveMessage(sockInput);
				System.out.println(ok);
			}
		}
		
		/*if(firstMove.equals("W")){
			while(true){
				//server makes second move each turn
				
				
				//get client OK
				while(!receiveMessage(sockInput).startsWith("OK"));
				
				String serverMessage = receiveMessage(sockInput);
				String[] tokens = serverMessage.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					int fromCol = Integer.parseInt(tokens[1]);
					int fromRow = Integer.parseInt(tokens[2]);
					int toCol = Integer.parseInt(tokens[3]);
					int toRow = Integer.parseInt(tokens[4]);
					System.out.println(serverMessage);
					//perform piece movement and update client board appropriately
					sendMessage(sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("S")){
					int sacCol = Integer.parseInt(tokens[1]);
					int sacRow = Integer.parseInt(tokens[2]);
					System.out.println(serverMessage);
					//perform sacrifice on that specific piece
					sendMessage(sockOutput, "OK"); //confirm move
				}
				
				//TODO: handle "TIE" "LOSER" "WINNER" "ILLEGAL" 
				//break loop if this occurs
				
				//let client perform move - where do movements get written to infopanel?
				//capture_move   ::==  A position position | W position position
				//paika_move     ::==  P position position
				//sacrifice_move ::==  S position
				//sendMessage("this is a move");
			}
		}
		else if(firstMove.equals("B")){
			//server moves first each turn
			
			
			while(true){
				//let client perform move
				//capture_move   ::==  A position position | W position position
				//paika_move     ::==  P position position
				//sacrifice_move ::==  S position
				
				String serverMessage = receiveMessage(sockInput);
				String[] tokens = serverMessage.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					int fromCol = Integer.parseInt(tokens[1]);
					int fromRow = Integer.parseInt(tokens[2]);
					int toCol = Integer.parseInt(tokens[3]);
					int toRow = Integer.parseInt(tokens[4]);
					System.out.println(serverMessage);
					//perform piece movement and update client board appropriately
					sendMessage(sockOutput, "OK"); //confirm move
				}
				else if(tokens[0].equals("S")){
					int sacCol = Integer.parseInt(tokens[1]);
					int sacRow = Integer.parseInt(tokens[2]);
					System.out.println(serverMessage);
					//perform sacrifice on that specific piece
					sendMessage(sockOutput, "OK"); //confirm move
				}
				//TODO: handle "TIE" "LOSER" "WINNER" "ILLEGAL" 
				//break loop if this occurs
				
				
				
				//get server OK
				while(!receiveMessage(sockInput).startsWith("OK"));
				
			}
		}*/
		
		
		//close client and server sockets upon termination
		try {
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("server: unable to close client socket");
			System.exit(1);
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("server: unable to close server socket");
			System.exit(1);
		}
	}
}
