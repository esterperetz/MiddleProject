package server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Entities.Request;
import Entities.ResourceType;
import ocsf.server.ConnectionToClient;

public class Router {

    private final OrderController orderController;
    private List<ConnectionToClient> clients;
    // private final UserController userController; 
    // private final WaitingListController waitingListController; ו

    public Router() {
    	
        this.orderController = new OrderController();
        clients=new ArrayList<>();//list of our online clients
        
        // this.userController = new UserController();
        // this.waitingListController = new WaitingListController();
    }

    

	
	 //Routes an incoming Request to the appropriate controller

	public void route(Request req, ConnectionToClient client) throws IOException {
        ResourceType resource = req.getResource(); 

        switch (resource) {
            case ORDER:
                orderController.handle(req, client,clients);
                break;

            case USER:
                // userController.handle(req, client);
                break;

            case WAITING_LIST:
                // waitingListController.handle(req, client);
                break;

            default:
            	//if unknown
                client.sendToClient("Unknown resource type: " + resource);
        }
    }
	
     //Returns the current list of connected (online) clients.
	public List<ConnectionToClient> getClients() {
		return clients;
	}
	
     //Replaces the current list of connected clients with a new list.
	public void setClients(List<ConnectionToClient> clients) {
		this.clients = clients;
	}

	public void addClientOnline(ConnectionToClient client) {
		this.clients.add(client);
	}
	
	public void removeClientOffline(ConnectionToClient client) {
		this.clients.remove(client) ;
	}
	
}