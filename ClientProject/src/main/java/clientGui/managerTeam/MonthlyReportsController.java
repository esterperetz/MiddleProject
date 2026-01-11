package clientGui.managerTeam;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import entities.ActionType;
import entities.Alarm;
import entities.Employee;
import entities.MyFile;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * Controller for the Monthly Reports screen.
 * Allows managers to select a specific month and year, download the corresponding
 * statistical report as a ZIP file from the server, and automatically open it.
 */
public class MonthlyReportsController extends MainNavigator implements Initializable, client.MessageListener<Object> {

    @FXML private ComboBox<String> cmbMonth;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private Button btnDownload;
    @FXML private Label lblStatus;

    private Employee emp;
    private Employee.Role role;
    
    public static MonthlyReportsController instance;

    /**
     * Initializes the controller class.
     * Populates the month and year combo boxes immediately after the FXML is loaded.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        initComboBoxes();
    }

    /**
     * Sets up the session data and registers the message listener.
     * Clears old listeners to ensure this controller receives the server's response.
     *
     * @param emp      The logged-in employee.
     * @param clientUi The client interface for server communication.
     * @param role     The role of the employee.
     */
    public void initData(Employee emp, ClientUi clientUi, Employee.Role role) {
        this.clientUi = clientUi;
        this.emp = emp;
        this.role = role;

        if (this.clientUi != null) {
            this.clientUi.removeAllListeners();
            this.clientUi.addListener(this);
        }
    }

    /**
     * Populates the combo boxes with months (01-12) and years.
     * Sets the default selection to the previous month.
     */
    private void initComboBoxes() {
        // Fill months 01-12
        for (int i = 1; i <= 12; i++) {
            cmbMonth.getItems().add(String.format("%02d", i));
        }

        // Fill years from 2024 to current
        int currentYear = LocalDate.now().getYear();
        for (int i = 2024; i <= currentYear; i++) {
            cmbYear.getItems().add(i);
        }
        
        // Set default to previous month
        LocalDate prevMonth = LocalDate.now().minusMonths(1);
        cmbMonth.setValue(String.format("%02d", prevMonth.getMonthValue()));
        cmbYear.setValue(prevMonth.getYear());
    }

    /**
     * Handles the "Download" button click.
     * Validates the date selection and sends a request to the server to download the report.
     *
     * @param event The action event.
     */
    @FXML
    void downloadReportBtn(ActionEvent event) {
        String month = cmbMonth.getValue();
        Integer year = cmbYear.getValue();

        // Validation
        if (month == null || year == null) {
            Alarm.showAlert("Selection Error", "Please select both month and year.", AlertType.WARNING);
            return;
        }

        // User Feedback
        lblStatus.setText("Downloading ZIP...");
        lblStatus.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        
        // Send Request
        String datePayload = month + "/" + year;
        Request req = new Request(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT, null, datePayload);
        
        if(clientUi != null) {
            clientUi.sendRequest(req);
        }
    }
    
    /**
     * Handles messages received from the server.
     * If the report is found, it triggers the file save. Otherwise, it shows an error.
     *
     * @param msg The message object from the server.
     */
    @Override
    public void onMessageReceive(Object msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;
            
            if (response.getAction() == ActionType.DOWNLOAD_REPORT) {
                Platform.runLater(() -> {
                    if (response.getStatus() == Response.ResponseStatus.SUCCESS) {
                        // Success
                        if (response.getData() instanceof MyFile) {
                            saveAndOpenZip((MyFile) response.getData());
                        }
                    } else {
                        // Failure
                        String err = response.getMessage_from_server();
                        lblStatus.setText("Report Not Found");
                        lblStatus.setStyle("-fx-text-fill: red;");
                        Alarm.showAlert("Not Found", "No report exists for " + cmbMonth.getValue() + "/" + cmbYear.getValue() + "\n(" + err + ")", AlertType.ERROR);
                    }
                });
            }
        }
    }

    /**
     * Saves the received byte array as a ZIP file in the user's Downloads folder and opens it.
     *
     * @param myFile The file object received from the server.
     */
    private void saveAndOpenZip(MyFile myFile) {
        try {
            if (myFile == null || myFile.getSize() == 0) {
                lblStatus.setText("Error: Empty File");
                return;
            }

            String userHome = System.getProperty("user.home");
            String fileName = "Report_" + cmbYear.getValue() + "_" + cmbMonth.getValue() + ".zip";
            
            // Construct safe path
            String downloadsPath = userHome + File.separator + "Downloads";
            String filePath = downloadsPath + File.separator + fileName;
            
            File file = new File(filePath);

            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(myFile.getMybytearray(), 0, myFile.getSize());
                bos.flush();
            }

            // Update UI
            lblStatus.setText("Saved to Downloads!");
            lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

            // Open File
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                Alarm.showAlert("Success", "File saved successfully at:\n" + filePath, AlertType.INFORMATION);
            }

        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText("Save Error");
            lblStatus.setStyle("-fx-text-fill: red;");
            Alarm.showAlert("Save Error", "Could not save file to Downloads folder.", AlertType.ERROR);
        }
    } 

    /**
     * Navigates back to the Employee Options screen.
     *
     * @param event The action event.
     */
    @FXML
    void backBtn(ActionEvent event) {
        ManagerOptionsController manager = super.loadScreen("managerTeam/EmployeeOption", event, clientUi); 
        if(manager != null)
            manager.initData(emp, clientUi, role);
        else
            System.out.println("Error move scree to ManagerOptionsController from MonthlyReportsController is null");
    }
}