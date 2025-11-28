package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import gui.ServerController;
//import CommonPrototype.Entites.*;
import ocsf.server.*;

/**
 * The class implements the Server side
 * sadddddddddddddddddddddddddddddddddddddd
 */
public class BistroServer extends AbstractServer {

	public BistroServer(int port) {
		super(port);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		// TODO Auto-generated method stub
		
	}

//	final public static int DEFAULT_PORT = 5555;
//	private MySQLConnection con;  // Will be used any time an SQL Query is needed
//	private List<ConnectionToClient> clientConnections = new ArrayList<>(); // Current connections
//	private List<List<String>> requiredList = new ArrayList<>(); // Log of current and former connections
//	private ServerController serverController;
//
//	/**
//	 * @param port
//	 * @param controller 
//	 * Constructor for the class, gets ServerController
//	 */
//	public BparkServer(ServerController controller) {
//
//		super(DEFAULT_PORT);
//		this.serverController = controller;
//		con = new MySQLConnection();
//		// TODO Auto-generated constructor stub
//	}
//
//	/**
//	 * @param msg
//	 * @param client 
//	 * Handles objects that are sent to the server
//	 */
//	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
//		if (msg instanceof Order) {
//			// An updated order from the client, checks if the change is valid and if so
//			// updates the DB
//			List<Order> orders;
//			orders = con.getallordersfromDB();
//			Order order = (Order) msg;
//			int spot = order.get_ParkingSpot().getSpotId();
//			String date = order.getorder_date().toString();
//			int index = -1;
//			for (int i = 0; i < orders.size(); i++) {
//				if (orders.get(i).get_order_id() == order.get_order_id())
//					index = i;
//				else if (orders.get(i).get_ParkingSpot().getSpotId() == spot
//						&& orders.get(i).getorder_date().toString().equals(date)) {
//					// Invalid order
//					System.out.println("can't place order!!!!!!!!!");
//					sendToSingleClient("can't place order!!!!!!!!!", client);
//					return;
//				}
//			}
//			// Valid order
//			con.updateDB(order);
//			orders.remove(index);
//			orders.add(index, order);
//			sendToAllClients(orders);
//			System.out.println("order placed");
//			sendToSingleClient("order placed", client);
//		} else if (msg instanceof String) {
//			String msgString = (String) msg;
//			System.out.println(msgString);
//			if (msgString.equals("Client disconnected")) // Note, make sure client sends a message before it disconnects
//				clientDisconnected(client);
//
//			if (msgString.startsWith("get_order: ")) {
//				String parts[] = msgString.split("get_order: ");
//				Order order = con.getOrderFromDB(parts[1]);
//				System.out.println("Retrieving an order...");
//				sendToSingleClient(order, client);
//			}
//		}
//	}
//
//	/**
//	 * @param msg
//	 * @param client 
//	 * Sends a object msg to a client
//	 */
//	public void sendToSingleClient(Object msg, ConnectionToClient client) {
//		try {
//			if (msg instanceof String) // Sends a String
//				client.sendToClient(msg);
//			else if (msg instanceof List) { // Sends a list of orders
//				List<Order> orderList = (List<Order>) msg;
//
//				client.sendToClient(orderList);
//				System.out.println(orderList);
//			} else if (msg instanceof Order) { // Sends one order
//				Order order = (Order) msg;
//				client.sendToClient(order);
//				System.out.println(order);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Prints to console that the server started
//	 */
//	protected void serverStarted() {
//		System.out.println(("Server listening for connections on port " + getPort()));
//	}
//
//	protected void serverStopped() {
//		// Honestly never used it...
//		System.out.println("Server has stopped listening for connections.");
//	}
//
//	/**
//	 * @param client 
//	 * Add the client to the list of connected clients Each client
//	 * has: id, IP, hostName, status{"Connected","Disconnected"} (all
//	 * strings)
//	 */
//	@Override
//	public void clientConnected(ConnectionToClient client) {
//		if (!clientConnections.contains(client)) {
//			clientConnections.add(client);
//			List<String> clientInfo = new ArrayList<>();
//			clientInfo.add(Long.toString(client.getId()));
//			clientInfo.add(client.getInetAddress().getHostAddress());
//			clientInfo.add(client.getInetAddress().getCanonicalHostName());
//			clientInfo.add("Connected");
//			requiredList.add(clientInfo);
//			serverController.recievedServerUpdate(requiredList);
//			sendToSingleClient(con.getallordersfromDB(), client);
//			// Log the connection
//			System.out.println(String.format("Client:%s IP:%s HostName:%s %s", clientInfo.get(0), clientInfo.get(1),
//					clientInfo.get(2), clientInfo.get(3)));
//
//		} else { // In case the same client tries to reconnect, may never be used
//			try {
//				clientSetStatus(client, "Connected");
//			} catch (Exception e) {
//				System.out.println("Connect failed!");
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	/**
//	 * @param client 
//	 * This method is called whenever a client disconnects Remove the
//	 * client from the list when they disconnect
//	 */
//	@Override
//	public void clientDisconnected(ConnectionToClient client) {
//		try {
//			clientSetStatus(client, "Disconnected");
//
//		} catch (Exception e) {
//			System.out.println("Disconnect failed!");
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * @param client
//	 * @param status
//	 * @throws Exception
//	 * Updates the status of a client to either "Connected"/"Disconnected"
//	 */
//	private void clientSetStatus(ConnectionToClient client, String status) throws Exception {
//		for (List<String> string : requiredList) {
//			if (string.get(0).equals(Long.toString(client.getId()))) {
//				string.set(3, status);
//				requiredList.set(requiredList.indexOf(string), string);
//				if (status.equals("Disconnected"))
//					clientConnections.remove(client);
//				else if (status.equals("Connected"))
//					clientConnections.add(client);
//				else {
//					throw new Exception();
//				}
//				serverController.recievedServerUpdate(requiredList);
//				// Log the disconnection
//				System.out.println(String.format("Client:%s IP:%s HostName:%s %s", string.get(0), string.get(1),
//						string.get(2), string.get(3)));
//				break;
//			}
//		}
//	}

}
