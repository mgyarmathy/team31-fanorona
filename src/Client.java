import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client extends Thread{

	private Socket c_socket = null;
	private InputStream c_sockInput = null;
	private OutputStream c_sockOutput = null;
	
	private boolean stopClient = false;
	
	private String SocketString;
	private int SocketPort;
	
	int COLS,ROWS;
	int clockTime;
	InfoPanel info;
	GamePanel board;
	Stopwatch stopw;
	
	public Client(String host,int port,GamePanel b,InfoPanel i){
		SocketString = host;
		SocketPort = port;
		
		board = b;
		info = i;
		stopw = b.stopw;
	}
	
	public void terminate(){
		stopClient = true;
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
			//System.exit(1);
		}
		
	}
	
	public String receiveMessage(InputStream c_socketInput){
		byte[] buf = new byte[1024];
		try {
			c_socketInput.read(buf, 0, buf.length);
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
		//connect to server over localhost
		while(c_socket == null){
			try {
				c_socket = new Socket(SocketString, SocketPort);
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host: FanoronaServer."); 
				//System.exit(1);
			} catch(IOException e){
				System.err.println("Couldn't get I/O for the connection to: FanoronaServer.");
				//System.exit(1);
			}
			
			if(c_socket==null){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(stopClient){
				break;
			}
		}
		
		if(!stopClient){
			//get input/output streams from socket
			while(c_sockInput==null && c_sockOutput == null){
				try {
					c_sockInput = c_socket.getInputStream();
				} catch (IOException e) {
					System.err.println("Client unable to get input stream"); 
					//System.exit(1);
				}
				try {
					c_sockOutput = c_socket.getOutputStream();
				} catch (IOException e) {
					System.err.println("Client unable to get output stream"); 
					//System.exit(1);
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(stopClient)break;
			}
			
			if(!stopClient){
				//read WELCOME message from server
				String welcome = receiveMessage(c_sockInput);
				if(!welcome.startsWith("WELCOME")){ System.out.println("error: server did not WELCOME"); }
				System.out.println(welcome);
			
			
			
				if(!stopClient){
					//read INFO from server
					String gameInfo = receiveMessage(c_sockInput);
					String firstMove = "W";
					if(gameInfo.startsWith("INFO")) { //parse game INFO
						String[] tokens = gameInfo.split("\\s+");
						COLS = Integer.parseInt(tokens[1]);
						ROWS = Integer.parseInt(tokens[2]);
						firstMove = tokens[3]; //either W or B
						clockTime = Integer.parseInt(tokens[4]);
						
						if(COLS % 2 == 1 && ROWS % 2 == 1){
							board.setBoardSize(ROWS, COLS);
						}
						else { System.err.println("error: invalid board size from server"); }//System.exit(1);}
						
					}
					else { System.err.println("error: server did not give INFO"); }//System.exit(1);}
					System.out.println(gameInfo);
					
					if(!stopClient){
						// if you want ai vs ai you have to turn ai on here;
						board.servermode=true;
						if(firstMove.equals("W")){board.Player=1;}
						else {board.Player=2;}
						
						
						board.newGame();
						
						//send READY to server for game to begin
						sendMessage(c_sockOutput, "READY");
						
						//receive BEGIN, and game begins
						String begin = receiveMessage(c_sockInput);
						
						
						if(begin.startsWith("BEGIN")){
							
							stopw.timeStart();
						}
						else { System.err.println("error: server did not send BEGIN message"); }// System.exit(1);}
						System.out.println(begin);
						
						if(!stopClient){
							while(true){
								if(stopClient)break;
								
								if(board.Player==1){
									// make white move by ai
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
											if(stopClient)break;
										}
										if(stopClient)break;
											
									sendMessage(c_sockOutput, board.Player1move);
									board.Player1newmove = false;
									String ok = receiveMessage(c_sockInput);
									System.out.println(ok);
									//check to see if move caused WIN/DRAW
									if(board.win) {
										sendMessage(c_sockOutput, "WINNER");
										info.write("YOU Lose");
										break;
									}
									if(board.draw){
										sendMessage(c_sockOutput, "DRAW");
										info.write("Game ends in DRAW");
										break;
									}
									
									if(stopClient)break;
										
									String playerMove = receiveMessage(c_sockInput);
									info.write(playerMove);
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
										
										System.out.println(playerMove);
										sendMessage(c_sockOutput, "OK"); //confirm move
										if(goodmove<0) {
											sendMessage(c_sockOutput, "ILLEGAL"); //check if move legal
											sendMessage(c_sockOutput, "LOSER"); //inform server that they lost
										}
									}
									else if(tokens[0].equals("S")){
										int sacCol = Integer.parseInt(tokens[1]);
										int sacRow = Integer.parseInt(tokens[2]);
										System.out.println(playerMove);
										//perform sacrifice on that specific piece
										goodmove = board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
										sendMessage(c_sockOutput, "OK"); //confirm move
										if(goodmove<0) {
											sendMessage(c_sockOutput, "ILLEGAL"); //check if move legal
											sendMessage(c_sockOutput, "LOSER"); //inform server that they lost
										}
									}
									else if(tokens[0].equals("ILLEGAL")){
										break;
									}
									else if(tokens[0].equals("TIME")){
									}
									else if(tokens[0].equals("LOSER")){
										info.write("YOU LOSE");
										break;
									}
									else if(tokens[0].equals("WINNER")){
									}
									else if(tokens[0].equals("TIE")){
										break;
									}
									if(stopClient)break;
								}
								if(board.Player==2){
								
									if(stopClient)break;
									String playerMove = receiveMessage(c_sockInput);
									info.write(playerMove);
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
										
										System.out.println(playerMove);
										sendMessage(c_sockOutput, "OK"); //confirm move
										if(goodmove<0) {
											sendMessage(c_sockOutput, "ILLEGAL"); //check if move legal
											sendMessage(c_sockOutput, "LOSER"); //inform server that they lost
										}
									}
									else if(tokens[0].equals("S")){
										int sacCol = Integer.parseInt(tokens[1]);
										int sacRow = Integer.parseInt(tokens[2]);
										System.out.println(playerMove);
										//perform sacrifice on that specific piece
										goodmove = board.serverSacrificePiece(new Point(sacCol-1, ROWS - sacRow));
										sendMessage(c_sockOutput, "OK"); //confirm move
										if(goodmove<0) {
											sendMessage(c_sockOutput, "ILLEGAL"); //check if move legal
											sendMessage(c_sockOutput, "LOSER"); //inform server that they lost
										}
									}
									else if(tokens[0].equals("ILLEGAL")){
										break;
									}
									else if(tokens[0].equals("TIME")){
										System.out.println("Time up");
										break;
									}
									else if(tokens[0].equals("LOSER")){
										info.write("YOU LOSE");
										break;
									}
									else if(tokens[0].equals("WINNER")){
									}
									else if(tokens[0].equals("TIE")){
										break;
									}
									
									if(stopClient)break;
									// make black move by ai
									//board.serverMovePiece(new Point(fromCol-1, ROWS - fromRow), toCol-1, ROWS-toRow,"A");
					
									if(board.P2AI){ //AI determines move
										board.Player2AImove();
									}
									//player determines move
										while(!board.Player2newmove){
											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											
											if(stopClient)break;
										}
									
											
									sendMessage(c_sockOutput, board.Player2move);
									board.Player2newmove = false;
									String ok = receiveMessage(c_sockInput);
									System.out.println(ok);
									//check to see if move caused WIN/DRAW
									if(board.win) {
										sendMessage(c_sockOutput, "LOSER");
										info.write("YOU WIN");
										break;
									}
									if(board.draw){
										sendMessage(c_sockOutput, "DRAW");
										info.write("Game ends in DRAW");
										break;
									}
									if(stopClient)break;
								}
							}
						}
					}
				}
			}
		}
		
		//close socket at end of session
		try {
			c_socket.close();
		} catch (IOException e) {
			System.err.println("Client unable to close client socket"); 
			//System.exit(1);
		}
		
	}
}
