package server.controller;

import java.io.IOException;

import Entities.Request;
import Entities.ResourceType;
import ocsf.server.ConnectionToClient;

public class Router {

    private final OrderController orderController;
    // private final UserController userController; 
    // private final WaitingListController waitingListController; ו

    public Router() {
    	
        this.orderController = new OrderController();
        // this.userController = new UserController();
        // this.waitingListController = new WaitingListController();
    }

    public void route(Request req, ConnectionToClient client) throws IOException {
        ResourceType resource = req.getResource(); 

        switch (resource) {
            case ORDER:
                orderController.handle(req, client);
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
}