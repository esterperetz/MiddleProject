package server.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import entities.ActionType;
import entities.MyFile;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import ocsf.server.ConnectionToClient;

/**
 * Controller for handling file downloads.
 * It finds the requested report on the server and sends it to the client.
 */
public class MyFileController {

    /**
     * Main handler for file requests.
     * Checks if the action is DOWNLOAD_REPORT and calls the processing method.
     */
    public void handle(Request req, ConnectionToClient client) {
        if (req.getAction() != ActionType.DOWNLOAD_REPORT) {
            return;
        }

        try {
            handleGetMonthlyReport(req, client);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            try {
                client.sendToClient(new Response(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT,
                        Response.ResponseStatus.ERROR, "Failed to download report: " + e.getMessage(), null));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Locates the HTML report file based on the month/year.
     * Converts the file to a byte array and sends it to the client.
     */
    private void handleGetMonthlyReport(Request req, ConnectionToClient client) throws SQLException, IOException {
        String filter = (String) req.getPayload();
        Integer month = null;
        Integer year = null;

        // Parse the date (MM/YYYY)
        if (filter != null && filter.contains("/")) {
            String[] parts = filter.split("/");
            try {
                month = Integer.parseInt(parts[0]);
                year = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                client.sendToClient(new Response(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT,
                        Response.ResponseStatus.ERROR, "Invalid date format", null));
                return;
            }
        }

        // Find the file
        String fileName = "Report_" + year + "_" + String.format("%02d", month) + ".html";
        String dirPath = "server_files/reports/";
        File file = new File(dirPath + fileName);
        
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            client.sendToClient(new Response(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT,
                    Response.ResponseStatus.ERROR, "Report not found on server.", null));
            return;
        }

        // Convert file to byte array (MyFile object)
        MyFile myFile = new MyFile(fileName);
        byte[] mybytearray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            bis.read(mybytearray, 0, mybytearray.length);
            
            myFile.initArray(mybytearray.length);
            myFile.setSize(mybytearray.length);
            myFile.setMybytearray(mybytearray);
        }
        
        // Send to client
        client.sendToClient(new Response(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT,
                Response.ResponseStatus.SUCCESS, "File downloaded successfully", myFile));
    }
}