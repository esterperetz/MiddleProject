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
 * Controller responsible for handling file download requests.
 * It locates report files on the server and sends them to the client.
 */
public class MyFileController {

    /**
     * Main handler for file-related requests.
     * Delegates the request to specific methods based on the action type.
     * Handles any errors by sending an error response to the client.
     *
     * @param req    The request received from the client.
     * @param client The connection to the client.
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
     * Processes a request to download a monthly report.
     * <p>
     * 1. Parses the date (MM/YYYY) from the request payload.
     * 2. Locates the corresponding ZIP file on the server.
     * 3. Reads the file into a byte array.
     * 4. Sends the file wrapped in a MyFile object to the client.
     * </p>
     *
     * @param req    The request containing the date filter.
     * @param client The connection to the client.
     * @throws SQLException If a database error occurs.
     * @throws IOException  If a file I/O error occurs.
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
        String zipFileName = "Report_" + year + "_" + String.format("%02d", month) + ".zip";
        String dirPath = "server_files/reports/";
        File file = new File(dirPath + zipFileName);
        
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            client.sendToClient(new Response(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT,
                    Response.ResponseStatus.ERROR, "Report not found on server.", null));
            return;
        }

        // Convert file to byte array (MyFile object)
        MyFile myFile = new MyFile(zipFileName);
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