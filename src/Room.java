import java.util.List;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * This is a chat room. It has two class members: a room name
 * and a list of users currently in the chat room
 * 
 * 			By:		Kim Nguyen
 * 			Date:	Jul, 22th 2016 
 */

public class Room {	
	private String roomName;				// room name
	private Map<String,ClientThread8> roomUsers;	// key is user name, value is thread handling the user
	
	public Room(String roomName) {
		this.roomName = roomName;
		roomUsers = new HashMap<String,ClientThread8>();
	}
	
	// post a message to all users, except one user
	public void postToAllExceptOne(String message, String userName) {
		synchronized (roomUsers) {				
			for (Map.Entry<String, ClientThread8> entry: roomUsers.entrySet()) {
				String name = entry.getKey();
				ClientThread8 thread = entry.getValue();
				if ( ! name.equals(userName) )
					thread.getPrintWriter().print(message);
					thread.getPrintWriter().flush();;
			}
		}
			
	}
	
	// post a message to all users in the room
	public void postToAll(String message) {
		synchronized (roomUsers) {
			Collection<ClientThread8> threads =  roomUsers.values();	
			for (ClientThread8 thread : threads) {
				thread.getPrintWriter().print(message);
				thread.getPrintWriter().flush();
			}
		}
		
	}
	
	// add a user to chat room
	// receives:
	// 	- user name
	// 	- the thread handling the user
	public Set <String> addUser(String userName, 
								ClientThread8 clientThread) {
		synchronized (roomUsers) {
			roomUsers.put(userName, clientThread);
			return roomUsers.keySet();			
		}		
	}
	
	// remove a user from chat room
	// receive:
	// 	- user name
	// return:
	//	- true: if userName is in list
	//	- false: if userName is not in list
	public boolean removeUser(String userName) {
		synchronized (roomUsers) {
			if (roomUsers.containsKey(userName)) {
				roomUsers.remove(userName);
				return true;
			}
		}
		return false;
	}
	
	// return number of users in the room
	public int getRoomSize() {		
		synchronized (roomUsers) {
			return roomUsers.size();			
		}		
	}	
	
	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public Map<String, ClientThread8> getUsers() {
		return roomUsers;
	}

	public void setUsers(Map<String, ClientThread8> users) {
		this.roomUsers = roomUsers;
	}


}
