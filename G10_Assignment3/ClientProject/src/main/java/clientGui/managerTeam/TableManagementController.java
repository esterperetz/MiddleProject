package clientGui.managerTeam;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.TableLogic;
import entities.ActionType;
import entities.Employee;
import entities.Response;
import entities.Table;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for managing restaurant tables.
 * Allows adding, updating, and deleting tables, as well as viewing their
 * current status.
 */
public class TableManagementController extends MainNavigator implements Initializable, MessageListener<Object> {

    @FXML
    private TableView<Table> tablesUserId;
    @FXML
    private TableColumn<Table, Integer> colTableNumber;
    @FXML
    private TableColumn<Table, Integer> colSeats;
    @FXML
    private TableColumn<Table, String> colStatus;

    @FXML
    private TextField txtTableNumber;
    @FXML
    private TextField txtSeats;

    private ClientUi clientUi;
    private Employee emp;
    private TableLogic tableLogic;
    private ObservableList<Table> tableList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     * Sets up table columns and adds a listener to populate fields when a table is
     * selected.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTableNumber.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("numberOfSeats"));
        colStatus.setCellValueFactory(cellData -> {
            Boolean occupied = cellData.getValue().getIsOccupied();
            return new javafx.beans.property.SimpleStringProperty(occupied != null && occupied ? "Occupied" : "Free");
        });

        tablesUserId.setItems(tableList);

        // Listen for selection to populate fields
        tablesUserId.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtTableNumber.setText(String.valueOf(newVal.getTableNumber()));
                txtSeats.setText(String.valueOf(newVal.getNumberOfSeats()));
                txtTableNumber.setDisable(true);
            } else {
                txtTableNumber.setDisable(false);
                txtTableNumber.clear();
                txtSeats.clear();
            }
        });
    }

    /**
     * Initializes the controller with necessary data.
     * 
     * @param emp      The logged-in employee
     * @param clientUi The client UI instance
     */
    public void initData(Employee emp, ClientUi clientUi) {
        this.emp = emp;
        this.clientUi = clientUi;
        this.tableLogic = new TableLogic(clientUi);
        // clientUi.addListener(this); // Listen for server responses
        refreshTable();
    }

    /**
     * Sends a request to fetch all tables from the server.
     */
    private void refreshTable() {
        tableLogic.getAllTables();
    }

    /**
     * Handles adding a new table.
     * Validates input and sends a create request to the server.
     */
    @FXML
    void handleAddTable(ActionEvent event) {
        try {
            int tableNum = Integer.parseInt(txtTableNumber.getText());
            int seats = Integer.parseInt(txtSeats.getText());

            Table newTable = new Table(tableNum, seats, false); // false = Free
            tableLogic.createTable(newTable);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Table Number and Seats must be valid integers.");
        }
    }

    /**
     * Handles updating an existing table.
     * Updates the number of seats for the selected table.
     */
    @FXML
    void handleUpdateTable(ActionEvent event) {
        try {
            int tableNum = Integer.parseInt(txtTableNumber.getText());
            int seats = Integer.parseInt(txtSeats.getText());

            // We only update seats here for simplicity, assuming status is managed by
            // system
            // To update, we should fetch the existing one or just send the update request
            Table updatedTable = new Table(tableNum, seats, false);
            // Ideally we'd keep the old status. For now, let's assume we send the object
            // and server handles it.
            // Or better, get selected item status if available.
            Table selected = tablesUserId.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getTableNumber() == tableNum) {
                updatedTable.setIsOccupied(selected.getIsOccupied());
            }

            tableLogic.updateTable(updatedTable);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Table Number and Seats must be valid integers.");
        }
    }

    /**
     * Handles deleting a table.
     * Sends a delete request for the selected table.
     */
    @FXML
    void handleDeleteTable(ActionEvent event) {
        Table selected = tablesUserId.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tableLogic.deleteTable(selected.getTableNumber());
        } else {
            showAlert("Selection Error", "Please select a table to delete.");
        }
    }

    /**
     * Clears the input fields and deselects any table.
     */
    @FXML
    void handleClearBtn(ActionEvent event) {
        clearFields();
    }

    /**
     * Navigates back to the Employee Options screen.
     */
    @FXML
    void handleBackBtn(ActionEvent event) {
        // clientUi.removeListener(this);
        ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
        if (controller != null) {
            controller.initData(emp, clientUi, emp.getRole());
        }
    }

    private void showAlert(String title, String content) {
        entities.Alarm.showAlert(title, content, Alert.AlertType.INFORMATION);
    }

    /**
     * Handles responses from the server.
     * Updates the table list or displays success/error alerts.
     */
    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                if (res.getResource() == entities.ResourceType.TABLE) {
                    switch (res.getAction()) {
                        case GET_ALL:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                                List<Table> data = (List<Table>) res.getData();
                                tableList.setAll(data);
                            }
                            break;
                        case CREATE:
                        case UPDATE:
                        case DELETE:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                                showAlert("Success", (String) res.getMessage_from_server());
                                refreshTable(); // Refresh list after modification
                                clearFields();
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

    private void clearFields() {
        txtTableNumber.clear();
        txtSeats.clear();
        txtTableNumber.setDisable(false);
        tablesUserId.getSelectionModel().clearSelection();
    }
}
