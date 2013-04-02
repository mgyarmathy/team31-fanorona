import java.io.*;
import java.net.*;


public class FanoronaMain {

	public static void main(String[] args) {
		FanoronaServer fserver = new FanoronaServer();
		Thread server = new Thread(fserver);
		server.start();
		
		Fanorona fclient = new Fanorona();
		Thread client = new Thread(fclient);
		client.start();
	}

}
