package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entities.Request;
import entities.ResourceType;
import ocsf.server.ConnectionToClient;

/**
 * The Router class acts as a central dispatcher for incoming client requests.
 * It analyzes the resource type of the request and delegates the processing
 * to the appropriate specific controller (e.g., OrderController, TableController).
 * It also manages the list of connected clients for broadcasting messages.
 */
public class Router {

    private final OrderController orderController;
    private final CustomerController customerController;
    private final TableController tableController;
    private final WaitingListController waitingListController;
    private final BusinessHourController businessHourController;
    private final EmployeeController employeeController;
    private final ReportController reportController;
    private final MyFileController myFileController;
    
    // List to hold all active client connections
    private static List<ConnectionToClient> clients;

    /**
     * Constructor: Initializes all the sub-controllers used for handling requests.
     * Also initializes the static list of clients if it hasn't been created yet.
     */
    public Router() {
        this.orderController = new OrderController();
        this.customerController = new CustomerController();
        this.tableController = new TableController();
        this.waitingListController = new WaitingListController();
        this.businessHourController = new BusinessHourController();
        this.employeeController = new EmployeeController();
        this.reportController = new ReportController();
        this.myFileController = new MyFileController();

        if (clients == null) {
            clients = new ArrayList<>();
        }
    }

    /**
     * Routes the incoming request to the appropriate controller based on the ResourceType.
     *
     * @param req    The request object containing the resource type and action.
     * @param client The connection to the client sending the request.
     * @throws IOException  If an I/O error occurs during response.
     * @throws SQLException If a database error occurs during processing.
     */
    public void route(Request req, ConnectionToClient client) throws IOException, SQLException {
        ResourceType resource = req.getResource();

        switch (resource) {
            case ORDER:
                orderController.handle(req, client, clients);
                break;

            case CUSTOMER:
                customerController.handle(req, client);
                break;

            case TABLE:
                tableController.handle(req, client);
                break;

            case WAITING_LIST:
                waitingListController.handle(req, client);
                break;
                
            case BUSINESS_HOUR:
                businessHourController.handle(req, client);
                break;
                
            case EMPLOYEE:
                employeeController.handle(req, client);
                break;
                
            case REPORT:
                reportController.handle(req, client);
                break;
                
            case REPORT_MONTHLY:
                myFileController.handle(req, client);
                break;

            default:
                client.sendToClient("Unknown resource type: " + resource);
        }
    }

    /**
     * Returns the list of currently connected clients.
     *
     * @return List of ConnectionToClient objects.
     */
    public List<ConnectionToClient> getClients() {
        return clients;
    }

    /**
     * Sets the list of connected clients.
     *
     * @param clientsList The new list of clients.
     */
    public void setClients(List<ConnectionToClient> clientsList) {
        clients = clientsList;
    }

    /**
     * Adds a new client connection to the active clients list.
     * Called when a client successfully connects.
     *
     * @param client The client connection to add.
     */
    public void addClientOnline(ConnectionToClient client) {
        clients.add(client);
    }

    /**
     * Removes a client connection from the active clients list.
     * Called when a client disconnects.
     *
     * @param client The client connection to remove.
     */
    public void removeClientOffline(ConnectionToClient client) {
        clients.remove(client);
    }

    /**
     * Broadcasts a message to all currently connected and alive clients.
     * This method is synchronized to prevent concurrent modification issues.
     *
     * @param message The message object to send.
     */
    public static synchronized void sendToAllClients(Object message) {
        if (clients != null) {
            for (ConnectionToClient c : clients) {
                try {
                    if (c.isAlive()) {
                        c.sendToClient(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}