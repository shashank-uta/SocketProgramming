//Name: Shashank Shekhar
//UTA ID - 1001767592
//REF: https://github.com/sanchitjain1993/ClientServerApp

import javax.swing.*;

import com.google.common.io.Files;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.io.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatClient {

	//String to hold username
	String username;

	//HashSet to hold list of uniques usernames
	volatile static Set<String> userNames = new HashSet();

	//UI declarations
	static JFrame chatWindow = new JFrame("Chat Application");

	
	static JTextArea chatArea = new JTextArea(22, 40);


	static JLabel blankLabel = new JLabel("           ");

	static JButton delete = new JButton("Delete");
	
	static JButton createDirectory = new JButton("Create");
	
	static JButton rename = new JButton("Rename");
	
	static JButton move = new JButton("Move");
	
	static JButton list = new JButton("List Directories");

	//input from server
	static BufferedReader in;

	//output to server
	static PrintWriter out;

	static JLabel nameLabel = new JLabel("         ");

	static JButton close = new JButton("Close");

	ChatClient()

	{

	//initializing all gui components
		chatWindow.setLayout(new FlowLayout());

		chatWindow.add(nameLabel);

		chatWindow.add(new JScrollPane(chatArea));

		chatWindow.add(blankLabel);

		
		chatWindow.add(createDirectory);
		
		chatWindow.add(delete);
		
		chatWindow.add(rename);
		
		chatWindow.add(move);
		
		chatWindow.add(list);

		chatWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		chatWindow.setSize(475, 500);

		chatWindow.setVisible(true);

		chatWindow.add(close, BorderLayout.SOUTH);


		chatArea.setEditable(false);
		

	}

	void startChat() throws Exception

	{

		//ip address hardcoded to localhost
		Socket soc = new Socket("localhost", 9806);

		//input stream from server
		in = new BufferedReader(new InputStreamReader(soc.getInputStream()));

		//output to server
		out = new PrintWriter(soc.getOutputStream(), true);
		
		// REF:https://www.tutorialspoint.com/how-to-add-action-listener-to-jbutton-in-java
		//action listener for close button
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println("LEFT" + username);
				userNames.remove(username);
				System.exit(0);
			}
		});
		while (true)

		{
			
			String str = in.readLine();

			if (str.equals("NAMEREQUIRED"))

				
			{
				while(true){
					String name = JOptionPane.showInputDialog(

							chatWindow,

							"Enter a valid username:",

							"Name Required!!",

							JOptionPane.PLAIN_MESSAGE);
					//regex validation for username
					if(name.matches("^[a-zA-Z0-9]{4,10}$")) {
						System.out.println("valid name");
						out.println(name);
						break;
					}
				}


			}

			//if unique name is not entered
			else if (str.equals("NAMEALREADYEXISTS"))

			{

				String name = JOptionPane.showInputDialog(

						chatWindow,

						"Enter another name:",

						"Name Already Exits!!",

						JOptionPane.WARNING_MESSAGE);

				out.println(name);

			}

			//when user enters valid username
			else if (str.startsWith("NAMEACCEPTED"))

			{

				nameLabel.setText("You are logged in as: " + str.substring(12));
				this.username = str.substring(12);
				//request to show logs of active users to client
				createDirectory.addActionListener(new CreateListener(this.username));
				delete.addActionListener(new DeleteListener(this.username));
				rename.addActionListener(new RenameListener(this.username));
				move.addActionListener(new MoveListener(this.username));
				list.addActionListener(new ListListener(this.username));
				getActiveUsersList();

			}

			//message from server with create			
			else if (str.startsWith("CREATE")) {
				String strArr[]= str.split(":");
				String message = strArr[1];
				chatArea.append(message + "\n");
			}
			
			//message from server with delete
			else if (str.startsWith("DELETE")) {
				String strArr[]= str.split(":");
				String message = strArr[1];
				chatArea.append(message + "\n");
			}
			
			//message from server with rename
			else if (str.startsWith("RENAME")) {
				String strArr[]= str.split(":");
				String message="";
				for(int i=1;i<strArr.length;i++) {
					message = message+strArr[i];
				}
				
				System.out.println(message);
				chatArea.append(message + "\n");
			}
			
			//message from server with move
			else if (str.startsWith("MOVE")) {
				String strArr[]= str.split(":");
				String message="";
				for(int i=1;i<strArr.length;i++) {
					message = message+strArr[i];
				}
				
				System.out.println(message);
				chatArea.append(message + "\n");
			}




		}

	}

	public static void getActiveUsersList() throws IOException {
		out.println("REQUESTUSERS");
		String list = in.readLine();

	}

	public static void main(String[] args) throws Exception {

		// TODO Auto-generated method stub

		ChatClient client = new ChatClient();

		client.startChat();

	}

}
//Create listener
class CreateListener implements ActionListener {

