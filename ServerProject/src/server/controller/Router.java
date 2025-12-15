package server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Entities.Request;
import Entities.ResourceType;
import ocsf.server.ConnectionToClient;

public class Router {

    private final OrderController orderController;
    private final SubscriberController subscriberController;
    
    private static List<ConnectionToClient> clients; 

    public Router() {
        this.orderController = new OrderController();
        this.subscriberController = new SubscriberController();
        
        if (clients == null) {
            clients = new ArrayList<>(); 
        }
    }

    public void route(Request req, ConnectionToClient client) throws IOException {
        ResourceType resource = req.getResource(); 

        switch (resource) {
            case ORDER:
                orderController.handle(req, client, clients);
                break;

            case SUBSCRIBER: // Handles all registered entities: Subscribers, Workers...
                subscriberController.handle(req, client);
                break;

            case WAITING_LIST:
                // waitingListController.handle(req, client);
                break;

            default:
                client.sendToClient("Unknown resource type: " + resource);
        }
    }
    
    public List<ConnectionToClient> getClients() {
        return clients;
    }
    
    public void setClients(List<ConnectionToClient> clientsList) {
        clients = clientsList; // בלי this כי זה משתנה סטטי
    }
    
    public void addClientOnline(ConnectionToClient client) {
        clients.add(client);
    }
    
    public void removeClientOffline(ConnectionToClient client) {
        clients.remove(client);
    }


    public static void sendToAllClients(Object message) {
        if (clients != null) {
            for (ConnectionToClient c : clients) {
                try {
                    c.sendToClient(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}