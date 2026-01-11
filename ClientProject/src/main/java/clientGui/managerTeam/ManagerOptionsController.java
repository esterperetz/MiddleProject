package clientGui.managerTeam;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.OrderUi_controller;
import clientGui.reservation.WaitingListController;
import clientGui.user.RegisterSubscriberController;
import clientLogic.EmployeeLogic;
import entities.Employee;
import entities.OpeningHours;
import entities.WaitingList;

import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import client.MessageListener;

public class ManagerOptionsController extends MainNavigator implements Initializable, MessageListener<Object> {

    // --- Internal Fields ---
    private Employee.Role isManager;
    private boolean isManagerFlag;
    private ObservableList<String> specialDatesModel;
    private Employee emp;

    // --- FXML UI Components (Left Side - Navigation) ---
    @FXML private Button btnViewReports;
    @FXML private Button btnMonthlyReports;
    @FXML private Label lblDashboardTitle;
    @FXML private Label lblDashboardSubtitle;
    @FXML private Button btnSignUp;

    // --- Schedule Management UI (Right Side - UPDATED) ---
    
    @FXML private DatePicker dpManageDate;      // בוחר התאריך
    @FXML private TextField txtManageOpen;      // שעת פתיחה
    @FXML private TextField txtManageClose;     // שעת סגירה
    @FXML private CheckBox cbIsSpecial;         // האם זה יום מיוחד?
    @FXML private ListView<String> listSpecialDates; // רשימת תצוגה
    @FXML private Label lblHoursStatus;         // לייבל סטטוס
	private EmployeeLogic employeeLogic;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        specialDatesModel = FXCollections.observableArrayList();
        listSpecialDates.setItems(specialDatesModel);
    }

    public void initData(Employee emp, ClientUi clientUi, Employee.Role isManager) {
        this.clientUi = clientUi;
        this.emp = emp;
        employeeLogic = new EmployeeLogic(this.clientUi);
        if (isManager == Employee.Role.MANAGER) {
            this.isManager = Employee.Role.MANAGER;
            this.isManagerFlag = true;
            btnViewReports.setVisible(true);
            btnViewReports.setManaged(true);
            btnSignUp.setVisible(true);
            btnSignUp.setManaged(true);
            btnMonthlyReports.setVisible(true);
            btnMonthlyReports.setManaged(true);
            lblDashboardTitle.setText("Hello Manager, " + emp.getUserName());
            lblDashboardSubtitle.setText("Manager Dashboard - Full Access");
        } else {
            this.isManagerFlag = false;
            this.isManager = Employee.Role.REPRESENTATIVE;
            btnViewReports.setVisible(false);
            btnViewReports.setManaged(false);
            btnSignUp.setVisible(false);
            btnSignUp.setManaged(false);
            btnMonthlyReports.setVisible(false);
            btnMonthlyReports.setManaged(false);
            lblDashboardTitle.setText("Hello, " + emp.getUserName());
            lblDashboardSubtitle.setText("Employee Dashboard");
        }

        if (this.clientUi == null) {
            System.err.println("Error: ClientUi is null in ManagerOptionsController!");
            return;
        }
    }

    /**
     * UNIFIED Method: Update hours for a specific date (Standard OR Special).
     */
    @FXML
    void updateScheduleBtn(ActionEvent event) {
    	try {
        LocalDate date = dpManageDate.getValue();
        String openTime = txtManageOpen.getText();
        String closeTime = txtManageClose.getText();
        boolean isSpecial = cbIsSpecial.isSelected();
        
      
            // 1. הגדרת הפורמט שהמשתמש מקליד (שעות:דקות)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            // 2. המרה ל-LocalTime (זה יודע להתמודד עם "08:00")
            LocalTime localOpen = LocalTime.parse(openTime, formatter);
            LocalTime localClose = LocalTime.parse(closeTime, formatter);

            // 3. המרה ל-java.sql.Time (בשביל ה-DB)
            Time sqlOpenTime = Time.valueOf(localOpen);
            Time sqlCloseTime = Time.valueOf(localClose);

            System.out.println("Converted: " + sqlOpenTime + " - " + sqlCloseTime);
            
            // כאן אתה שולח לשרת...
            // updateBusinessHours(date, sqlOpenTime, sqlCloseTime, ...);

      
        // 1. Validation
        if (date == null) {
            setStatus("Please select a date first.", true);
            return;
        }
        if (openTime.isEmpty() || closeTime.isEmpty()) {
            setStatus("Please enter both opening and closing times.", true);
            return;
        }

        // 2. Logic processing
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String typeStr = isSpecial ? "(Special Event)" : "(Updated Hours)";
        
        // יצירת מחרוזת לתצוגה ברשימה למטה (רק בשביל הפידבק ב-GUI)
        String listEntry = String.format("%s: %s - %s %s", dateStr, openTime, closeTime, typeStr);

       
        System.out.println("Updating schedule: " + listEntry);
        if (date != null) {
            // המרה ישירה ופשוטה
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            
            if(typeStr.equals("(Updated Hours)")) {
            	
           	 employeeLogic.createOpeningHours(new OpeningHours(sqlDate,null,sqlOpenTime,sqlCloseTime));
           }
           else {
        	   LocalDate localDate = LocalDate.now();
        	   employeeLogic.createOpeningHours(new OpeningHours(java.sql.Date.valueOf(localDate.now()),sqlDate,sqlOpenTime,sqlCloseTime));
            }
        }
        
       
        // 4. Update UI
        specialDatesModel.add(0, listEntry); // Add to top of list
        setStatus("Schedule updated successfully!", false);
    	} catch (DateTimeParseException e) {
              setStatus("Invalid time format! Use HH:mm (e.g., 08:00)", true);
              return;
          }
        
        // Clear fields? Optional.
        // dpManageDate.setValue(null);
        // cbIsSpecial.setSelected(false);
    }

    @FXML
    void removeSpecialDateBtn(ActionEvent event) {
        String selectedItem = listSpecialDates.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            specialDatesModel.remove(selectedItem);
            // TODO: Notify server to revert hours for this date
            setStatus("Entry removed", false);
        } else {
            setStatus("Select an item to remove", true);
        }
    }

    private void setStatus(String msg, boolean isError) {
        lblHoursStatus.setText(msg);
        lblHoursStatus.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
    }

    // --- Navigation Methods (No Changes) ---

    @FXML
    void goToWaitingListBtn(ActionEvent event) {
        WaitingListController waiting_list = super.loadScreen("reservation/WaitingList", event, clientUi);
        if (waiting_list != null) {
            waiting_list.initData(emp, this.clientUi, this.isManager);
        } else {
            System.err.println("Failed to load WaitingList.");
        }
    }

    @FXML
    void goToMonthlyReportsBtn(ActionEvent event) {
         MonthlyReportsController m = super.loadScreen("managerTeam/MonthlyReports", event, clientUi);
         if (m != null) {
             m.initData(this.emp, this.clientUi, this.isManager); 
         } else {
             System.out.println("error: MonthlyReportsController is null");
         }
    }

    @FXML
    void goToOrderDetailsBtn(ActionEvent event) {
        OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
        if (controller != null) {
            controller.initData(emp, this.clientUi, this.isManager);
        } else {
            System.err.println("Failed to load OrderUi.");
        }
    }

    @FXML
    public void goToSignUpEmployee(ActionEvent event) {
        try {
            RegisterEmployeeController registerEmployee = super.loadScreen("managerTeam/RegisterEmployee", event, clientUi);
            registerEmployee.initData(emp, this.clientUi, this.isManager);
        } catch (NullPointerException e) {
            System.out.println("Error: the object RegisterEmployeeController is null");
        }
    }

    @FXML
    void goToRegisterSubscriberBtn(ActionEvent event) {
        RegisterSubscriberController r = super.loadScreen("user/RegisterSubscriber", event, clientUi);
        try {
            r.initData(emp, this.clientUi, this.isManager);
        } catch (NullPointerException e) {
            System.out.println("Error: RegisterSubscriberController is null");
        }
    }

    @FXML
    void goToReportsBtn(ActionEvent event) {
        ReportsController r = super.loadScreen("managerTeam/ReportsScreen", event, clientUi);
        if (r != null) {
            r.initData(emp, this.clientUi, this.isManager);
        } else {
            System.out.println("Error: ReportsController is null!!");
        }
    }

    @FXML
    void goToTableManagementBtn(ActionEvent event) {
        TableManagementController controller = super.loadScreen("managerTeam/TableManagement", event, clientUi);
        if (controller != null) {
            controller.initData(emp, clientUi);
        } else {
            System.err.println("Failed to load TableManagement.");
        }
    }

    @FXML
    void goBackBtn(ActionEvent event) {
        System.out.println("Going back / Signing out...");
        super.loadScreen("navigation/SelectionScreen", event, clientUi);
    }

    @Override
    public void onMessageReceive(Object msg) {
        System.out.println("Manager Controller received: " + msg.toString());
    }
}