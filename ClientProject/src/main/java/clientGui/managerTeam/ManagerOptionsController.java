package clientGui.managerTeam;

import javafx.application.Platform;
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
import entities.Response;
import entities.Table;
import entities.WaitingList;

import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
    
    @FXML private DatePicker dpManageDate;     
    @FXML private TextField txtManageOpen;      
    @FXML private TextField txtManageClose;     
    @FXML private CheckBox cbIsSpecial;         
    @FXML private ListView<String> listSpecialDates; 
    @FXML private Label lblHoursStatus;     
    @FXML private CheckBox cbIsClosed;
	private EmployeeLogic employeeLogic;
	private String specialDate;
	private String listEntry;
	

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
        LocalDate date = dpManageDate.getValue();
        String openTimeStr = txtManageOpen.getText();
        String closeTimeStr = txtManageClose.getText();
        boolean isSpecial = cbIsSpecial.isSelected();
        boolean isClosed = cbIsClosed.isSelected();

        if (date == null) {
            setStatus("Please select a date first.", true);
            return;
        }

        if (!isClosed && (openTimeStr == null || openTimeStr.trim().isEmpty() ||
                          closeTimeStr == null || closeTimeStr.trim().isEmpty())) {
            setStatus("Please enter both opening and closing times.", true);
            return;
        }

        try {
            Time sqlOpenTime = null;
            Time sqlCloseTime = null;

            if (!isClosed) {
            	
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
                LocalTime localOpen = LocalTime.parse(openTimeStr, formatter);
                LocalTime localClose = LocalTime.parse(closeTimeStr, formatter);
                sqlOpenTime = Time.valueOf(localOpen);
                sqlCloseTime = Time.valueOf(localClose);
            }

            String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String typeStr = isSpecial ? "(Special Event)" : "(Updated Hours)";
            String timeDisplay = isClosed ? "[CLOSED]" : sqlOpenTime + " - " + sqlCloseTime;
            
            this.listEntry = String.format("%s: %s %s", dateStr, timeDisplay, typeStr);

            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            OpeningHours oh;

            if (!isSpecial) {
            	
                oh = new OpeningHours(sqlDate, null, sqlOpenTime, sqlCloseTime, isClosed);
            } else {
                oh = new OpeningHours(sqlDate, sqlDate, sqlOpenTime, sqlCloseTime, isClosed);
            }

            employeeLogic.createOpeningHours(oh);

        } catch (DateTimeParseException e) {
            e.printStackTrace();
            setStatus("Invalid time format! Use HH:mm (e.g., 08:00 or 8:00)", true);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("An error occurred during update.", true);
        }
    }
        
     

    @FXML
    void removeSpecialDateBtn(ActionEvent event) {
        String selectedItem = listSpecialDates.getSelectionModel().getSelectedItem();
        
        if (selectedItem != null) {
        
        	if(selectedItem.contains("(Special Event)")) {
        		String[] parts = selectedItem.split(":");
                String dateString = parts[0].trim(); 

                // המרה לתאריך SQL
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                java.sql.Date dateToDelete = java.sql.Date.valueOf(localDate);

                this.specialDate = selectedItem;
                employeeLogic.cancelOpeningHours(dateToDelete);
        	 	
        	}
        	else {
        		setStatus("could remove please select special date!", true);
        	}
  
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
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                if (res.getResource() == entities.ResourceType.BUSINESS_HOUR) {
                    switch (res.getAction()) {
                        case CREATE:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                                OpeningHours data = (OpeningHours) res.getData();
                                if(listEntry != null) {
                                	specialDatesModel.add(0, listEntry); 
	                                setStatus("Schedule updated successfully!", false);
	                                txtManageOpen.clear();
	                                txtManageClose.clear();
                                }
                                else
                                	 setStatus("Schedule update failed!", true);
                              
                            }
                            else
                            	setStatus("Could not remove from DB", true);
                            break;
                      
                        case UPDATE:
                        	   if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                        		   
                                   setStatus(" date has been updated! ", false);
                               }
                               else {
                                   setStatus("Could not remove from DB", true);
                               }
                               break;
                        case DELETE:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                            	if(this.specialDate != null) {
                            		specialDatesModel.remove(this.specialDate);
                                    setStatus("Special date removed. Reverted to standard hours.", false);
                                    
                            	}else
                            		setStatus("Special date could not be removed.", true);
                            }
                            else {
                                setStatus("Could not remove from DB", true);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

}