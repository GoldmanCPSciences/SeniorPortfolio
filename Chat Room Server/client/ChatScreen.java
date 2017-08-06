/** * This program is the client.  * It has a simple GUI for the user. *  * @author Evan Wang * @author Andy Goldman */import java.awt.*;import java.awt.event.*;import java.io.BufferedReader;import java.io.DataInputStream;import java.io.DataOutputStream;import java.io.IOException;import java.io.InputStreamReader;import java.net.Socket;import java.util.ArrayList;import java.util.Arrays;import java.util.Iterator;import javax.swing.*;import javax.swing.border.*;import javax.swing.text.DefaultCaret;@SuppressWarnings("serial")public class ChatScreen extends JFrame implements ActionListener, KeyListener{	private JButton sendButton;	private JButton exitButton;	private JTextField sendText;	private JTextArea displayArea;	private JTextArea nameArea;	private Socket connection = null;	private DataOutputStream toServer;	private BufferedReader fromServer;	private String userName=null;	private ArrayList<String> userList;	private final int MESSAGE_LIMIT = 512;		public ChatScreen() throws IOException	{				userName = JOptionPane.showInputDialog("Enter your username\n(Only letters and _)");		//some error checking for entering username		//so we can follow protocol		if (userName.length()>16)		{			JOptionPane.showMessageDialog(null, "User Name too long: max 16, you have " + userName.length());			System.exit(0);		}				if(!userName.matches("[a-zA-Z0-9_]+"))		{			JOptionPane.showMessageDialog(null, "User Name can only have letters numbers and underscores");			System.exit(0);		}				JPanel p = new JPanel();		Border etched = BorderFactory.createEtchedBorder();		Border titled = BorderFactory.createTitledBorder(etched,"Enter Message Here ...");		p.setBorder(titled);		/**		 * set up all the components		 */		sendText = new JTextField(30);		sendButton = new JButton("Send");		exitButton = new JButton("Exit");		/**		 * register the listeners for the different button clicks		 */		sendText.addKeyListener(this);		sendButton.addActionListener(this);		exitButton.addActionListener(this);		/**		 * add the components to the panel		 */		p.add(sendText);		p.add(sendButton);		p.add(exitButton);		/**		 * add the panel to the "south" end of the container		 */		getContentPane().add(p, "South");		/**		 * add the text area for displaying output. Associate a scrollbar with		 * this text area. Note we add the scrollpane to the container, not the		 * text area		 */		displayArea = new JTextArea(15, 40);		displayArea.setEditable(false);		displayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));		JScrollPane scrollPane = new JScrollPane(displayArea);		getContentPane().add(scrollPane, "Center");		DefaultCaret caret = (DefaultCaret)displayArea.getCaret();		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);				/**		 * display for the names on the chat room.		 */		nameArea = new JTextArea(15, 9);		nameArea.setEditable(false);		nameArea.setFont(new Font("SansSerif", Font.PLAIN, 12));		JScrollPane nameListPane = new JScrollPane(nameArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);		getContentPane().add(nameListPane, "East");				pack();		String accepted = null;		try		{			connection = new Socket(JOptionPane.showInputDialog("IP address to connect to"), 1337);			toServer = new DataOutputStream(connection.getOutputStream());			toServer.write(("0 "+ userName+ "\r\n").getBytes());			toServer.flush();			fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));						accepted = fromServer.readLine();						//catching if the username has been taken					if(accepted.charAt(0)=='2')			{					JOptionPane.showMessageDialog(null, "User Name Already Taken");				System.exit(0);			}			else if(accepted.charAt(0) != '1')			{				JOptionPane.showMessageDialog(null, "Unknown Error");				System.exit(0);			}						//if it passes, we set the username			String uList = accepted.substring(2,accepted.indexOf(" ",2));			userList = new ArrayList<String>(Arrays.asList(uList.split(",")));			userList.remove(userName);						this.setTitle(userName);		}		catch (Exception e)		{			e.printStackTrace();		}				ChatInputListener l = new ChatInputListener(connection, this);		Thread t = new Thread(l);		t.start();				//Set name of chat to user name		setTitle(userName+"'s chat room");		setVisible(true);		sendText.requestFocus();				//Add user to list of users		nameArea.append("You: "+userName+"\n");		updateNameList();		/** anonymous inner class to handle window closing events */		addWindowListener(new WindowAdapter()		{			public void windowClosing(WindowEvent evt)			{				sendDisconnectRequest();				System.exit(0);			}		});		displayText(accepted.substring(accepted.indexOf(" ", 2), accepted.length()));	}	/**	 * This gets the text the user entered and outputs it in the display area.	 */	public void displayText()	{				//Check to make sure message fits protocol		String message = sendText.getText().trim();		if(message.contains("\n")||message.contains("\r"))			return;		if(message.length()>MESSAGE_LIMIT)		{			displayArea.append("you have too many characters\n");		    sendText.setText(message.substring(0,MESSAGE_LIMIT+1));			return;		}		if(message.length()==0)			return;		try		{			 /**			 * Private message:			 * <4><" "><fromUsername><" "><toUsername><" "><message></r/n>			 */			if(message.charAt(0)=='\\'&& message.charAt(1)=='w')			{				if(message.length()>2)				{					toServer.write(("4 "+userName+" "+message.substring(2, message.indexOf(" ",2))+" "+message.substring(message.indexOf(" ")+1) + "\r\n").getBytes());				}				else				{					displayArea.append("Can't send empty whisper");					return;				}			}			else			{				toServer.write(("3 "+message + "\r\n").getBytes());			}						toServer.flush();		}		catch (IOException j)		{			j.printStackTrace();		}		sendText.setText("");		sendText.requestFocus();	}		/**	 * Called from separate input thread.	 * Appends message received from server to display	 * @param Message received from server	 */	public void displayText(String message)	{		displayArea.append(message + "\n");	}	/**	 * This method responds to action events .... i.e. button clicks and	 * fulfills the contract of the ActionListener interface.	 */	public void actionPerformed(ActionEvent evt)	{		Object source = evt.getSource();		if (source != exitButton)			displayText();		else		{			//send disconnect message			sendDisconnectRequest();			System.exit(0);		}	}	/**	 * These methods responds to keystroke events and fulfills the contract of	 * the KeyListener interface.	 */	/**	 * This is invoked when the user presses the ENTER key.	 */	public void keyPressed(KeyEvent e)	{		if (e.getKeyCode() == KeyEvent.VK_ENTER)			displayText();	}	/** Not implemented */	public void keyReleased(KeyEvent e)	{	}	/** Not implemented */	public void keyTyped(KeyEvent e)	{	}	public static void main(String[] args)	{		try		{			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());			JFrame chatScreenWindow = new ChatScreen();		}		catch (Exception e)		{			System.err.println("FATAL EEEEEEEEERROR");		}	}		//updates name list for any changes	private void updateNameList()	{		nameArea.setText("");		nameArea.append("You: "+userName+"\n");		Iterator<String> i = userList.iterator();		while(i.hasNext())			nameArea.append(i.next()+"\n");		}		public void removeFromNameList(String user)	{		userList.remove(user);		updateNameList();	}		public void addToNameList(String user)	{		userList.add(user);		updateNameList();	}		private void sendDisconnectRequest()	{		System.out.println("Client disconnecting");		try		{			toServer.write(("7\r\n").getBytes());		}		catch (IOException e)		{			e.printStackTrace();		}	}}