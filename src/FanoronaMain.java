import java.io.*;
import java.net.*;


public class FanoronaMain {

	public static void main(String[] args) {
		FanoronaServer fserver = new FanoronaServer();
		new Thread(fserver).start();
		
		Fanorona fclient = new Fanorona();
		new Thread(fclient).start();
	}

}
