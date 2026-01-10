package clientGui.managerTeam;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import client.MessageListener;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType; // ייבוא חשוב
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class MonthlyReportsController extends MainNavigator implements Initializable, MessageListener<Object> {

    @FXML
    private ComboBox<String> cmbMonth;

    @FXML
    private ComboBox<Integer> cmbYear;

    @FXML
    private Button btnDownload;

    @FXML
    private Label lblStatus;

    private Employee emp;
    private Employee.Role role;

    public static MonthlyReportsController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        initComboBoxes();
    }

    public void initData(Employee emp, ClientUi clientUi, Employee.Role role) {
        this.clientUi = clientUi;
        this.emp = emp;
        this.role = role;
        // חשוב להירשם להאזנה להודעות כדי ש-onMessageReceive יפעל
        // client.ChatClient.client.addMessageListener(this); (תלוי במימוש שלך)
    }

    private void initComboBoxes() {
        for (int i = 1; i <= 12; i++) {
            cmbMonth.getItems().add(String.format("%02d", i));
        }
        int currentYear = LocalDate.now().getYear();
        for (int i = 2024; i <= currentYear; i++) {
            cmbYear.getItems().add(i);
        }
        LocalDate prevMonth = LocalDate.now().minusMonths(1);
        cmbMonth.setValue(String.format("%02d", prevMonth.getMonthValue()));
        cmbYear.setValue(prevMonth.getYear());
    }

    @FXML
    void downloadReportBtn(ActionEvent event) {
        String month = cmbMonth.getValue();
        Integer year = cmbYear.getValue();

        if (month == null || year == null) {
            Alarm.showAlert("Selection Error", "Please select both month and year.",AlertType.ERROR);
            return;
        }

        lblStatus.setText("Downloading report...");
        lblStatus.setStyle("-fx-text-fill: blue;");

        String datePayload = month + "/" + year;
        Request req = new Request(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT, null, datePayload);
        this.clientUi.sendRequest(req);
    }

    @Override
    public void onMessageReceive(Object msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;

            // בדיקה אם זו התשובה לבקשה שלנו
            if (response.getAction() == ActionType.DOWNLOAD_REPORT) {

                Platform.runLater(() -> {
                    // מקרה הצלחה: השרת החזיר SUCCESS
                    if (response.getStatus() == Response.ResponseStatus.SUCCESS) {
                        Object payload = response.getData();
                        if (payload instanceof MyFile) {
                            saveAndOpenFile((MyFile) payload);
                        }
                    } 
                    // מקרה כישלון: השרת החזיר ERROR (אין קובץ או בעיה אחרת)
                    else {
                        String errorDetails = response.getMessage_from_server();
                        if (errorDetails == null && response.getData() instanceof String) {
                            errorDetails = (String) response.getData();
                        }
                        if (errorDetails == null) errorDetails = "Report not found on server.";
                        
                        // עדכון לייבל
                        lblStatus.setText("Failed: " + errorDetails);
                        lblStatus.setStyle("-fx-text-fill: red;");
                        
                        // הקפצת הודעה למשתמש
                        Alarm.showAlert("Report Not Found", "No report exists for the selected date.\nDetails: " + errorDetails,AlertType.ERROR);
                    }
                });
            }
        }
    }

   

    // פונקציה זו נשארה לשימוש פנימי או קריאות אחרות
    public void receiveReportFile(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof String) {
                lblStatus.setText((String) msg);
                lblStatus.setStyle("-fx-text-fill: red;");
                Alarm.showAlert("Error", (String) msg,AlertType.ERROR); // הוספתי גם כאן
                return;
            }

            if (msg instanceof MyFile) {
                MyFile myFile = (MyFile) msg;
                saveAndOpenFile(myFile);
            }
        });
    }

    private void saveAndOpenFile(MyFile myFile) {
        try {
            String userHome = System.getProperty("user.home");
            String filePath = userHome + "/Downloads/" + myFile.getFileName();
            File file = new File(filePath);

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(myFile.getMybytearray(), 0, myFile.getSize());
            bos.flush();
            bos.close();
            fos.close();

            lblStatus.setText("Report saved to Downloads!");
            lblStatus.setStyle("-fx-text-fill: green;");

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText("Error saving file: " + e.getMessage());
            lblStatus.setStyle("-fx-text-fill: red;");
            Alarm.showAlert("System Error", "Could not save the file locally.",AlertType.ERROR);
        }
    }

    @FXML
    void backBtn(ActionEvent event) {
        ManagerOptionsController managerOptions = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
        if (managerOptions != null) {
            managerOptions.initData(emp, clientUi, role);
        }
    }
}