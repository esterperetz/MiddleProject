package server.controller;

import java.io.IOException;
import java.sql.Date;
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
//				getHoursForDay(req, client);
			case UPDATE:
				handleUpdate(req,client);
				break;
			case CREATE:
				handleSave(req, client);
				break;
			case DELETE:
				handleDelete(req,client);
				break;
			default:
				break;
			}
		} catch (SQLException e) {
			client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.DATABASE_ERROR,
					e.getMessage(), null));
		}
	}

	private void handleUpdate(Request req, ConnectionToClient client) {
		
		Integer dayOfWeek = (Integer) req.getId();
		String toDo = (String)req.getPayload();
		try {
		if (toDo.equals("close") && businessHourDAO.setStandardDayClosed(dayOfWeek)) {
			
				client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
						Response.ResponseStatus.SUCCESS, "Date has been closed.", null));
				syncAllClients();
	
		}
		else if (toDo.equals("open") && businessHourDAO.setStandardDayClosed(dayOfWeek)) {
			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
					Response.ResponseStatus.SUCCESS, "Date has been opened.", null));
			syncAllClients();
		}
		else {
			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
					Response.ResponseStatus.ERROR, "Hours failed to be updated", null));
			syncAllClients();
			
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void handleDelete(Request req, ConnectionToClient client) throws SQLException {
		Date date = (Date) req.getPayload();
		try {
		if (businessHourDAO.deleteOpeningHours(date)) {
			
				client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
						Response.ResponseStatus.SUCCESS, "Hours updated", null));
				syncAllClients();
	
		}
		else {
			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
					Response.ResponseStatus.ERROR, "Hours failed to be updated", null));
			syncAllClients();
			
		}
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<OpeningHours> hours = businessHourDAO.getAllOpeningHours();
		client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET_ALL,
				Response.ResponseStatus.SUCCESS, null, hours));
	}

//	private void getHoursForDay(Request req, ConnectionToClient client) throws SQLException, IOException {
//		int requestedDate = (int) req.getPayload();
//		OpeningHours openingHours = businessHourDAO.getHoursForDate(requestedDate);
//		if (openingHours == null) {
//			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET, Response.ResponseStatus.ERROR,
//					"Error: cant find opening hour for this date", null));
//		} else
//			client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET,
//					Response.ResponseStatus.SUCCESS, null, openingHours));
//	}

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