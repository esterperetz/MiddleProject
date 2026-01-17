package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import DAO.CustomerDAO;
import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Order;
import entities.Order.OrderStatus;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.WaitingList;
import ocsf.server.ConnectionToClient;

/**
 * Controller class responsible for handling all Waiting List related operations.
 * This includes viewing the list, adding customers to the waiting list, 
 * removing them, and promoting them to actual orders.
 */
public class WaitingListController {

    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Main handler for Waiting List requests. Dispatches the request to the appropriate 
     * private method based on the ActionType.
     *
     * @param req    The request object received from the client.
     * @param client The connection instance of the client sending the request.
     * @throws IOException If an I/O error occurs when sending a response.
     */
    public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.WAITING_LIST) {
            client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR,
                    "Incorrect resource type.", null));
            return;
        }

        try {
            switch (req.getAction()) {
                case GET_ALL:
                    handleGetAll(req, client);
                    break;
                    
                case GET_ALL_LIST:
                    handleGetAllListWithCustomer(req, client);
                    break;
                    
                case GET_WAITING_LIST_BY_DATE:
                    handleGetWaitingListByDate(req, client);
                    break;
                    
                case ENTER_WAITING_LIST:
                    handleEnterWaitingList(req, client);
                    break;

                case EXIT_WAITING_LIST:
                    handleExitWaitingList(req, client);
                    break;

                case PROMOTE_TO_ORDER:
                    handlePromoteToOrder(req.getId(), client);
                    break;

                default:
                    client.sendToClient(new Response(ResourceType.WAITING_LIST, req.getAction(),
                            Response.ResponseStatus.ERROR, "Unknown action", null));
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient(new Response(ResourceType.WAITING_LIST, req.getAction(),
                    Response.ResponseStatus.DATABASE_ERROR, e.getMessage(), null));
        }
    }

    /**
     * Retrieves all waiting list entries from the database.
     */
    private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
        List<WaitingList> list = waitingListDAO.getAllWaitingList();
        client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL, 
                Response.ResponseStatus.SUCCESS, null, list));
    }

    /**
     * Retrieves all waiting list entries joined with customer details (as a Map).
     * Also broadcasts the update to all connected clients.
     */
    private void handleGetAllListWithCustomer(Request req, ConnectionToClient client) throws SQLException, IOException {
        List<Map<String, Object>> list = waitingListDAO.getAllWaitingListWithCustomersToFilter();
        
        client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL_LIST,
                Response.ResponseStatus.SUCCESS, null, list));
        
        sendListToAllClients();
    }

    /**
     * Retrieves the waiting list filtered by a specific date.
     */
    private void handleGetWaitingListByDate(Request req, ConnectionToClient client) throws SQLException, IOException {
        java.sql.Date dateToFilter = (java.sql.Date) req.getPayload();
        List<Map<String, Object>> list = waitingListDAO.getWaitingListFromDate(dateToFilter);
        
        client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL_LIST, 
                Response.ResponseStatus.SUCCESS, null, list));
    }

    /**
     * Handles the process of entering a customer into the waiting list.
     * Steps:
     * 1. Identifies the customer (by Subscriber Code or Email).
     * 2. Creates a new customer if they are a guest.
     * 3. Checks if the customer is already waiting for this date.
     * 4. Creates a "Placeholder Order" (Pending status).
     * 5. Adds the entry to the Waiting List table.
     */
    private void handleEnterWaitingList(Request req, ConnectionToClient client) throws SQLException, IOException {
        WaitingList item = (WaitingList) req.getPayload();

        // Generate a random 4-digit confirmation code
        int generatedCode = 1000 + (int) (Math.random() * 9000);
        item.setConfirmationCode(generatedCode);
        item.setEnterTime(new Date());

        Customer finalCustomer = null;
        Integer subCode = item.getCustomer().getSubscriberCode();

        // Step 1: Identify Customer
        if (subCode != null && subCode > 0) {
            finalCustomer = customerDAO.getCustomerBySubscriberCode(subCode);
            if (finalCustomer == null) {
                client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
                        Response.ResponseStatus.ERROR, "Invalid Subscriber Code.", null));
                return;
            }
        } else {
            String email = item.getCustomer().getEmail();
            finalCustomer = customerDAO.getCustomerByEmail(email);

            if (finalCustomer == null) {
                // New Guest Customer
                Customer newGuest = item.getCustomer();
                newGuest.setType(CustomerType.REGULAR);
                customerDAO.createCustomer(newGuest);
                finalCustomer = customerDAO.getCustomerByEmail(email);
            }
        }

        if (finalCustomer == null || finalCustomer.getCustomerId() == null) {
            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
                    Response.ResponseStatus.DATABASE_ERROR, "Failed to identify customer.", null));
            return;
        }

        item.setCustomerId(finalCustomer.getCustomerId());
        item.setCustomer(finalCustomer);

        // Step 2: Check duplication
        if (waitingListDAO.isCustomerWaitingForDate(finalCustomer.getCustomerId(), item.getReservationDate())) {
            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
                    Response.ResponseStatus.ERROR, "You are already in the waiting list for this time.", null));
            return;
        }

        // Step 3: Create Placeholder Order
        Order placeholderOrder = new Order(0, item.getReservationDate(), item.getNumberOfGuests(),
                item.getConfirmationCode(), finalCustomer, null, new Date(), null, null, 0.0, OrderStatus.PENDING);

        if (orderDAO.createOrder(placeholderOrder)) {
            // Step 4: Add to Waiting List
            if (waitingListDAO.enterWaitingList(item)) {
                client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
                        Response.ResponseStatus.SUCCESS, String.valueOf(generatedCode), true));
                sendListToAllClients();
            } else {
                client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
                        Response.ResponseStatus.DATABASE_ERROR, "Failed to add to waiting list.", null));
            }
        } else {
            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
                    Response.ResponseStatus.DATABASE_ERROR, "Failed to create placeholder order.", null));
        }
    }

    /**
     * Removes an entry from the waiting list based on the Waiting List ID.
     */
    private void handleExitWaitingList(Request req, ConnectionToClient client) throws SQLException, IOException {
        if (req.getId() == null) {
            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST,
                    Response.ResponseStatus.ERROR, "Missing ID", null));
            return;
        }
        
        if (waitingListDAO.exitWaitingList(req.getId())) {
            // EmailService.sendConfirmation(null, null); // Currently disabled
            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST,
                    Response.ResponseStatus.SUCCESS, "Removed", true));
            sendListToAllClients();
        }
    }

    /**
     * Promotes a waiting list entry to an active order.
     * Updates the order status to APPROVED, removes from waiting list, and notifies user.
     * * NOTE: There is a known issue where waiting list items move to orders unexpectedly 
     * (verified with thread issues). Please consult with Liel regarding this logic.
     * * @param waitingId The ID of the waiting list entry to promote.
     * @param client    The client connection (can be null if triggered internally).
     * @return true if promotion was successful, false otherwise.
     */
    public boolean handlePromoteToOrder(Integer waitingId, ConnectionToClient client) throws SQLException, IOException {
        if (waitingId == null) {
            if (client != null) {
                client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER,
                        Response.ResponseStatus.ERROR, "Missing ID", null));
            }
            return false;
        }

        WaitingList entry = waitingListDAO.getByWaitingId(waitingId);
        if (entry == null || entry.getInWaitingList() == 0) {
            return false;
        }

        Customer customer = entry.getCustomer();
        if (customer == null && entry.getCustomerId() != null) {
            customer = customerDAO.getCustomerByCustomerId(entry.getCustomerId());
        }

        if (customer == null) {
            System.err.println("Failed to promote waiting list entry " + waitingId + ": Customer not found.");
            return false;
        }

        Order existingOrder = orderDAO.getOrderByConfirmationCodeWithStatusNull(entry.getConfirmationCode());
        if (existingOrder == null) {
            System.err.println("Critical Error: Order not found for code " + entry.getConfirmationCode());
            return false;
        }

        // Update Order details
        existingOrder.setCustomer(customer);
        existingOrder.setOrderStatus(OrderStatus.APPROVED);

        if (orderDAO.updateOrder(existingOrder)) {
            // Remove from waiting list and update views
            waitingListDAO.exitWaitingList(waitingId);
            List<WaitingList> updatedList = waitingListDAO.getAllWaitingList();
            
            EmailService.sendConfirmation(existingOrder.getCustomer(), existingOrder);

            // Broadcast updates
            Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
                    Response.ResponseStatus.SUCCESS, null, updatedList));
            Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
                    Response.ResponseStatus.SUCCESS, null, orderDAO.getAllOrders()));

            if (client != null) {
                client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER,
                        Response.ResponseStatus.SUCCESS, null, true));
            }
            return true;
        }
        return false;
    }

    /**
     * Helper method to broadcast the updated waiting list to all connected clients.
     */
    private void sendListToAllClients() throws SQLException {
        List<Map<String, Object>> list = waitingListDAO.getAllWaitingListWithCustomersToFilter();
        Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL_LIST,
                Response.ResponseStatus.SUCCESS, null, list));
    }
}