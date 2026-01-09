package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import DAO.ReportDAO;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import ocsf.server.ConnectionToClient;

public class ReportController {

    private final ReportDAO reportDao = new ReportDAO();

    /**
     * Handles incoming report requests.
     * Aggregates hashmaps (data) from multiple DAO methods into a single response
     * hashmap.
     */
    public void handle(Request req, ConnectionToClient client) {
        // Validate action type
        if (req.getAction() != ActionType.GET_MONTHLY_REPORT) {
            return;
        }

        System.out.println("Processing Reports..." + req.getPayload());

        try {
            handleGetMonthlyReport(req, client);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            try {
                client.sendToClient(new Response(
                        ResourceType.REPORT,
                        ActionType.GET_MONTHLY_REPORT,
                        Response.ResponseStatus.ERROR,
                        "Failed to generate report",
                        null));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleGetMonthlyReport(Request req, ConnectionToClient client) throws SQLException, IOException {
        String filter = (String) req.getPayload();
        Integer month = null;
        Integer year = null;

        if (filter != null && filter.contains("/")) {
            String[] parts = filter.split("/");
            try {
                month = Integer.parseInt(parts[0]);
                year = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // Keep nulls
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("arrivals", reportDao.getArrivalsByHour(month, year));
        data.put("departures", reportDao.getDeparturesByHour(month, year));
        data.put("cancellations", reportDao.getCancellationsByHour(month, year));
        data.put("dailyOrders", reportDao.getDailyOrderCount(month, year));

        client.sendToClient(new Response(req.getResource(), ActionType.GET_MONTHLY_REPORT,
                Response.ResponseStatus.SUCCESS, null, data));
    }
}