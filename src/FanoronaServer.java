
import java.awt.Point;
import java.io.*;
import java.net.*;




public class FanoronaServer implements Runnable{
	//establish server port
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private InputStream sockInput = null;
	private OutputStream sockOutput = null;
	
	private boolean stopServer = false;
	
	//private String SocketString;
	private int SocketPort;
	
	int COLS,ROWS;
	int timer_msecs;
	InfoPanel info;
	GamePanel board;
	Stopwatch stopw;
	
	public FanoronaServer(int port,GamePanel b,InfoPanel i){
		//SocketString = host;
		SocketPort = port;
		
		board = b;
		info = i;
		stopw = b.stopw;
		
		timer_msecs = stopw.getInterval();
	}
	
	public void terminate(){
		stopServer = true;
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
			//System.exit(1);
		}
		
	}
	
	public String receiveMessage(InputStream s_socketInput){
		byte[] buf = new byte[1024];
		try {
			s_socketInput.read(buf, 0, buf.length);
		} catch (IOException e) {
			System.err.println("Client unable to read"); 
			//System.exit(1);
		}
		String message = new String(buf);
		return message;
	}

	@Override
	public void run() {
		
		board.servermode=true;
		//create serverSocket
		if(!stopServer){
			while(serverSocket==null){
				try {
					serverSocket = new ServerSocket(SocketPort);
				} catch (IOException ex) {
					System.out.println("Could not listen on port: 4555");
					//System.exit(-1);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(stopServer)break;
			}
			
			//wait for client to connect
			while(clientSocket==null){
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					System.err.println("Accept failed.");
					//System.exit(1);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(stopServer)break;
			}
			System.out.println("connection established");
		
		
			//set up client input/output streams
			while(sockInput ==null){
				try {
					sockInput = clientSocket.getInputStream();
				} catch (IOException e) {
					System.err.println("server: unable to get client input stream");
					//System.exit(1);
				}
				try {
					sockOutput = clientSocket.getOutputStream();
				} catch (IOException e) {
					System.err.println("server: unable to get client output stream");
					//System.exit(1);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(stopServer)break;
			}
		}
	
		
		if(!stopServer){
			String firstMove = "B";
			
			//boolean AIControl = true;
			sendMessage(sockOutput, "WELCOME");
			COLS = board.getNumberofCols();
			ROWS = board.getNumberOfRows();
			timer_msecs = stopw.getInterval();
		    
			sendMessage(sockOutput, "INFO " + COLS + " " + ROWS + " " + firstMove + " " + timer_msecs);
			
			
			
			String ready = receiveMessage(sockInput);
			System.out.println(ready);
			
			
			board.servermode=true;
			if(firstMove.equals("B")) {board.Player=1; }
			else {board.Player=2;}
			
			sendMessage(sockOutput, "BEGIN");
			board.newGame();
		
			if(!stopServer){
					while(true){
						if(stopServer) break;
						if(board.Player==1){
							// make white move by AI
							//board.serverMovePiece(new Point(fromCol-1, ROWS - fromRow), toCol-1, ROWS-toRow,"A");
			
							if(board.P1AI){ //AI determines move
								board.Player1AImove();
							}
							//player determines move
								while(!board.Player1newmove){
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									if(stopServer) break;
								}
								if(stopServer) break;
							board.Player1newmove = false;	
								
							
							//info.write(board.Player1move);			
							sendMessage(sockOutput, board.Player1move);
							
							
							String ok = receiveMessage(sockInput);
							System.out.println(ok);
							if(board.win) {
								sendMessage(sockOutput, "LOSER");
								info.write("YOU WIN");
								break;
							}
							if(board.draw){
								sendMessage(sockOutput, "TIE");
								info.write("Game ends in DRAW");
								break;
							}
							if(stopServer) break;
							
							String playerMove = receiveMessage(sockInput);
							//info.write(playerMove);
							//parse player move
							
							int goodmove=0;	
							String[] tokens = playerMove.split("\\s+");
							if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
								for(int i = 0; i<tokens.length; i=i+6){
									String dir = tokens[i];
									int fCol = Integer.parseInt(tokens[i+1]);
									int fRow = Integer.parseInt(tokens[i+2]);
									int tCol = Integer.parseInt(tokens[i+3]);
									int tRow = Integer.parseInt(tokens[i+4]);
									//tokens[i+5] = "+"
									goodmove = board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
								}
								
								if(stopServer) break;
								System.out.println(playerMove);
								sendMessage(sockOutput, "OK"); //confirm move received
								if(goodmove<0) {
									sendMessage(sockOutput, "ILLEGAL"); break; //check if move legal
									//sendMessage(sockOutput, "LOSER"); //inform server that they lost
								}
							}
							else if(tokens[0].equals("S")){
								int sacCol = Integer.parseInt(tokens[1]);
								int sacRow = Integer.parseInt(tokens[2]);
								System.out.println(playerMove);
								//perform sacrifice on that specific piece
								goodmove = board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
								sendMessage(sockOutput, "OK"); //confirm move received
								if(goodmove<0) {
									sendMessage(sockOutput, "ILLEGAL");  break;//check if move legal
									//sendMessage(sockOutput, "LOSER"); //inform server that they lost
								}
							}
							else if(tokens[0].equals("ILLEGAL")){
								info.write("that move is not valid");
								break;
							}
							else if(tokens[0].equals("TIME")){
								info.write("Time up");
								break;
							}
							else if(tokens[0].equals("LOSER")){
								info.write("YOU LOSE");
								break;
							}
							else if(tokens[0].equals("WINNER")){
								info.write("YOU WIN");
								break;
							}
							else if(tokens[0].equals("TIE")){
								info.write("Game ends in DRAW");
								break;
							}
							
							while(board.TurnCount%2==1 && !board.draw && !board.win){
								//info.write("waiting P1");
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							
								board.countPieces();
								//board.resetmovestuff();
								if(stopServer) break;
							}
							
							
							if(stopServer) break;
							// add win/lose/draw detection
							
						}
						if(board.Player==2){
							if(stopServer) break;
							String playerMove = receiveMessage(sockInput);
							//info.write(playerMove);
							//parse player move
							int goodmove = 0;
							String[] tokens = playerMove.split("\\s+");
							if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
								for(int i = 0; i<tokens.length; i=i+6){
									String dir = tokens[i];
									int fCol = Integer.parseInt(tokens[i+1]);
									int fRow = Integer.parseInt(tokens[i+2]);
									int tCol = Integer.parseInt(tokens[i+3]);
									int tRow = Integer.parseInt(tokens[i+4]);
									//tokens[i+5] = "+"
									goodmove = board.serverMovePiece(new Point(fCol-1, ROWS - fRow), tCol-1, ROWS-tRow, dir);
								}
								if(stopServer) break;
								System.out.println(playerMove);
								sendMessage(sockOutput, "OK"); //confirm move
								if(goodmove<0) {sendMessage(sockOutput, "ILLEGAL"); break; } //check if move legal
								if(board.stopw.isTimeUp()) {sendMessage(sockOutput, "TIME"); break;} //check time expired
							}
							else if(tokens[0].equals("S")){
								int sacCol = Integer.parseInt(tokens[1]);
								int sacRow = Integer.parseInt(tokens[2]);
								System.out.println(playerMove);
								//perform sacrifice on that specific piece
								goodmove = board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
								sendMessage(sockOutput, "OK"); //confirm move
								if(goodmove<0) {sendMessage(sockOutput, "ILLEGAL"); break; } //check if move legal
								if(board.stopw.isTimeUp()) {sendMessage(sockOutput, "TIME"); break;} //check time expired
							}
							else if(tokens[0].equals("ILLEGAL")){
								info.write("that move is not valid");
								break;
							}
							else if(tokens[0].equals("TIME")){
								info.write("Time up");
								break;
							}
							else if(tokens[0].equals("LOSER")){
								info.write("YOU LOSE");
								break;
							}
							else if(tokens[0].equals("WINNER")){
								info.write("YOU WIN");
								break;
							}
							else if(tokens[0].equals("TIE")){
								info.write("Game ends in DRAW");
								break;
							}
							if(stopServer) break;
							// make black move by AI
							//board.serverMovePiece(new Point(fromCol-1, ROWS - fromRow), toCol-1, ROWS-toRow,"A");
							
							while(board.TurnCount%2==0 && !board.draw && !board.win){
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								board.countPieces();
								//board.resetmovestuff();
								if(stopServer) break;
							}
							
							if(board.P2AI){ //AI determines move
								board.Player2AImove();
							}
							else{ //player determines move
								while(!board.Player2newmove){
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									if(stopServer) break;
								}
							}
							if(stopServer) break;
							board.Player2newmove = false;
							
							sendMessage(sockOutput, board.Player2move);
							
							String ok = receiveMessage(sockInput);
							System.out.println(ok);
							//check to see if move caused WIN/DRAW
							if(board.win) {
								sendMessage(sockOutput, "LOSER");
								//info.write("YOU LOSE");
								break;
							}
							if(board.draw){
								sendMessage(sockOutput, "TIE");
								//info.write("Game ends in DRAW");
								break;
							}
						}
					}
			}
		}
	

				
				//close client and server sockets upon termination
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.err.println("server: unable to close client socket");
					//System.exit(1);
				}
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.err.println("server: unable to close server socket");
					//System.exit(1);
				}
	}
}