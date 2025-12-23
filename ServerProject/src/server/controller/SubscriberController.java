package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.SubscriberDAO;
import Entities.ActionType;
import Entities.Request;
import Entities.ResourceType;
import Entities.Response;
import Entities.Subscriber;
import ocsf.server.ConnectionToClient;

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

			case GET_ALL:
				getAllSubscribers(req, client);
				break;

			case UPDATE:
				updateSubscriber(req, client);
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

	private void registerSubscriber(Request req, ConnectionToClient client) throws IOException, SQLException {

		Subscriber newSub = (Subscriber) req.getPayload();
		System.out.println("client " + newSub.getEmail());
		
		// Updated to camelCase
		Subscriber existing = subscriberDAO.getSubscriberBySubscriberName(newSub.getSubscriberName());
		if (existing != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.ERROR, "Error: Username already exists.", null));
			return;
		}

		boolean success = subscriberDAO.createSubscriber(newSub);
		if (success) {
			// Updated to camelCase
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.SUCCESS, "Subscriber_id" + newSub.getSubscriberId(), newSub));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
					Response.ResponseStatus.ERROR, "Error: Failed to create subscriber in DB.", null));
		}
	}

	private void getSubscriberById(Request req, ConnectionToClient client) throws IOException, SQLException {
		int id = req.getId();
		Subscriber sub = subscriberDAO.getSubscriberById(id);
		if (sub != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.SUCCESS,
					"id:" + id, sub));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.NOT_FOUND,
					"Error: Subscriber not found.", null));
		}
	}

	private void getAllSubscribers(Request req, ConnectionToClient client) throws IOException, SQLException {
		List<Subscriber> list = subscriberDAO.getAllSubscribers();
		client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, list));
	}

	private void updateSubscriber(Request req, ConnectionToClient client) throws IOException, SQLException {
		Subscriber subToUpdate = (Subscriber) req.getPayload();
		boolean success = subscriberDAO.updateSubscriberDetails(subToUpdate);

		if (success) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Success: Subscriber updated.", null));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: Failed to update subscriber.", null));
		}
	}
}