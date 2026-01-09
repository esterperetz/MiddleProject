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
import ocsf.server.ConnectionToClient;

public class CustomerController {

	private final CustomerDAO CustomerDAO = new CustomerDAO();

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

				case FORGOT_CODE:
					handleForgotCode(req, client);
					break;

				default:
					client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
							Response.ResponseStatus.ERROR, "Error: Unknown action for User/Subscriber resource.",
							null));
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void registerCustomer(Request req, ConnectionToClient client) throws IOException, SQLException {
		boolean isUnique = false;
		int code;
		Customer newCub = (Customer) req.getPayload();

		// Updated to camelCase
		Customer existing = CustomerDAO.getSubscriberBySubscriberEmail(newCub.getEmail());
		if (newCub.getType() == CustomerType.SUBSCRIBER && existing != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.ERROR, "Error: Email already exists.", null));
			return;
		}
		if (newCub.getType() == CustomerType.SUBSCRIBER) {

			// Loop until a unique number is found
			do {
				// Generate a number (e.g., 5 digits: 10000 to 99999)
				code = 10000 + (int) (Math.random() * 90000);

				// Check against DB if this number already exists
				if (CustomerDAO.getCustomerBySubscriberCode(code) == null) {
					isUnique = true;
				}
			} while (!isUnique);
			newCub.setSubscriberCode(code);
		}

		boolean success = CustomerDAO.createCustomer(newCub);
		if (success) {
			// Updated to camelCase
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.SUCCESS, "Customer_id" + newCub.getCustomerId(), newCub));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.ERROR, "Error: Failed to create subscriber in DB.", null));
		}
	}

	private void getSubscriberById(Request req, ConnectionToClient client) throws IOException, SQLException {
		int id = req.getId();
		Customer sub = CustomerDAO.getCustomerBySubscriberCode(id);
		if (sub != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.SUCCESS,
					"id:" + id, sub));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.NOT_FOUND,
					"Error: Subscriber not found.", null));
		}
	}

	private void getAllSubscribers(Request req, ConnectionToClient client) throws IOException, SQLException {
		List<Customer> list = CustomerDAO.getAllCustomers();
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, list));
	}

	private void updateSubscriber(Request req, ConnectionToClient client) throws IOException, SQLException {
		Customer subToUpdate = (Customer) req.getPayload();
		boolean success = CustomerDAO.updateCustomerDetails(subToUpdate);

		if (success) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Success: Subscriber updated.", null));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: Failed to update subscriber.", null));
		}
	}

	private void handleForgotCode(Request req, ConnectionToClient client) throws IOException {
		try {
			String idStr = (String) req.getPayload();
			int subId = Integer.parseInt(idStr);
			Customer sub = CustomerDAO.getCustomerBySubscriberCode(subId);

			if (sub != null) {
				// Mock sending email
				System.out.println("Processing Forgot Code for Subscriber ID: " + subId);
				System.out.println("Mock: Sending email to " + sub.getEmail() + " with code.");

				client.sendToClient(new Response(req.getResource(), ActionType.FORGOT_CODE,
						Response.ResponseStatus.SUCCESS, "Code sent to your email/phone.", null));
			} else {
				client.sendToClient(new Response(req.getResource(), ActionType.FORGOT_CODE,
						Response.ResponseStatus.ERROR, "Error: Subscriber not found.", null));
			}
		} catch (Exception e) {
			client.sendToClient(new Response(req.getResource(), ActionType.FORGOT_CODE,
					Response.ResponseStatus.ERROR, "Error: Invalid ID format.", null));
		}
	}
}