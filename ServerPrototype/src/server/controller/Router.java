package server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Entities.Request;
import Entities.ResourceType;
import ocsf.server.ConnectionToClient;

/**
 * * Router is responsible for delegating incoming client requests
 * to the appropriate controller based on the requested resource type.
 *
 * Responsibilities:
 *  - Maintain a list of connected (online) clients.
 *  - Route Request objects to the relevant controller (e.g., OrderController).
 *  - Provide basic client-management utilities (add/remove online clients).
 */
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

    

	/**
	 * * Routes an incoming Request to the appropriate controller
     * according to the resource type specified in the Request.
     *
     * @param req    The incoming Request object sent by the client, containing
     *               the resource type, action, and any associated data.
     * @param client The specific client connection that sent this request.
     *
     * @throws IOException If sending a response back to the client fails.
     *
     * Behavior:
     *  - Reads the resource type from the Request (ORDER, USER, WAITING_LIST, etc.).
     *  - For ORDER, delegates handling to OrderController, passing also the
     *    list of connected clients so it can broadcast updates.
     *  - For unsupported / unimplemented resources, sends an error message
     *    back to the requesting client.
	 */
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
	/**
     * Returns the current list of connected (online) clients.
     *
     * @return List of ConnectionToClient objects representing all clients
     *         currently tracked by the Router.
     */
	public List<ConnectionToClient> getClients() {
		return clients;
	}
	/**
     * Replaces the current list of connected clients with a new list.
     *
     * @param clients A new List of ConnectionToClient to be used
     *                as the active client list.
     */
	public void setClients(List<ConnectionToClient> clients) {
		this.clients = clients;
	}
	/**
	 * @param client
	 * adding online client to the client list
	 */
	public void addClientOnline(ConnectionToClient client) {
		this.clients.add(client);
	}
	
	/**
	 * @param client
	 * removing offline client from the client list
	 */
	public void removeClientOffline(ConnectionToClient client) {
		this.clients.remove(client) ;
	}
	
}