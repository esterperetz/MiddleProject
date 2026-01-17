package clientGui.managerTeam;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberHistoryController;
import clientLogic.UserLogic;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import entities.Response;
import entities.ResourceType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

/**
 * Controller for managing subscribers.
 * Displays a table of all registered subscribers and allows navigation to their
 * history.
 */
public class SubscriberManagementController extends MainNavigator implements Initializable, MessageListener<Object> {

    // --- FXML Elements for Table ---
    @FXML
    private TableView<Customer> subscriberTable;
    @FXML
    private TableColumn<Customer, Integer> colId;
    @FXML
    private TableColumn<Customer, String> colName;
    @FXML
    private TableColumn<Customer, String> colPhone;
    @FXML
    private TableColumn<Customer, String> colEmail;
    @FXML
    private TableColumn<Customer, String> colType;

    // --- Data & Logic ---
    private ClientUi clientUi;
    private Employee connectedEmployee;
    private Employee.Role role;
    private UserLogic userLogic;

    private ObservableList<Customer> subscriberList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     * Sets up the table columns and adds a double-click listener to open subscriber
     * history.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("subscriberCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        subscriberTable.setItems(subscriberList);

        subscriberTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Customer> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (!row.isEmpty() && mouseEvent.getButton() == MouseButton.PRIMARY
                        && mouseEvent.getClickCount() == 2) {
                    Customer clickedRow = row.getItem();
                    ActionEvent fakeEvent = new ActionEvent(subscriberTable, null);
                    openSubscriberHistory(clickedRow, fakeEvent);
                }
            });
            return row;
        });
    }

    /**
     * Initializes the controller with necessary data.
     * 
     * @param emp      The logged-in employee
     * @param clientUi The client UI instance
     * @param role     The role of the employee
     */
    public void initData(Employee emp, ClientUi clientUi, Employee.Role role) {
        this.clientUi = clientUi;
        this.connectedEmployee = emp;
        this.role = role;
        this.userLogic = new UserLogic(clientUi);

        refreshSubscribers();
    }

    /**
     * Refreshes the subscriber list by sending a request to the server.
     */
    private void refreshSubscribers() {
        userLogic.getAllSubscribers();
    }

    /**
     * Handles responses from the server.
     * Updates the subscriber table with the fetched data.
     */
    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;

                if (res.getResource() == ResourceType.CUSTOMER) {
                    switch (res.getAction()) {
                        case GET_ALL:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                                if (res.getData() instanceof List) {
                                    List<Customer> data = (List<Customer>) res.getData();
                                    subscriberList.setAll(data);

                                }
                            }
                            break;

                        case REGISTER_CUSTOMER:
                        case UPDATE:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                                showAlert("Success", (String) res.getMessage_from_server());
                                refreshSubscribers();
                            } else {
                                showAlert("Error", (String) res.getMessage_from_server());
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        });
    }

    /**
     * Opens the Subscriber History screen for the selected customer.
     * 
     * @param selectedCustomer The customer to view history for
     * @param event            The action event
     */
    private void openSubscriberHistory(Customer selectedCustomer, ActionEvent event) {
        SubscriberHistoryController subHistoryController = super.loadScreen("user/SubscriberHistory", event, clientUi);

        if (subHistoryController != null) {
            int subId = selectedCustomer.getSubscriberCode();

            CustomerType type = CustomerType.SUBSCRIBER;

            subHistoryController.initData(subId, type, connectedEmployee, selectedCustomer);
        }
    }

    /**
     * Navigates back to the Employee Options screen.
     */
    @FXML
    void goBackBtn(ActionEvent event) {
        ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
        if (controller != null) {
            controller.initData(connectedEmployee, clientUi, role);
        }
    }

    private void showAlert(String title, String content) {
        entities.Alarm.showAlert(title, content, Alert.AlertType.INFORMATION);
    }
}