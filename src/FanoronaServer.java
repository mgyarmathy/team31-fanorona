import java.io.*;
import java.net.*;


public class FanoronaServer implements Runnable{
	//establish server port
	public FanoronaServer(){}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
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
		
		InputStream sockInput = null;
		OutputStream sockOutput = null;
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
		
		byte[] buf = new byte[1024];
		char[] message = "WELCOME".toCharArray();
	    for(int i = 0; i<message.length; i++){
	    	buf[i] = (byte)message[i];
	    }
	    try {
			sockOutput.write(buf, 0, buf.length);
		} catch (IOException e1) {
			System.err.println("server: unable to write to output stream");
			System.exit(1);
		}
	    
	    buf = new byte[1024];
	    message = "INFO 9 5 B 5000".toCharArray();
	    for(int i = 0; i<message.length; i++){
	    	buf[i] = (byte)message[i];
	    }
	    buf[message.length] = (byte)' ';
	    try {
			sockOutput.write(buf, 0, buf.length);
		} catch (IOException e1) {
			System.err.println("server: unable to write to output stream");
			System.exit(1);
		}
	    
	    
	    try {
			int bytes_read = sockInput.read(buf, 0, buf.length);
		} catch (IOException e1) {
			System.err.println("Client unable to read"); 
			System.exit(1);
		}
		
		String ready = new String(buf);
		System.out.println(ready);
		
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
