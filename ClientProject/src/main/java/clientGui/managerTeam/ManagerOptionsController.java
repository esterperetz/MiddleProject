package clientGui.managerTeam;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.OrderUi_controller;
import clientGui.reservation.WaitingListController;
import clientGui.user.RegisterSubscriberController;
import clientLogic.EmployeeLogic;
import entities.Employee;
import entities.OpeningHours;
import entities.Response;
import entities.ResourceType;
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
    private TranslateTransition currentTransition;
    private Employee.Role isManager;
    private boolean isManagerFlag;
    private ObservableList<String> specialDatesModel;
    private Employee emp;
    @FXML private Pane tickerPane;
    @FXML private Label lblTicker;
    @FXML private javafx.scene.text.TextFlow tfTicker;

    @FXML private Button btnViewReports;
    @FXML private Button btnMonthlyReports;
    @FXML private Button btnViewSubscribers;
    @FXML private Button btnManageTables; 
    
    @FXML private Label lblDashboardTitle;
    @FXML private Label lblDashboardSubtitle;
    @FXML private Button btnSignUp;

    // --- Schedule Management UI (Right Side) ---
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
        
        // Ticker mask setup
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(tickerPane.widthProperty());
        clip.heightProperty().bind(tickerPane.heightProperty());
        tickerPane.setClip(clip);
        cbIsSpecial.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                cbIsClosed.setSelected(false);
            }
        });

        cbIsClosed.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                cbIsSpecial.setSelected(false); 
                
                txtManageOpen.clear();
                txtManageClose.clear();
            }
        });
    }
    
    private void initTicker() {
       employeeLogic.getAllOpeningHours();
    }

    private void startAnimation(double paneWidth) {
        if (currentTransition != null) {
            currentTransition.stop();
        }
        tfTicker.setTranslateX(paneWidth);
        tfTicker.applyCss();
        tfTicker.layout();
        double contentWidth = tfTicker.prefWidth(-1); 
        if (contentWidth <= 0) {
            contentWidth = tfTicker.getChildren().stream()
                    .mapToDouble(node -> node.getLayoutBounds().getWidth())
                    .sum() + 20;
        }

        currentTransition = new TranslateTransition();
        currentTransition.setNode(tfTicker);
        double totalDistance = paneWidth + contentWidth;
        double speedPixelsPerSecond = 80.0; 
        double durationSeconds = totalDistance / speedPixelsPerSecond;

        currentTransition.setDuration(Duration.seconds(durationSeconds));
        currentTransition.setFromX(paneWidth);
        currentTransition.setToX(-contentWidth);
        currentTransition.setCycleCount(Animation.INDEFINITE);
        currentTransition.setInterpolator(Interpolator.LINEAR);
        currentTransition.play();
    }

    public void initData(Employee emp, ClientUi clientUi, Employee.Role isManager) {
        this.clientUi = clientUi;
        this.emp = emp;
        employeeLogic = new EmployeeLogic(this.clientUi);
        if (isManager == Employee.Role.MANAGER) {
            this.isManager = Employee.Role.MANAGER;
            this.isManagerFlag = true;
            
            setButtonVisible(btnViewReports, true);
            setButtonVisible(btnSignUp, true);
            setButtonVisible(btnMonthlyReports, true);
            setButtonVisible(btnManageTables, true); // Show table management

            lblDashboardTitle.setText("Hello Manager, " + emp.getUserName());
            lblDashboardSubtitle.setText("Manager Dashboard - Full Access");
        } else {
            this.isManagerFlag = false;
            this.isManager = Employee.Role.REPRESENTATIVE;
            
            // --- Employee Permissions ---
            setButtonVisible(btnViewReports, false);
            setButtonVisible(btnSignUp, false);
            setButtonVisible(btnMonthlyReports, false);
            setButtonVisible(btnManageTables, false); // Hide table management

            lblDashboardTitle.setText("Hello, " + emp.getUserName());
            lblDashboardSubtitle.setText("Employee Dashboard");
        }

        if (this.clientUi == null) {
            System.err.println("Error: ClientUi is null in ManagerOptionsController!");
            return;
        }
        initTicker();
    }

    // Helper to safely set visibility
    private void setButtonVisible(Button btn, boolean isVisible) {
        if (btn != null) {
            btn.setVisible(isVisible);
            btn.setManaged(isVisible);
        }
    }

    // --- Navigation Methods ---
    @FXML
    void goToSubscriberManagementBtn(ActionEvent event) {
        SubscriberManagementController controller = super.loadScreen("managerTeam/SubscriberManagement", event, clientUi);
        if (controller != null) {
            controller.initData(emp, clientUi, isManager);
        } else {
            System.err.println("Error: Could not load SubscriberManagementController.");
        }
    }
    @FXML
    void goToTableManagementBtn(ActionEvent event) {
        TableManagementController controller = super.loadScreen("managerTeam/TableManagement", event, clientUi);
        if (controller != null) {
            controller.initData(emp, clientUi);
        } else {
            System.err.println("Failed to load TableManagement screen.");
        }
    }

    // ... (Keep existing navigation methods: goToWaitingListBtn, goToOrderDetailsBtn, etc.) ...
    @FXML
    void goToWaitingListBtn(ActionEvent event) {
        WaitingListController waiting_list = super.loadScreen("reservation/WaitingList", event, clientUi);
        if (waiting_list != null) waiting_list.initData(emp, this.clientUi, this.isManager);
    }

    @FXML
    void goToMonthlyReportsBtn(ActionEvent event) {
         MonthlyReportsController m = super.loadScreen("managerTeam/MonthlyReports", event, clientUi);
         if (m != null) m.initData(this.emp, this.clientUi, this.isManager); 
    }

    @FXML
    void goToOrderDetailsBtn(ActionEvent event) {
        OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
        if (controller != null) controller.initData(emp, this.clientUi, this.isManager);
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
        if(r != null) r.initData(emp, this.clientUi, this.isManager);
    }

    @FXML
    void goToReportsBtn(ActionEvent event) {
        ReportsController r = super.loadScreen("managerTeam/ReportsScreen", event, clientUi);
        if (r != null) r.initData(emp, this.clientUi, this.isManager);
    }

    @FXML
    void goBackBtn(ActionEvent event) {
        super.loadScreen("navigation/SelectionScreen", event, clientUi);
    }

    // --- Schedule Logic (Keep existing methods: updateScheduleBtn, removeSpecialDateBtn) ---
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

        if (!isClosed) { 
            if (openTimeStr == null || openTimeStr.trim().isEmpty() || 
                closeTimeStr == null || closeTimeStr.trim().isEmpty()) {
                
                setStatus("Cannot update: Time fields are empty!", true);
                return; 
            }
        }

        try {
            Time sqlOpenTime = null;
            Time sqlCloseTime = null;

            if (!isClosed) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
                LocalTime localOpen = LocalTime.parse(openTimeStr, formatter);
                LocalTime localClose = LocalTime.parse(closeTimeStr, formatter);
                
                if (localClose.isBefore(localOpen)) {
                    setStatus("Closing time cannot be before opening time.", true);
                    return;
                }
                
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
            setStatus("Invalid time format! Use HH:mm (e.g., 08:00)", true);
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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                java.sql.Date dateToDelete = java.sql.Date.valueOf(localDate);
                this.specialDate = selectedItem;
                employeeLogic.cancelOpeningHours(dateToDelete);
            } else {
                setStatus("could not remove please select special date!", true);
            }
        } else {
            setStatus("Select an item to remove", true);
        }
    }

    // --- Message Handling (REFACTORED) ---

    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                if (res.getResource() == ResourceType.BUSINESS_HOUR) {
                    handleBusinessHourResponse(res);
                }
            }
        });
    }

    /**
     * Handles all business hour related responses (Splits the switch case logic).
     */
    private void handleBusinessHourResponse(Response res) {
        switch (res.getAction()) {
            case GET_ALL:
                handleGetAllResponse(res);
                break;
            case CREATE:
                handleCreateResponse(res);
                break;
            case UPDATE:
                handleUpdateResponse(res);
                break;
            case DELETE:
                handleDeleteResponse(res);
                break;
            default:
                break;
        }
    }

    private void handleGetAllResponse(Response res) {
        if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
            List<OpeningHours> listOp = (ArrayList<OpeningHours>)res.getData();
            updateTickerFromList(listOp);
        }
    }

    private void handleCreateResponse(Response res) {
        if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
            if(listEntry != null) {
                specialDatesModel.add(0, listEntry); 
                setStatus("Schedule updated successfully!", false);
                txtManageOpen.clear();
                txtManageClose.clear();
            } else {
                setStatus("Schedule updated successfully (No list entry).", false);
            }
        } else {
            setStatus("Could not create/update schedule", true);
        }
    }

    private void handleUpdateResponse(Response res) {
        if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
            setStatus("Date has been updated!", false);
        } else {
            setStatus("Could not update", true);
        }
    }

    private void handleDeleteResponse(Response res) {
        if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
            if(this.specialDate != null) {
                specialDatesModel.remove(this.specialDate);
                setStatus("Special date removed.", false);
            } else {
                setStatus("Special date could not be removed (Ref mismatch).", true);
            }
        } else {
            setStatus("Could not remove from DB", true);
        }
    }

    // --- UI Helper Methods ---

    private void setStatus(String msg, boolean isError) {
        lblHoursStatus.setText(msg);
        lblHoursStatus.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
    }

    private String getDayShortName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Sun";
            case 2: return "Mon";
            case 3: return "Tue";
            case 4: return "Wed";
            case 5: return "Thu";
            case 6: return "Fri";
            case 7: return "Sat";
            default: return "Day" + dayOfWeek;
        }
    }

    private void updateTickerFromList(List<OpeningHours> listOp) {
        if (listOp == null || listOp.isEmpty()) {
            Platform.runLater(() -> {
                tfTicker.getChildren().clear();
                Text t = new Text("No opening hours available.");
                t.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");
                tfTicker.getChildren().add(t);
            });
            return;
        }
        listOp.sort((o1, o2) -> Integer.compare(o1.getDayOfWeek(), o2.getDayOfWeek()));
        Platform.runLater(() -> {
            tfTicker.getChildren().clear(); 
            for (OpeningHours oh : listOp) {
                boolean isHoliday = (oh.getSpecialDate() != null);
                StringBuilder sb = new StringBuilder();
                String dayName = getDayShortName(oh.getDayOfWeek());
                sb.append(dayName).append(": ");
                if (oh.isClosed()) {
                    sb.append("CLOSED");
                } else {
                    String start = oh.getOpenTime().toString();
                    String end = oh.getCloseTime().toString();
                    if (start.length() >= 5) start = start.substring(0, 5);
                    if (end.length() >= 5) end = end.substring(0, 5);
                    sb.append(start).append("-").append(end);
                }
                if (isHoliday) sb.append(" (HOLIDAY)");
                sb.append("   •   "); 
                Text textNode = new Text(sb.toString());
                if (isHoliday) {
                    textNode.setStyle("-fx-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
                } else {
                    textNode.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");
                }
                tfTicker.getChildren().add(textNode);
            }
            if (tickerPane.getWidth() > 0) {
                startAnimation(tickerPane.getWidth());
            } else {
                tickerPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() > 0) startAnimation(newVal.doubleValue());
                });
            }
        });
    }
}