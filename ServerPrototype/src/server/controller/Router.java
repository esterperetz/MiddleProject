package server.controller;

import Entities.Request;
import Entities.ResourceType;
import ocsf.server.ConnectionToClient;

public class Router {

    private final OrderController orderController;
    // later: private final UserController userController;
    // later: private final WaitingListController waitingListController;

    public Router(OrderController orderController) {
        this.orderController = orderController;
        // when you have more controllers, add them to ctor as params
        // this.userController = userController;
        // this.waitingListController = waitingListController;
    }

    public void route(Request request, ConnectionToClient client) {
        ResourceType resource = request.getResource();

        switch (resource) {
            case ORDER:
                orderController.handle(request, client);
                break;

            // case USER:
            //     userController.handle(request, client);
            //     break;

            // case WAITING_LIST:
            //     waitingListController.handle(request, client);
            //     break;

            default:
                System.out.println("Unknown resource: " + resource);
        }
    }
}
