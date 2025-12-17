package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import DAO.SubscriberDAO;
import DBConnection.DBConnection;
import Entities.ActionType;
import Entities.Request;
import Entities.ResourceType;
import Entities.Subscriber;
import ocsf.server.ConnectionToClient;

//Controller responsible for handling all subscriber-related requests.

public class SubscriberController {

	private final SubscriberDAO subscriberDAO = new SubscriberDAO();

	public void handle(Request req, ConnectionToClient client) throws SQLException {
		if (req.getResource() != ResourceType.SUBSCRIBER) {
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
			case REGISTER_SUBSCRIBER:
				registerSubscriber(req, client);
				break;

			case GET_BY_ID:
				getSubscriberById(req, client);
				break;

			case GET_ALL: // For manager reports/view
				getAllSubscribers(req, client);
				break;

			case UPDATE:
				updateSubscriber(req, client);
				break;

			default:
				client.sendToClient("Error: Unknown action for User/Subscriber resource.");
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// --- Private Handling Methods ---

	private void registerSubscriber(Request req, ConnectionToClient client) throws IOException, SQLException {
		Subscriber newSub = (Subscriber) req.getPayload();

		// Check if username already exists
		Subscriber existing = subscriberDAO.getSubscriberBySubscriberName(newSub.getSubscriber_name());
		if (existing != null) {
			client.sendToClient("Error: Username already exists.");
			return;
		}

		boolean success = subscriberDAO.createSubscriber(newSub);
		if (success) {
			// Send back the object (which now has the generated ID) or a success message
			client.sendToClient(new Request(req.getResource(), ActionType.REGISTER_SUBSCRIBER, newSub.getSubscriber_id(), newSub));
		} else {
			client.sendToClient("Error: Failed to create subscriber in DB.");
		}
	}

	private void getSubscriberById(Request req, ConnectionToClient client) throws IOException, SQLException {
		int id = req.getId();
		Subscriber sub = subscriberDAO.getSubscriberById(id);
		if (sub != null) {
			client.sendToClient(new Request(req.getResource(), ActionType.GET_BY_ID, id, sub));
		} else {
			client.sendToClient("Error: Subscriber not found.");
		}
	}

	private void getAllSubscribers(Request req, ConnectionToClient client) throws IOException, SQLException {
		List<Subscriber> list = subscriberDAO.getAllSubscribers();
		client.sendToClient(new Request(req.getResource(), ActionType.GET_ALL, null, list));
	}

	private void updateSubscriber(Request req, ConnectionToClient client) throws IOException, SQLException {
		Subscriber subToUpdate = (Subscriber) req.getPayload();
		boolean success = subscriberDAO.updateSubscriberDetails(subToUpdate);

		if (success) {
			client.sendToClient("Success: Subscriber updated.");
		} else {
			client.sendToClient("Error: Failed to update subscriber.");
		}
	}
}