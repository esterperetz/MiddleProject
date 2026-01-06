package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import DAO.BusinessHourDAO;
import entities.ActionType;
import entities.OpeningHours;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import ocsf.server.ConnectionToClient;

public class BusinessHourController {
	private final BusinessHourDAO businessHourDAO = new BusinessHourDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
		try {
			switch (req.getAction()) {
			case GET_ALL:
				handleGetAll(req, client);
				break;
			case GET:
				getHoursForDay(req, client);
			case UPDATE:
				break;
			case CREATE:
				handleSave(req, client);
				break;
			default:
				break;
			}
		} catch (SQLException e) {
			client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.DATABASE_ERROR,
					e.getMessage(), null));
		}
	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<OpeningHours> hours = businessHourDAO.getAllOpeningHours();
		client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET_ALL,
				Response.ResponseStatus.SUCCESS, null, hours));
	}

	private void getHoursForDay(Request req, ConnectionToClient client) throws SQLException, IOException {
		int requestedDate = (int) req.getPayload();
		OpeningHours openingHours = businessHourDAO.getHoursForDate(requestedDate);
		if (openingHours == null) {
			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET, Response.ResponseStatus.ERROR,
					"Error: cant find opening hour for this date", null));
		} else
			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET,
					Response.ResponseStatus.SUCCESS, null, openingHours));
	}

	private void handleSave(Request req, ConnectionToClient client) throws SQLException, IOException {
		OpeningHours oh = (OpeningHours) req.getPayload();
		if (businessHourDAO.saveOrUpdate(oh)) {
			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
					Response.ResponseStatus.SUCCESS, "Hours updated", null));
			syncAllClients();
		}
	}

	private void syncAllClients() throws SQLException {
		List<OpeningHours> updatedHours = businessHourDAO.getAllOpeningHours();
		Router.sendToAllClients(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET_ALL,
				Response.ResponseStatus.SUCCESS, null, updatedHours));
	}
}