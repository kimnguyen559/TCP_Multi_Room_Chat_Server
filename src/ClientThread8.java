import java.util.List;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This is a thread that takes over a client from the main server 
 * thread. It takes care of the client until client disconnects and
 * exits.  
 * 
 * User can log in a system with a login name then join one of the
 * two chat rooms.  Then user can start the chat.
 * 
 * When a user enters a chat room, the thread announces them to other 
 * users in the room.
 *
 * While a user is in a chat room, as soon as the thread receives a message 
 * from them, it echoes back to others in the room.
 *
 * When a user quits a chat room, the thread announces the user’s departure.
 * It then disconnects the client and exits.
 * 
 * 			By:		Kim Nguyen
 * 			Date:	Jul, 22th 2016 
 */

public class ClientThread8 extends Thread {
	private final static String WELCOME_MSG = "<= Welcome to the XYZ chat server.\r\n";
	private final static String NAME_PROMPT = "<= Login Name?\r\n";
	private final static String BYE_MSG = "<= BYE";	
	
	private String userName;					//	user the thread is serving
	private String roomName;					// 	chat room user's currently in
	
	private DataInputStream is;					// 	input stream from client
	private PrintStream os;						// output stream to client
	
	private BufferedReader br;					// input reader
    private PrintWriter pw;						// output writer
    
	private Socket clientSocket;				// socket to connect with client
	
	private Map<String,Room> rooms;				// list of chat rooms available
	private Set<String> users;					// list of all users

	public ClientThread8(Socket clientSocket, 
							Map<String,Room> rooms,
							Set<String> users) {
		this.clientSocket = clientSocket;
		this.rooms = rooms;
		this.users = users;
	}
	
	public PrintWriter getPrintWriter() {
		return pw;
	}
	
	public PrintStream getOutputStream() {
		return os;
	}

