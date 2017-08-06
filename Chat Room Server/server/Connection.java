/**
 * Evan Wang
 * This class runs in a separate thread in order to process requests from the client to the server. 
 * It creates a Handler object for each new request and sends the socket information there. 
 */


import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;




public class Connection implements Runnable
{
	private Socket	client;
	private HashMap<String,Socket> userMap;
	private Handler handler = new Handler();
	
	public Connection(Socket client,HashMap<String,Socket> userMap) {
		System.out.println(handler);
		this.client = client;
		this.userMap = userMap;
	}

    /**
     * This method runs in a separate thread.
     */	
	public void run() { 
		try {
			handler.process(client,userMap);
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
	}
}

