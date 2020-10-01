//Name: Shashank Shekhar
//UTA ID - 1001767592
//REF: https://github.com/sanchitjain1993/ClientServerApp

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import java.net.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;

public class ChatServer {

	// UI Declarations
	static JTextArea serverLog;
	static JFrame serverWindow;
	static JScrollPane scrollPane;
	static JButton online = new JButton("Show online Users");
	static JButton offline = new JButton("Show offline Users");
	static ArrayList<String> userNames = new ArrayList<String>();
	static ArrayList<String> offlineUserNames = new ArrayList<String>();

	static ArrayList<PrintWriter> printWriters = new ArrayList<PrintWriter>();
	static Map<String, PrintWriter> nameWriter = new HashMap();

	public static void main(String[] args) throws Exception {

		JFrame serverWindow = null;
		serverWindow = new JFrame("Chat Server");
		serverWindow.setSize(650, 400);
		serverWindow.setLayout(new FlowLayout());
		serverWindow.setResizable(false);
		serverLog = new JTextArea(20, 50);

		serverLog.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(serverLog);
		serverWindow.add(scrollPane);
		serverWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		serverWindow.setVisible(true);
		serverWindow.add(online);
		serverWindow.add(offline);

		System.out.println("Waiting for clients...");

		ServerSocket ss = new ServerSocket(9806);
		serverLog.append("Server started on port 9806" + "\n\n");
		//this will show all the online users when button is pressed
		online.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Online Users: ";
				for (String users : userNames) {
					message += "\n" + users;
				}
				JOptionPane.showMessageDialog(ChatServer.serverWindow, message);
			}
		});

		//this will show all the users who went offline 
		offline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Offline Users: ";
				for (String users : offlineUserNames) {
					message += "\n" + users;
				}
				JOptionPane.showMessageDialog(ChatServer.serverWindow, message);
			}
		});

		while (true)

		{

			Socket soc = ss.accept();

			System.out.println("Connection established");

			//spawning a new thread for each user
			ConversationHandler handler = new ConversationHandler(soc);

			handler.start();

		}

	}

}

class ConversationHandler extends Thread

{

	Socket socket;

	BufferedReader in;

	PrintWriter out;

	String name;

	public ConversationHandler(Socket socket) throws IOException {

		this.socket = socket;
	}

	public void run()

