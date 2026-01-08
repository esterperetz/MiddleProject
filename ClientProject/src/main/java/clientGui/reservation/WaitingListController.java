package clientGui.reservation;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientLogic.WaitingListLogic;
import entities.ActionType;
import entities.Customer;
import entities.Employee;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.WaitingList;

import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import client.MessageListener;

public class WaitingListController extends MainNavigator implements Initializable, MessageListener<Object> {

    @FXML
    private DatePicker filterDate;

    private Employee.Role isManager;
    private Employee emp;

    @FXML
    private TableView<WaitingList> waitingListTable;

    @FXML
    private TableColumn<WaitingList, Integer> colWaitingId;
    @FXML
    private TableColumn<WaitingList, Integer> colCustomerId;
    @FXML
    private TableColumn<WaitingList, Integer> colGuests;
    @FXML
    private TableColumn<WaitingList, Date> colEnterTime; 
    @FXML
    private TableColumn<WaitingList, Integer> colConfirmationCode;

    @FXML
    private TableColumn<WaitingList, String> colCustomerName;
    @FXML
    private TableColumn<WaitingList, String> colCustomerPhone;
    @FXML
    private TableColumn<WaitingList, String> colCustomerEmail;

    private ObservableList<WaitingList> waitingListData = FXCollections.observableArrayList();

    private WaitingListLogic waitingListLogic;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        colWaitingId.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getWaitingId()));

        colCustomerId.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getCustomerId()));

        colGuests.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getNumberOfGuests()));

        colEnterTime.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getEnterTime()));

        colConfirmationCode.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getConfirmationCode()));

        colCustomerName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

        colCustomerPhone.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getPhoneNumber()));

        colCustomerEmail.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getEmail()));

        waitingListTable.setItems(waitingListData);
    }

    public void initData(Employee emp, ClientUi clientUi, Employee.Role isManager) {
        this.emp = emp;
        this.clientUi = clientUi;
        this.isManager = isManager;
        this.waitingListLogic  = new WaitingListLogic(clientUi);
        waitingListLogic.getAllWaitingListCustomer();
    }

  

    @SuppressWarnings("unchecked")
    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                Object data = res.getData();

                switch (res.getAction()) {
                
                case GET_ALL:
                    if (data instanceof List) {
                        List<?> list = (List<?>) data;
                        
                        waitingListData.clear();

                        if (list.isEmpty()) {
                            waitingListTable.refresh();
                            return;
                        }

                        for (Object obj : list) {
                            if (obj instanceof Map) {
                                Map<String, Object> row = (Map<String, Object>) obj;
                                
                                Customer customer = new Customer();
                                customer.setName((String) row.get("customer_name"));
                                customer.setPhoneNumber((String) row.get("phone_number"));
                                customer.setEmail((String) row.get("email"));
                                
                                if (row.get("customer_id") != null) {
                                    customer.setCustomerId((Integer) row.get("customer_id"));
                                }

                                Integer waitingId = (Integer) row.get("waiting_id"); // ודאי שזה השם ב-DB
                                Integer customerId = (Integer) row.get("customer_id");
                                Integer guests = (Integer) row.get("number_of_guests");
                                Integer code = (Integer) row.get("confirmation_code");

                                Date enterTime = null;
                                if (row.get("enter_time") != null) {
                                    enterTime = new java.util.Date(((java.sql.Timestamp) row.get("enter_time")).getTime());
                                }

                                WaitingList item = new WaitingList(waitingId, customerId, guests, enterTime, code, customer);
                                waitingListData.add(item);
                            }
                        }
                        waitingListTable.refresh();
                    }
                    break;

                case PROMOTE_TO_ORDER:
                case EXIT_WAITING_LIST:
                    if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                        if (res.getMessage_from_server() != null) {
                            showAlert("Success", res.getMessage_from_server());
                        }
                    } else {
                        showAlert("Error", res.getMessage_from_server());
                    }
                    break;
                    
                default:
                    break;
                }
            } 
            else if (msg instanceof String) {
                System.out.println("Message from server: " + msg);
            }
        });
    }


    @FXML
    void handleDateSelect(ActionEvent event) {
        LocalDate selectedDate = filterDate.getValue();
        if (selectedDate != null) {
            System.out.println("Filtering list for date: " + selectedDate);
        }
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        filterDate.setValue(null);
    }

    @FXML
    void handleAssignTable(ActionEvent event) {
        WaitingList selected = waitingListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a customer to assign.");
            return;
        }
        Request req = new Request(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER, selected.getWaitingId(), null);
        clientUi.sendRequest(req);
    }

    @FXML
    void handleRemoveEntry(ActionEvent event) {
        WaitingList selected = waitingListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a customer to remove.");
            return;
        }
        Request req = new Request(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST, selected.getWaitingId(), null);
        clientUi.sendRequest(req);
    }

    @FXML
    void handleBackBtn(ActionEvent event) {
        ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
        if (controller != null) {
            controller.initData(emp, clientUi, this.isManager);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}