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
		int timer_msecs = 0000;
		
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
		
		board.servermode=true;
		if(firstMove.equals("B"))board.Player=1;
		else board.Player=2;
		
		
		
		sendMessage(sockOutput, "BEGIN");
		board.newGame();
		setVisible(true);
		//capture_move   ::==  A position position | W position position
				//paika_move     ::==  P position position
				//sacrifice_move ::==  S position
		//int fromCol = 5;
		//int fromRow = 2;
		//int toCol = 5;
		//int toRow = 3;
		
		
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
						
				sendMessage(sockOutput, board.Player1move);
				board.Player1newmove = false;
				String ok = receiveMessage(sockInput);
				System.out.println(ok);
					
				String playerMove = receiveMessage(sockInput);
				info.write(playerMove);
				//parse player move
				String[] tokens = playerMove.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					String dir = tokens[0];
					
					for(int i = 1; i<tokens.length; i=i+5){
						int fCol = Integer.parseInt(tokens[i]);
						int fRow = Integer.parseInt(tokens[i+1]);
						int tCol = Integer.parseInt(tokens[i+2]);
						int tRow = Integer.parseInt(tokens[i+3]);
						//tokens[i+4] = "+"
						board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
					}
					
					System.out.println(playerMove);
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
			if(board.Player==2){
			
				String playerMove = receiveMessage(sockInput);
				info.write(playerMove);
				//parse player move
				String[] tokens = playerMove.split("\\s+");
				if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
					
					String dir = tokens[0];
					
					for(int i = 1; i<tokens.length; i=i+5){
						int fCol = Integer.parseInt(tokens[i]);
						int fRow = Integer.parseInt(tokens[i+1]);
						int tCol = Integer.parseInt(tokens[i+2]);
						int tRow = Integer.parseInt(tokens[i+3]);
						//tokens[i+4] = "+"
						board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
					}
					
					System.out.println(playerMove);
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
				while(!board.Player2newmove){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// Auto-generated catch block
						e.printStackTrace();
					}
				}
						
				sendMessage(sockOutput, board.Player1move);
				board.Player1newmove = false;
				String ok = receiveMessage(sockInput);
				System.out.println(ok);
			}
		}
		
		
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


