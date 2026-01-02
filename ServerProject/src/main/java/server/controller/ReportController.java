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

    private final ReportDAO reportDAO = new ReportDAO();

    /**
     * Handles incoming report requests.
     * Aggregates data from multiple DAO methods into a single response object.
     */
    public void handle(Request req, ConnectionToClient client) {
        // Validate action type
        if (req.getAction() != ActionType.GET_MONTHLY_REPORT) {
            return;
        }

        try {
            // Fetch data for all charts
            Map<Integer, Integer> arrivals = reportDAO.getArrivalsByHour();
            Map<Integer, Integer> departures = reportDAO.getDeparturesByHour();
            Map<Integer, Integer> cancellations = reportDAO.getCancellationsByHour();
            Map<String, Integer> dailyOrders = reportDAO.getDailyOrderCount();

            // Pack all data into a single map to send to client
            Map<String, Object> fullReportData = new HashMap<>();
            fullReportData.put("arrivals", arrivals);
            fullReportData.put("departures", departures);
            fullReportData.put("cancellations", cancellations);
            fullReportData.put("dailyOrders", dailyOrders);
            System.out.println("DEBUG REPORT: Daily Orders Map -> " + dailyOrders);
            System.out.println("DEBUG REPORT: Arrivals Map -> " + arrivals);
            // Send successful response
            client.sendToClient(new Response(
                    ResourceType.REPORT, 
                    ActionType.GET_MONTHLY_REPORT, 
                    Response.ResponseStatus.SUCCESS, 
                    null, 
                    fullReportData
            ));

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            try {
                client.sendToClient(new Response(
                		ResourceType.REPORT, 
                		ActionType.GET_MONTHLY_REPORT, 
                		Response.ResponseStatus.ERROR, 
                		"Failed to generate report", 
                		null));
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }
}