	public void run() {		
		try {			
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());		
			
			br = new BufferedReader(new InputStreamReader
										(clientSocket.getInputStream()));
		    pw = new PrintWriter(clientSocket.getOutputStream(), true);
			
			String input;		
			pw.print(WELCOME_MSG);	// send welcome message to user
			pw.flush();
			pw.print(NAME_PROMPT);	// prompt for a login name
			pw.flush();			
			
			while(true) {				// loop to read user's input				
				input = br.readLine().trim();
				
				if (input.startsWith("/rooms") ) {		// if client wants list of rooms
					getRooms();
				}
				
				else if (input.startsWith("/join")) {	// if client wants to enter a room
					joinChatRoom(input);
				}
				
				else if (input.startsWith("/leave") ) {	// if client wants to leave chat room
					leaveChatRoom();
				}
				
				else if (input.startsWith("/quit")) {	// if client wants to quit the system
					quit();
					break;
				}
				
				else {
					if (userName == null) {				// if client has not logged in yet
						logIn(input);
					}
					
					else if (roomName != null 				// if client is in a room and
								&& input.length() > 0 &&	// message is not empty and
								( ! input.startsWith("/"))) {	// it's not start with '/'
						postMessage(input);
					}					
				}
			}			
						
			is.close();					// close input stream
			os.close();					// close output stream
			
			pw.close();					// close output printer
			br.close();					// close input reader
			
			clientSocket.close();		// close socket
		} 
		catch (IOException e) {
		}		
	}
	
	// user post a message
	private void postMessage(String input) {
		String output = "<= " + userName + ": ";
		output += input+"\r\n";
		
		Room room = rooms.get(roomName);
		room.postToAll(output);			
	}
	
	// user logs in with a name
	private void logIn(String input) {
			String output = "";
			int result = validateUserName(input);
			
			if ( result != 0 ) {				// name is not good
				if (result == 1 ) {
					output = "<= Sorry, name cannot be empty.\r\n";
				}
				else if (result == 2 ) {
					output = "<= Sorry, allowed letters are [a-zA-Z0-1.-_].\r\n";
				}
				else {
					output = "<= Sorry, name taken.\r\n";
				}
				output += NAME_PROMPT;			
			}
			else {								// name is good
				synchronized (users) {													
					users.add(input);			// add user					
				}
				userName = input;			// set userName
				output = "<= Welcome " + userName + "!\r\n";			
			}
						
			pw.print(output);				// send message to user
			pw.flush();
		}
	
	// validate a user name, return:
	// - 1 for empty name
	// - 2 for name with illegal char
	// - 3 for duplicate name
	// - 0 for valid name
	private int validateUserName(String input) {	   
		if (input.length() == 0)	{						// empty name	   		
			return 1;
		}
		   	
		if ( ! input.matches("^[a-zA-Z0-9_.-]+$")) { 		// contains illegal char	   		
			return 2;
		}	   	
		   	
		synchronized (users){							// check existing user names
			if (users.contains(input)) {				// duplicate name	   			
		   		return 3;
		   	}
		}	   	 	
		return 0;										// name is good, return 0
	}	
	
	// send list of chat rooms to user
	private void getRooms() {
		String output = "<= Rooms:\r\n";
		for(Map.Entry<String, Room> entry : rooms.entrySet()) {	// loop thru each chat room in list
			String roomName = entry.getKey();					
			Room room = entry.getValue();
			output += "<= * " + roomName + " (" + room.getRoomSize() + ")\r\n";
		}
		output += "<= end of list.\r\n";		
		
		pw.print(output);				// send message to user
		pw.flush();
	}
	
	// place user in a chat room
	private void joinChatRoom(String input) {
		String output = "";
		if (userName == null) {					// if user has not logged in
			output = "<= You have not logged in yet.\r\n";
			output += NAME_PROMPT;						// promt for name
		}
		else if (roomName != null) {			// if user is already in a chat room
			output = "<= You're already in a chat room.\r\n";
		}
		else {											// user has logged in, but not in a chat room
			String[] words = input.split("\\s+");		// split input to get room name
			if (words.length < 2) {						// if no room name 
				output = "<= Room name missing.\r\n";
			}
			else {										// if there is a room name
				String name = words[1];
				if ( ! rooms.containsKey(name))			// if room name is not valide 
					output = "<= Room not exist.\r\n";
				else {
					Room room = rooms.get(name);			// get the room client wants
					Set<String> roomUsers = (Set<String>) 	// add client to the room
											room.addUser(userName, this);										
					
					output = "<= entering room: " + name+"\r\n";
					for (String user : roomUsers) {
						output += "<= * " + user + "\r\n";
					}
					output += "<= end of list.\r\n";
					roomName = name;
					
					String message = "<= * new user joined " + 
										roomName+": " + userName +"\r\n";
					room.postToAllExceptOne(message, userName);
				}					
			}			
		}		
		
		pw.print(output);				// send message to user
		pw.flush();
	}
	
	// user leaves chat room
	private void leaveChatRoom() {
		String output = "";
		if (userName == null) {					// if user has not logged in
			output = "<= You have not logged in yet.\r\n";
			output += NAME_PROMPT;				// prompt for name
		}
		
		else if (roomName == null) {			// if user is not in a chat room
			output = "<= You're not in a chat room.\r\n";
		}
			
		else {									// user has logged in, but not in a chat room
			Room room = rooms.get(roomName);
			String message = "<= * user has left " + 
								roomName+": " + userName+"\r\n";
			room.postToAll(message);
			room.removeUser(userName);
			roomName = null;						
		}
		
		pw.print(output);				// send message to user
		pw.flush();
	}
	
	// user exits the system
	private void quit() {		
		if (userName != null) {				// if user has logged in			
			if (roomName != null)			// if user in a chat room
				leaveChatRoom();			// check out the chat room
			
			synchronized (users) {			// remove user from list
				users.remove(userName);
			}
		}
		String output = BYE_MSG;
		
		pw.print(output);				// send message to user
		pw.flush();
	}
}
