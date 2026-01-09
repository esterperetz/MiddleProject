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

    public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.REPORT) {
            client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.ERROR,
                    "Error: Incorrect resource type requested.", null));
            return;
        }

        try {
            switch (req.getAction()) {
                case GET_MONTHLY_REPORT:
                    handleGetMonthlyReport(req, client);
                    break;
                default:
                    client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR,
                            "Unsupported action", null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR,
                    "Database error: " + e.getMessage(), null));
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