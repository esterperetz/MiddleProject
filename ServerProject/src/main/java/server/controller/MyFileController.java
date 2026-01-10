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

public class MyFileController {

	public void handle(Request req, ConnectionToClient client) {
		if (req.getAction() != ActionType.DOWNLOAD_REPORT) {
			return;
		}

		// System.out.println("Processing Reports..." + req.getPayload());

		try {
			handleGetMonthlyReport(req, client);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			try {
				client.sendToClient(new Response(ResourceType.REPORT, ActionType.DOWNLOAD_REPORT,
						Response.ResponseStatus.ERROR, "Failed to download report: " + e.getMessage(), null));
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
				client.sendToClient(new Response(ResourceType.REPORT, ActionType.DOWNLOAD_REPORT,
						Response.ResponseStatus.ERROR, "Invalid date format", null));
				return;
			}
		}
		String fileName = "Report_" + year + "_" + String.format("%02d", month) + ".html";
        String dirPath = "server_files/reports/";
        File file = new File(dirPath + fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            client.sendToClient(new Response(ResourceType.REPORT, ActionType.DOWNLOAD_REPORT,
                    Response.ResponseStatus.ERROR, "Report not found on server.", null));
            return;
        }

        // 3. יצירת אובייקט MyFile והמרת הקובץ ל-byte array
        MyFile myFile = new MyFile(fileName);
        byte[] mybytearray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            bis.read(mybytearray, 0, mybytearray.length);
            
            myFile.initArray(mybytearray.length);
            myFile.setSize(mybytearray.length);
            myFile.setMybytearray(mybytearray);
        }
        client.sendToClient(new Response(ResourceType.REPORT, ActionType.DOWNLOAD_REPORT,
                Response.ResponseStatus.SUCCESS, "File downloaded successfully", myFile));
	}
}
