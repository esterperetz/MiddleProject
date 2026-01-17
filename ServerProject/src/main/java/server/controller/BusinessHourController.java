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

/**
 * Controller class responsible for managing the restaurant's business/opening hours.
 * Handles retrieval, creation, deletion, and updates of operating times.
 */
public class BusinessHourController {
    
    private final BusinessHourDAO businessHourDAO = new BusinessHourDAO();

    /**
     * Main handler for Business Hour requests.
     * Routes the request based on the ActionType.
     *
     * @param req    The request object.
     * @param client The client connection.
     * @throws IOException If an I/O error occurs.
     */
    public void handle(Request req, ConnectionToClient client) throws IOException {
        try {
            switch (req.getAction()) {
                case GET_ALL:
                    handleGetAll(req, client);
                    break;
                case GET:
                    // getHoursForDay(req, client);
                case UPDATE:
                    // handleUpdate(req,client);
                    break;
                case CREATE:
                    handleSave(req, client);
                    break;
                case DELETE:
                    handleDelete(req, client);
                    break;
                default:
                    break;
            }
        } catch (SQLException e) {
            client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.DATABASE_ERROR,
                    e.getMessage(), null));
        }
    }

    /**
     * Handles updates to business status (open/close).
     * Note: Currently logic sends success message but logic is partial.
     *
     * @param req    The request containing the ID and action payload.
     * @param client The client connection.
     */
    private void handleUpdate(Request req, ConnectionToClient client) {
        Integer dayOfWeek = (Integer) req.getId();
        String toDo = (String) req.getPayload();
        try {
            if (toDo.equals("close")) {
                client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
                        Response.ResponseStatus.SUCCESS, "Date has been closed.", null));
                syncAllClients();

            } else if (toDo.equals("open")) {
                client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
                        Response.ResponseStatus.SUCCESS, "Date has been opened.", null));
                syncAllClients();
            } else {
                client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
                        Response.ResponseStatus.ERROR, "Hours failed to be updated", null));
                syncAllClients();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Deletes a specific opening hour entry based on the date.
     *
     * @param req    The request containing the Date payload.
     * @param client The client connection.
     * @throws SQLException If a database error occurs.
     */
    private void handleDelete(Request req, ConnectionToClient client) throws SQLException {
        Date date = (Date) req.getPayload();
        try {
            if (businessHourDAO.deleteOpeningHours(date)) {
                client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
                        Response.ResponseStatus.SUCCESS, "Hours updated", null));
                syncAllClients();
            } else {
                client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
                        Response.ResponseStatus.ERROR, "Hours failed to be updated", null));
                syncAllClients();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all defined opening hours from the database.
     *
     * @param req    The request object.
     * @param client The client connection.
     * @throws SQLException If a database error occurs.
     * @throws IOException  If an I/O error occurs.
     */
    private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
        List<OpeningHours> hours = businessHourDAO.getAllOpeningHours();
        client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET_ALL,
                Response.ResponseStatus.SUCCESS, null, hours));
    }

    /**
     * Saves new opening hours or updates existing ones.
     *
     * @param req    The request containing the OpeningHours object.
     * @param client The client connection.
     * @throws SQLException If a database error occurs.
     * @throws IOException  If an I/O error occurs.
     */
    private void handleSave(Request req, ConnectionToClient client) throws SQLException, IOException {
        OpeningHours oh = (OpeningHours) req.getPayload();

        if (businessHourDAO.saveOrUpdate(oh)) {
            client.sendToClient(new Response(ResourceType.BUSINESS_HOUR, req.getAction(),
                    Response.ResponseStatus.SUCCESS, "Hours updated", oh));
            syncAllClients();
        }
    }

    /**
     * Helper method to broadcast updated hours to all connected clients.
     *
     * @throws SQLException If a database error occurs.
     */
    private void syncAllClients() throws SQLException {
        List<OpeningHours> updatedHours = businessHourDAO.getAllOpeningHours();
        Router.sendToAllClients(new Response(ResourceType.BUSINESS_HOUR, ActionType.GET_ALL,
                Response.ResponseStatus.SUCCESS, null, updatedHours));
    }
}