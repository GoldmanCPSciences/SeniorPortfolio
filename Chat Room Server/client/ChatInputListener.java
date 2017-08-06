import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author Evan Wang
 * @author Andy Goldman
 * 
 * This is a separate thread run by the ChatScreen.
 * It listens for messages from the server and appends them to the dispay in ChatScreen.
 */

public class ChatInputListener implements Runnable
{
	BufferedReader serverReader;
	ChatScreen cs;

	public ChatInputListener(Socket serverSocket, ChatScreen cs)
			throws IOException
	{
		serverReader = new BufferedReader(new InputStreamReader(
				serverSocket.getInputStream()));
		this.cs = cs;//Reference to GUI object so we can append to the display
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
					//Read server message
					String serverMessage = (serverReader.readLine());
					
					if (serverMessage != null)//If string is null, connection closed
					{
						
						//Parse input
						 System.out.println("Server says: " + serverMessage);
						
						 if(serverMessage.charAt(0)=='5')//<5><” “><Username><” “><time><” “><message></r/n>
						 {
							 cs.displayText(serverMessage.substring(serverMessage.indexOf(" ")+1,serverMessage.indexOf(" ",2))+": "+serverMessage.substring(serverMessage.indexOf(" ",findNthOccurence(serverMessage,3,' ')), serverMessage.length()));
						 }
						 else if(serverMessage.charAt(0)=='6')//<6><” “><fromUsername><” “><toUsername><” “><time><” “><message></r/n>
						 {
							 cs.displayText(serverMessage.substring(serverMessage.indexOf(" ")+1,serverMessage.indexOf(" ",2))+" whispered: "+serverMessage.substring(serverMessage.indexOf(" ",findNthOccurence(serverMessage,4,' ')), serverMessage.length()));
						 }
						 else if (serverMessage.charAt(0) == '9')
							cs.removeFromNameList(serverMessage.substring(2,
									serverMessage.length()));
						else if (serverMessage.charAt(0) == '1'&& serverMessage.charAt(1) == '0')
						{
									cs.addToNameList(serverMessage.substring(3,
									serverMessage.length()));
						}
	
					}
				}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}		
		
		
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
	
}
