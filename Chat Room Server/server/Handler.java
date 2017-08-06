/**
 * @author Greg Gagne
 * @author Evan Wang
 */

import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;

public class Handler
{
	public static final int BUFFER_SIZE = 1024;

	byte[] buffer = new byte[BUFFER_SIZE];
	private HashMap<String, Socket> userMap;
	private final String welcomeMessage = "Welcome to the server";
	FileInputStream inputStream;
	PrintWriter logWriter;
	String userName;

	/**
	 * this method is invoked by a separate thread
	 */
	public void process(Socket client, HashMap<String, Socket> userMap)
			throws java.io.IOException
	{
		//preferred hash map implementation so we could delete names
		//and sockets easily
		this.userMap = userMap;

		InputStream fromClient = null;
		
		DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
		Socket server = null;

		/**
		 * get the input and output streams associated with the socket.
		 */
		fromClient = new BufferedInputStream(client.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

		try
		{
			String clientMessage = (reader.readLine());
			System.out.println(clientMessage);
			if (clientMessage.charAt(0) == '0')
			{
				userName = clientMessage.substring(clientMessage.indexOf(" ")+1);
				if (userMap.containsKey(userName))
				{
					toClient.write("2\r\n".getBytes());
					System.out.println("Fail");
					client.close();
				}
				else
				{		
					clientHasJoined();
					System.out.println(userName);
					userMap.put(userName, client);
					
//					<1><" "><user1,user2,user3,...><" "><welcome message></r/n>
					toClient.write(("1 "+userListToString()+" "+welcomeMessage+"\r\n").getBytes());
										
					chat(reader);
				}
			}
		}
		//If we have a socket error, we remove that user since we can no longer communicate with them
		catch (NullPointerException npe)
		{
			userMap.remove(userName);
			clientHasLeft();
			System.out.println("Finished");
		}
		//if there's an error writing to the socket, 
		//we remove from the map of users since that's most
		//likely what happened
		catch (SocketException se)
		{
			userMap.remove(userName);
			clientHasLeft();
			System.out.println("Finished");
		}
		finally
		{
			// close streams and sockets
			if (fromClient != null)
				fromClient.close();
			if (toClient != null)
				toClient.close();
			if (client != null)
				client.close();
			if (server != null)
				server.close();
			if (inputStream != null)
				inputStream.close();
		}
	}
	
	/**
	 * @return the user list as a string, comma delimited
	 */
	private String userListToString()
	{
		String userListString = userMap.keySet().toString().replaceAll("\\s", "");
		return userListString.substring(1, userListString.length()-1);
	}

	/**
	 * Sends message to all:
	 * <3><" "><message></r/n>
	 * 
	 * What it should have received:
	 * <4><" "><fromUsername><" "><toUsername><" "><message></r/n>
	 * 
	 * @param clientMessage, the message to send
	 * @throws IOException
	 */
	private void sendToAll(String clientMessage) throws IOException
	{
		clientMessage = clientMessage.substring(clientMessage.indexOf(" ")+1, clientMessage.length());
		Iterator<Entry<String, Socket>> i = userMap.entrySet().iterator();

		while (i.hasNext())
		{
			String uName = i.next().getKey();
			Socket curr = userMap.get(uName);

			if (!curr.isClosed())
			{
				DataOutputStream current = new DataOutputStream(curr.getOutputStream());
				current.write(("5 " +userName+" "+ZonedDateTime.now().format(DateTimeFormatter.ofPattern("uuuu MM dd HH mm ss")).replaceAll("\\s", ":")+" "+ clientMessage + "\r\n").getBytes());
			}
		}
	}
	
	/**
	 * <6><" "><fromUsername><" "><toUsername><" "><time><" "><message></r/n>
	 * 
	 * @param clientMessage
	 * @throws IOException
	 */
	private void sendPrivateMesage(String clientMessage) throws IOException
	{
		int third = findNthOccurence(clientMessage,3,' ');
		String messageContent = clientMessage.substring(third+1, clientMessage.length());
		clientMessage = clientMessage.substring(2, third);
		String from = clientMessage.substring(0, clientMessage.indexOf(" "));
		String to = clientMessage.substring(clientMessage.indexOf(" ")+1,clientMessage.length());
		Socket toSocket = userMap.get(to);
		DataOutputStream current = new DataOutputStream(toSocket.getOutputStream());
		current.write(("6 " +from+" "+ to +" "+ZonedDateTime.now().format(DateTimeFormatter.ofPattern("uuuu MM dd HH mm ss")).replaceAll("\\s", ":")+" "+ messageContent+"\r\n").getBytes());
		
		DataOutputStream toSender = new DataOutputStream(userMap.get(userName).getOutputStream());
		toSender.write(("6 " +from+" "+ to +" "+ZonedDateTime.now().format(DateTimeFormatter.ofPattern("uuuu MM dd HH mm ss")).replaceAll("\\s", ":")+" "+ messageContent+"\r\n").getBytes());
	}
	
	/**
	 * Returns -1 if no occurence is found
	 * @param toSearch
	 * @param occurence
	 * @param toFind
	 * @return
	 */
	private int findNthOccurence(String toSearch,int occurence, char toFind)
	{
		int o = 0;
		for(int i = 0; i < toSearch.length(); i++)
		{
			if(toSearch.charAt(i)==toFind)
				o++;
			if(o==occurence)
				return i;
		}
		return -1;
			
	}
	
	/**
	 * Broadcast the name of the client who has disconnected
	 * <9><" "><username></r/n>
	 * @throws IOException
	 */
	private void clientHasLeft() throws IOException
	{
		Iterator<Entry<String, Socket>> i = userMap.entrySet().iterator();

		while (i.hasNext())
		{
			String uName = i.next().getKey();
			Socket curr = userMap.get(uName);
			
			if (!curr.isClosed())
			{
				DataOutputStream current = new DataOutputStream(curr.getOutputStream());
				current.write(("9 " +userName+"\r\n").getBytes());
			}
		}
	}
	
	/**
	 * Broadcast the name of the client who has connected
	 * <10><" "><username></r/n>
	 * @throws IOException
	 */
	private void clientHasJoined() throws IOException
	{
		
		Iterator<Entry<String, Socket>> clientIterator = userMap.entrySet().iterator();

		while (clientIterator.hasNext())
		{
			String currUserName = clientIterator.next().getKey();
			Socket currUserSocket = userMap.get(currUserName);

			
			if (!currUserSocket.isClosed())
			{
				DataOutputStream current = new DataOutputStream(currUserSocket.getOutputStream());
				current.write(("10 " +userName+"\r\n").getBytes());
			}
		}
	}
	/**
	 * Sent to client when they want to disconnect
	 * <8></r/n>
	 * @throws IOException
	 */
	private void sendDisconnectToClient() throws IOException
	{
		Socket clientSocket = userMap.get(userName);
		if(!clientSocket.isClosed())
		{
			DataOutputStream current = new DataOutputStream(clientSocket.getOutputStream());
			current.write(("8\r\n").getBytes());	
		}
		
	}
	
	/**
	 * The method that listens for input for the client
	 * Called from process
	 */
	private void chat(BufferedReader reader) throws IOException
	{
		String clientMessage = null;
		boolean running = true;
		while (running)
		{
			clientMessage = (reader.readLine());
			System.out.println("Client message: " + clientMessage);
			if (clientMessage != null)
			{
				/**
				 * Sends general message
				 * <3><" "><message></r/n>
				 */
				if(clientMessage.charAt(0)=='3')
				{
					sendToAll(clientMessage);
				}
				
				/**
				 * User sends private to server
				 * <4><" "><fromUsername><" "><toUsername><" "><message></r/n>
				 */
				else if(clientMessage.charAt(0)=='4')
				{
					System.out.println("private message");
					sendPrivateMesage(clientMessage);
				}
				else if(clientMessage.charAt(0)=='7')
				{
					System.out.println("Client wants to disconnect");
					sendDisconnectToClient();
					running = false;
				}

			}
			
			else
				running = false;
		}
		
		//Remove user when they disconect
		userMap.remove(userName);
		clientHasLeft();
		System.out.println("Finished");
	}
}