	String username;
	

	public CreateListener(String username) {

		this.username = username;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//get directory path from username
		String path = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter directory name to create inside "+ username,

				"Directory name required!!",

				JOptionPane.PLAIN_MESSAGE);

		//send to server for directory creation
		ChatClient.out.println("CREATE:"+username+"\\"+path);
		
	}
	
}


//delete listener
class DeleteListener implements ActionListener {

	String username;
	

	public DeleteListener(String username) {

		this.username = username;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//get directory path from username to delete
		String path = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter directory name with path to be deleted inside "+ username,

				"Directory path required!!",

				JOptionPane.PLAIN_MESSAGE);

		ChatClient.out.println("DELETE:"+username+"\\"+path);
		
	}
	
}

//Rename listener
class RenameListener implements ActionListener {

	String username;
	

	public RenameListener(String username) {

		this.username = username;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//get directory to rename
		String sourcePath = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter directory name with path to be renamed inside "+ username,

				"Directory path required!!",

				JOptionPane.PLAIN_MESSAGE);
		
		//get directory new name
		String targetPath = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter new directory name with path to be renamed inside "+ username,

				"Directory path required!!",

				JOptionPane.PLAIN_MESSAGE);

		ChatClient.out.println("RENAME:"+username+"\\"+sourcePath+":"+username+"\\"+targetPath);
		
	}
	
}

//Move Listener
class MoveListener implements ActionListener {

	String username;
	

	public MoveListener(String username) {

		this.username = username;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//get source path from user
		String sourcePath = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter directory name with path to be moved inside "+ username,

				"Directory path required!!",

				JOptionPane.PLAIN_MESSAGE);
		
		//get target path from user
		String targetPath = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter new directory name with path to be moved inside "+ username,

				"Directory path required!!",

				JOptionPane.PLAIN_MESSAGE);

		ChatClient.out.println("MOVE:"+username+"\\"+sourcePath+":"+username+"\\"+targetPath);
		
	}
	
}



//List listener
class ListListener implements ActionListener {

	String username;
	

	public ListListener(String username) {

		this.username = username;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//get directory path from username
		String path = JOptionPane.showInputDialog(

				ChatClient.chatWindow,

				"Enter directory name with path inside "+username+" to list its content",

				"Directory name required!!",

				JOptionPane.PLAIN_MESSAGE);

		String homeDirectory=System.getProperty("user.dir");
		String finalPath=homeDirectory+"\\"+username+"\\"+path;
		
		File file1 = new File(finalPath);
		
		try {
			ChatClient.chatArea.append("Listing directories at "+path+"\n");
			for (File file: Files.fileTraverser().breadthFirst(file1))
			{
				ChatClient.chatArea.append(file.getAbsolutePath()+"\n");	
			}
		} catch (Exception e1) {
			ChatClient.chatArea.append(e1.getMessage()+"\n");
		}
		
		
	}
	
}


