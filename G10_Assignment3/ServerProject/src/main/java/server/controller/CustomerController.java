package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.CustomerDAO;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.Table;
import ocsf.server.ConnectionToClient;

/**
 * Controller class responsible for managing Customer and Subscriber data.
 * Handles registration, retrieval, and updates of customer profiles.
 */
public class CustomerController {

    private final CustomerDAO CustomerDAO = new CustomerDAO();
    /**
     * Main handler for Customer requests.
     * Routes the request based on the ActionType to the appropriate method.
     *
     * @param req    The request object.
     * @param client The client connection.
     * @throws SQLException If a database error occurs.
     */
	public void handle(Request req, ConnectionToClient client) throws SQLException {
		if (req.getResource() != ResourceType.CUSTOMER) {
			try {
				client.sendToClient("Error: Incorrect resource type. Expected SUBSCRIBER.");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		ActionType action = req.getAction();
		System.out.println("SubscriberController handling action: " + action);

        try {
            switch (action) {
                case REGISTER_CUSTOMER:
                    registerCustomer(req, client);
                    break;

                case GET_BY_ID:
                    getSubscriberById(req, client);
                    break;

                case GET_ALL:
                    getAllSubscribers(req, client);
                    break;

                case UPDATE:
                    updateSubscriber(req, client);
                    break;
                case CHECK_QR_CODE:
                    break;

			default:
				client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
						Response.ResponseStatus.ERROR, "Error: Unknown action for User/Subscriber resource.", null));
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

    ///need to be checked
    /**
     * Registers a new customer in the database.
     * Checks if the email already exists for subscribers before creation.
     *
     * @param req    The request containing the new Customer object.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
	private void registerCustomer(Request req, ConnectionToClient client) throws IOException, SQLException {
		Customer newCub = (Customer) req.getPayload();

		// Updated to camelCase
		Customer existing = CustomerDAO.getCustomerByEmail(newCub.getEmail());
		if (newCub.getType() == CustomerType.SUBSCRIBER && existing != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.ERROR, "Error: Email already exists.", null));
			
			return;
		}

		boolean success = CustomerDAO.createCustomer(newCub);
		if (success) {
			// Updated to camelCase
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_CUSTOMER,
					Response.ResponseStatus.SUCCESS, "Customer_id" + newCub.getCustomerId(), newCub));
			sendCustomerToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_CUSTOMER,
					Response.ResponseStatus.ERROR, "Error: Failed to create subscriber in DB.", null));
		}
	}
	   /**
     * Retrieves a subscriber by their Subscriber Code (ID).
     *
     * @param req    The request containing the ID.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */

	private void getSubscriberById(Request req, ConnectionToClient client) throws IOException, SQLException {
		int id = req.getId();
		Customer sub = CustomerDAO.getCustomerBySubscriberCode(id);
		if (sub != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.SUCCESS,
					"id:" + id, sub));
			sendCustomerToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.NOT_FOUND,
					"Error: Subscriber not found.", null));
		}
	}
	  /**
     * Retrieves all customers/subscribers from the database.
     *
     * @param req    The request object.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
	private void getAllSubscribers(Request req, ConnectionToClient client) throws IOException, SQLException {
		List<Customer> list = CustomerDAO.getAllCustomers();
		client.sendToClient(
				new Response(ResourceType.CUSTOMER, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, list));
		sendCustomerToAllClients();
	}

	 /**
     * Updates an existing subscriber's details.
     *
     * @param req    The request containing the updated Customer object.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
    private void updateSubscriber(Request req, ConnectionToClient client) throws IOException, SQLException {
        Customer subToUpdate = (Customer) req.getPayload();
        boolean success = CustomerDAO.updateCustomerDetails(subToUpdate);

		if (success) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Success: Subscriber updated.", null));
			sendCustomerToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: Failed to update subscriber.", null));
		}
	}

    /**
     * Helper method to broadcast the updated list of customers to all clients.
     * Note: Sends the data under ResourceType.ORDER (preserved from original code).
     */
	private void sendCustomerToAllClients() throws SQLException, IOException {
		List<Customer> list = CustomerDAO.getAllCustomers();
		Router.sendToAllClients(
				new Response(ResourceType.CUSTOMER, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, list));

	}
}