package server.controller;

import java.io.IOException;


import DBConnection.DBConnection;
import entities.Request;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.gui.ServerViewController;

public class ServerController extends AbstractServer {

    private final ServerViewController view;
    private final Router router;
    private final OrderCleanupThread cleanupThread;
    private final WaitingListCheckThread waitingListThread;
    
    public ServerController(int port, ServerViewController view) {
        super(port);
        this.view = view;
        this.router = new Router();
        view.setServerController(this);
        this.cleanupThread = new OrderCleanupThread(); 
        this.waitingListThread = new WaitingListCheckThread();
        this.cleanupThread.start(); 
        this.waitingListThread.start();
        view.log("Background threads (Cleanup & WaitingList) initialized and started.");
    }

    
    /**
     * Called when a client connects to the server.
     * Logs the connection and updates the GUI table via ServerViewController.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        super.clientConnected(client);
        String ip = client.getInetAddress().getHostAddress();
        
        String host = client.getInetAddress().getHostName();
        
        view.log("Client connected: " + ip + " / " + host);
        view.addClient(ip, host); // Update the table
        router.addClientOnline(client);
    }

    /**
     * Called when a client disconnects from the server.
     * Logs the disconnection and updates the GUI table via ServerViewController.
     */
    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
    	try {
    		String ip = client.getInetAddress().getHostAddress();
            view.log("Client disconnected: " + ip);
            view.removeClient(ip); // Update the table
            router.removeClientOffline(client);
            super.clientDisconnected(client);//move to end
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        
        

    }

    /**
     * Called when an exception occurs on the client's connection.
     * Ensures the client is removed from the list.
     */
    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
    	try {
        view.log("Client exception: " + client.getInetAddress().getHostAddress() + " - " + exception.getMessage());
        clientDisconnected(client); // Ensure cleanup
    	}
    	catch(Exception e) {};
    }
    
    /**
     * cleans the DBconections before closing the serverApp
     */
    @Override
    public void serverClosed() {
    	 /// need to send all clients from here
        Router.sendToAllClients("quit");
        DBConnection.getInstance().closeConnection();
       
        view.log("Server closed and single persistent DB connection closed.");
    }
    
    
    /**
     * Server receive Object/Request from client and send to router
     */
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        try {
        	
            if (msg instanceof String && "quit".equals(msg)) {
                clientDisconnected(client);
                return;
            }

            if (!(msg instanceof Request)) {
                System.err.println("Unsupported message type: " + (msg != null ? msg.getClass() : "null"));
                return;
            }

            Request request = (Request) msg;
            router.route(request, client);

        } catch (Exception e) {
            e.printStackTrace();
            view.log("Error processing message from client: " + e.getMessage());
            try {
                client.sendToClient("Error: " + e.getMessage());
            } catch (Exception ex) {
            	view.log("failed send to client.");
            }
        }
    }

}