	{

		try

		{
			//input from client
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			//output to client
			out = new PrintWriter(socket.getOutputStream(), true);

			int count = 0;

			while (true)

			{

				if (count > 0)

				{

					out.println("NAMEALREADYEXISTS");

				}

				else

				{

					out.println("NAMEREQUIRED");

				}

				name = in.readLine();

				if (name == null)

				{

					return;

				}

				if (!ChatServer.userNames.contains(name))

				{

					ChatServer.userNames.add(name);
					ChatServer.offlineUserNames.remove(name);

					break;

				}

				count++;

			}

			out.println("NAMEACCEPTED" + name);
			// if valid username is entered server will display the message
			ChatServer.serverLog.append(name + " joined" + "\n");

			//adding the user to writers list
			ChatServer.printWriters.add(out);
			//adding the user writer to map
			ChatServer.nameWriter.put(name, out);

			String activeNames = "";
			for (String name1 : ChatServer.userNames) {
				activeNames += name1 + ",";
			}
			// Update all clients about the newly connected client
			for (PrintWriter writer : ChatServer.printWriters) {
				writer.println("USERLIST " + activeNames);
			}

			//update the new client about all online users
			for (PrintWriter writer : ChatServer.printWriters) {
				writer.println("USERJOINED" + name);
			}
			String homeDirectory=System.getProperty("user.dir");
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			File file = new File(homeDirectory+"\\"+name);
		      //Creating the directory
		      boolean bool = file.mkdir();
		      if(bool){
		         System.out.println("Directory created successfully");
		      }else{
		         System.out.println("Directory already created");
		      }

			while (true)

			{

				String message = in.readLine();

				if (message == null)

				{
					return;

				}
				//if user leaves the chatroom
				else if (message.startsWith("LEFT")) {
					//remove user from user list
					ChatServer.userNames.remove(message.substring(4));
					//add user to offline user list
					ChatServer.offlineUserNames.add(name);
					ChatServer.nameWriter.remove(message.substring(4));
					
					String activeNames2 = "";
					for (String name1 : ChatServer.userNames) {
						activeNames2 += name1 + ",";
					}

					// Update all clients about the current active users
					for (PrintWriter writer : ChatServer.printWriters) {
						writer.println("USERLIST " + activeNames2);
					}
					//update the clients about the user which left the chatroom
					for (PrintWriter writer : ChatServer.printWriters) {
						writer.println("USERLEFT" + name);
					}
					message = message.substring(4) + " left";
					ChatServer.serverLog.append(name + " left" + "\n");
					break;
				}
				//display all users log to  new connected client
				else if (message.startsWith("REQUEST")) {

					String activeNames2 = "";
					for (String name1 : ChatServer.userNames) {
						activeNames2 += name1 + ",";
					}

					PrintWriter out2 = ChatServer.nameWriter.get(name);
					out2.println("USERSLOG" + activeNames2);

				}
				
				else if (message.startsWith("CREATE")) {

					String str[]=message.split(":");
					String createPath= str[1];
					File file1 = new File(homeDirectory+"\\"+createPath);
					PrintWriter out2 = ChatServer.nameWriter.get(name);
					boolean isCreate=false;
					if(file1.exists()) {
						out2.println("CREATE:" + createPath +" already exists");
					}
					else {
						//Creating the directory
					      isCreate = file1.mkdirs();
					      if(isCreate) {
						     out2.println("CREATE:" + createPath +" created successfully");
					      }else{
					         out2.println("CREATE:" + createPath + " error creating directory");
					      }
					}
	
				}
				
				else if (message.startsWith("DELETE")) {

					String str[]=message.split(":");
					String deletePath= str[1];
					File file1 = new File(homeDirectory+"\\"+deletePath);
					PrintWriter out2 = ChatServer.nameWriter.get(name);
					try {
						FileUtils.deleteDirectory(file1);
						out2.println("DELETE:" + deletePath +" deleted successfully");
					} catch (Exception e) {
						out2.println("DELETE:" + deletePath +" deletion failed due to "+e.getMessage());
					}
				     
				}
				
				else if (message.startsWith("RENAME")) {

					String str[]=message.split(":");
					String sourcePath= str[1];
					String targetPath = str[2];
					PrintWriter out2 = ChatServer.nameWriter.get(name);
					try {
						File file1 = new File(homeDirectory+"\\"+sourcePath);
						File file2 = new File(homeDirectory+"\\"+targetPath);
						 FileUtils.moveDirectory(FileUtils.getFile(file1), FileUtils.getFile(file2));
						out2.println("RENAME:" + sourcePath +" renamed successfully to "+targetPath);
					} catch (Exception e) {
						out2.println("RENAME:" +e.getMessage());
					}
				     
				}
				
				else if (message.startsWith("MOVE")) {

					String str[]=message.split(":");
					String sourcePath= str[1];
					String targetPath = str[2];
					PrintWriter out2 = ChatServer.nameWriter.get(name);
					try {
						File file1 = new File(homeDirectory+"\\"+sourcePath);
						File file2 = new File(homeDirectory+"\\"+targetPath+"\\");
						 FileUtils.moveDirectoryToDirectory(FileUtils.getFile(file1), FileUtils.getFile(file2),true);
						out2.println("MOVE:" + sourcePath +" moving successfully to "+targetPath);
					} catch (Exception e) {
						out2.println("MOVEE:" +e.getMessage());
					}
				     
				}


			}

		}

		catch (Exception e)

		{

			System.out.println(e);

		}

	}

}
