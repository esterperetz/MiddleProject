package clientGui.reservation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import entities.ActionType;
import entities.Employee;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.WaitingList;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import client.MessageListener;

public class WaitingListController extends MainNavigator implements Initializable, MessageListener<Object> {
    
    @FXML
    private DatePicker filterDate;

    @FXML
    private TableView<WaitingList> waitingListTable; 

    @FXML
    private TableColumn<WaitingList, Integer> colPosition;
    @FXML
    private TableColumn<WaitingList, String> colName;
    @FXML
    private TableColumn<WaitingList, String> colPhone;
    @FXML
    private TableColumn<WaitingList, Integer> colGuests;
    @FXML
    private TableColumn<WaitingList, String> colTime;
    @FXML
    private TableColumn<WaitingList, String> colStatus;
    
    private Employee.Role isManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Mapping columns to WaitingList entity properties
        colPosition.setCellValueFactory(new PropertyValueFactory<>("waitingId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("identificationDetails")); 
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("enterTime"));
        
        // Static value for status column
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Waiting"));
    }

    /**
     * Triggered when a date is picked from the calendar.
     */
    @FXML
    void handleDateSelect(ActionEvent event) {
        LocalDate selectedDate = filterDate.getValue();
        if (selectedDate != null) {
            System.out.println("Filtering list for date: " + selectedDate);
            // Future implementation: Add server logic to filter by date if needed
        }
    }

    /**
     * Clears the date filter and reloads all entries.
     */
    @FXML
    void handleClearFilter(ActionEvent event) {
        filterDate.setValue(null);
        System.out.println("Filter cleared. Reloading all entries.");
        loadWaitingList();
    }

    @FXML
    void handleAssignTable(ActionEvent event) {
        System.out.println("Assign Table Clicked");
        
        WaitingList selected = waitingListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a customer to assign.");
            return;
        }
        
        // Send PROMOTE request to server
        Request req = new Request(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER, selected.getWaitingId(), null);
        clientUi.sendRequest(req);    }

    @FXML
    void handleRemoveEntry(ActionEvent event) {
        System.out.println("Remove Entry Clicked");
        
        WaitingList selected = waitingListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a customer to remove.");
            return;
        }
        
        // Send DELETE request to server
        Request req = new Request(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST, selected.getWaitingId(), null);
        clientUi.sendRequest(req);    }

    @FXML
    void handleBackBtn(ActionEvent event) {
        // Navigate back to the Manager Options screen
        ManagerOptionsController controller = 
                super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, this.isManager);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
    }

    public void initData(ClientUi c, Employee.Role isManager) {
        this.clientUi = c;
        this.isManager = isManager;
        
        // Initial load of data from server
        loadWaitingList();
    }
    
    private void loadWaitingList() {
        // Sending GET_ALL request to server
        Request req = new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL, null, null);
        clientUi.sendRequest(req);    }

    @Override
    public void onMessageReceive(Object msg) {
        // Always update GUI on the JavaFX Application Thread
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                
                // Case 1: Populate Table with Data
                if (res.getResource() == ResourceType.WAITING_LIST && res.getAction() == ActionType.GET_ALL) {
                    if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                        List<WaitingList> list = (List<WaitingList>) res.getData(); // Using getData()
                        ObservableList<WaitingList> data = FXCollections.observableArrayList(list);
                        waitingListTable.setItems(data);
                        waitingListTable.refresh();
                    }
                }
                
                // Case 2: Handle Promote/Delete responses (Refresh list on success)
                else if (res.getResource() == ResourceType.WAITING_LIST && 
                        (res.getAction() == ActionType.PROMOTE_TO_ORDER || res.getAction() == ActionType.EXIT_WAITING_LIST)) {
                    
                    if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                        loadWaitingList(); // Reload list to reflect changes
                        if (res.getMessage_from_server() != null) {
                            showAlert("Success", res.getMessage_from_server());
                        }
                    } else {
                        showAlert("Error", res.getMessage_from_server());
                    }
                }
            }
        });
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}