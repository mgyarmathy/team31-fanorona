import java.io.*;
import java.net.*;


public class FanoronaServer implements Runnable{
	//establish server port
	public FanoronaServer(){}
	
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
		
		sendMessage(sockOutput, "BEGIN");
		//capture_move   ::==  A position position | W position position
				//paika_move     ::==  P position position
				//sacrifice_move ::==  S position
		sendMessage(sockOutput, "A 6 3 5 3"); //server sends first move
		String ok = receiveMessage(sockInput);
		System.out.println(ok);
		String playerMove = receiveMessage(sockInput);
		//parse player move
		String[] tokens = playerMove.split("\\s+");
		if(tokens[0].equals("A") || tokens[0].equals("W") || tokens[0].equals("P")){
			int fromCol = Integer.parseInt(tokens[1]);
			int fromRow = Integer.parseInt(tokens[2]);
			int toCol = Integer.parseInt(tokens[3]);
			int toRow = Integer.parseInt(tokens[4]);
			System.out.println(playerMove);
			//TODO: perform piece movement and update client board appropriately
			sendMessage(sockOutput, "OK"); //confirm move
		}
		else if(tokens[0].equals("S")){
			int sacCol = Integer.parseInt(tokens[1]);
			int sacRow = Integer.parseInt(tokens[2]);
			System.out.println(playerMove);
			//perform sacrifice on that specific piece
			sendMessage(sockOutput, "OK"); //confirm move
		}
		sendMessage(sockOutput, "OK"); //confirm move received
		
		
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
