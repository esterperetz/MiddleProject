package clientGui.reservation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import entities.Alarm;
import entities.Employee;
import entities.Employee.Role;
import entities.Order;
import entities.Response;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class OrderUi_controller extends MainNavigator implements MessageListener<Object> {

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private ComboBox<String> cmbStatusFilter;

    // --- Table Columns ---
    @FXML
    private TableColumn<Order, Integer> Order_numberColumn;
    @FXML
    private TableColumn<Order, String> clientNameColumn;
    @FXML
    private TableColumn<Order, String> clientPhoneColumn;
    @FXML
    private TableColumn<Order, String> clientEmailColumn;
    @FXML
    private TableColumn<Order, Integer> customer_idColumn;
    @FXML
    private TableColumn<Order, Date> DateColumn;
    @FXML
    private TableColumn<Order, Date> arrivalTimeColumn;
    @FXML
    private TableColumn<Order, Integer> itemColumn;
    @FXML
    private TableColumn<Order, Double> totalPriceColumn;
    @FXML
    private TableColumn<Order, Order.OrderStatus> statusColumn;
    @FXML
    private TableColumn<Order, Integer> confirmation_codeColumn;
    @FXML
    private TableColumn<Order, Date> date_of_placing_orderColumn;

    private FilteredList<Order> filteredData;
    private ObservableList<Order> orderData = FXCollections.observableArrayList();
    private OrderLogic orderLogic;
    private String ip;
    private Employee.Role isManager;
    private Employee emp;

    public OrderUi_controller() {
    }

    @FXML
    private javafx.scene.layout.AnchorPane rootPane;

    @FXML
    private void initialize() {
        // Fade In Animation
        if (rootPane != null) {
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300),
                    rootPane);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }

        // Setup Columns
        Order_numberColumn
                .setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getOrderNumber()));
        clientNameColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty((cellData.getValue()).getCustomer().getName()));
        clientPhoneColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty((cellData.getValue()).getCustomer().getPhoneNumber()));
        clientEmailColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty((cellData.getValue()).getCustomer().getEmail()));
        customer_idColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getCustomer().getCustomerId()));
        DateColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getOrderDate()));
        arrivalTimeColumn
                .setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getArrivalTime()));
        itemColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getNumberOfGuests()));
        totalPriceColumn
                .setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getTotalPrice()));
        statusColumn
                .setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getOrderStatus()));
        confirmation_codeColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getConfirmationCode()));
        date_of_placing_orderColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>((cellData.getValue()).getDateOfPlacingOrder()));

        setupEditableColumns();

        // Setup Status ComboBox
        if (cmbStatusFilter != null) {
            cmbStatusFilter.setItems(FXCollections.observableArrayList(
                    "ALL", "PENDING", "APPROVED", "SEATED", "CANCELLED","PAID"));
            cmbStatusFilter.getSelectionModel().select("ALL");
        }

        // Setup Filtering
        filteredData = new FilteredList<>(orderData, p -> true);
        orderTable.setItems(filteredData);

        if (filterDatePicker != null) {
            filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        }
        if (cmbStatusFilter != null) {
            cmbStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        }
    }

    private void updateFilter() {
        filteredData.setPredicate(order -> {
            boolean dateMatch = true;
            if (filterDatePicker.getValue() != null) {
                if (order.getOrderDate() == null) {
                    dateMatch = false;
                } else {
                    LocalDate orderDate;
                    if (order.getOrderDate() instanceof java.sql.Date) {
                        orderDate = ((java.sql.Date) order.getOrderDate()).toLocalDate();
                    } else {
                        orderDate = order.getOrderDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    }
                    LocalDate selectedDate = filterDatePicker.getValue();
                    LocalDate today = LocalDate.now();

                    boolean isAfterSelection = !orderDate.isBefore(selectedDate);
                    boolean isBeforeToday = !orderDate.isAfter(today);
                    dateMatch = isAfterSelection && isBeforeToday;
                }
            }

            boolean statusMatch = true;
            if (cmbStatusFilter != null && cmbStatusFilter.getValue() != null) {
                String selectedStatus = cmbStatusFilter.getValue();
                if (!selectedStatus.equals("ALL")) {
                    String orderStatusStr = order.getOrderStatus().toString();
                    if (!orderStatusStr.equalsIgnoreCase(selectedStatus)) {
                        statusMatch = false;
                    }
                }
            }
            return dateMatch && statusMatch;
        });
    }

    @FXML
    private void handleClearFilter(ActionEvent event) {
        if (filterDatePicker != null)
            filterDatePicker.setValue(null);
        if (cmbStatusFilter != null)
            cmbStatusFilter.getSelectionModel().select("ALL");
    }

    public void initData(Employee emp, ClientUi clientUi, Role isManager) {
        this.emp = emp;
        this.clientUi = clientUi;
        this.isManager = isManager;
        this.ip = clientUi.getIp();

        orderLogic = new OrderLogic(clientUi);

        System.out.println("Initialization: Requesting all orders...");
        orderLogic.getAllOrders();

        Platform.runLater(() -> {
            Stage stage = (Stage) orderTable.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("Closing client...");
                clientUi.disconnectClient();
                System.exit(0);
            });
        });
    }

    public void refreshTableData() {
        System.out.println("LOG: Refreshing Order Table data from server.");
        orderLogic.getAllOrders();
    }

    // --- REFACTORED Message Handling ---

    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                handleResponse((Response) msg);
            } else if (msg instanceof String) {
                handleStringMessage((String) msg);
            }
        });
    }

    /**
     * Main handler for Response objects from server.
     */
    private void handleResponse(Response res) {
        switch (res.getAction()) {
            case GET_ALL:
                handleGetAllOrders(res.getData());
                break;
            case SEND_EMAIL:
                System.out.println((String) res.getData());
                break;
            case CREATE:
            case UPDATE:
            case DELETE:
                // Currently just placeholders or logic handled by refresh
                break;
            case GET_BY_ID:
                System.out.println("Received order: " + res.getData());
                break;
            default:
                handleOperationResult(res.getData());
                break;
        }
    }

    /**
     * Logic for parsing the list of orders from the server.
     */
    @SuppressWarnings("unchecked")
    private void handleGetAllOrders(Object data) {
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            orderData.clear();

            if (list.isEmpty()) {
                orderTable.refresh();
                return;
            }

            for (Object obj : list) {
                if (obj instanceof Map) {
                    Map<String, Object> row = (Map<String, Object>) obj;
                    Order o = new Order();

                    o.setOrderNumber((Integer) row.get("order_number"));
                    o.getCustomer().setCustomerId((Integer) row.get("customer_id"));
                    o.setNumberOfGuests((Integer) row.get("number_of_guests"));
                    o.setTotalPrice(((Number) row.get("total_price")).doubleValue());
                    o.setConfirmationCode((Integer) row.get("confirmation_code"));

                    // Enum Status
                    String statusStr = (String) row.get("order_status");
                    if (statusStr != null)
                        o.setOrderStatus(Order.OrderStatus.valueOf(statusStr));

                    // Dates
                    if (row.get("order_date") != null)
                        o.setOrderDate(new java.util.Date(((java.sql.Timestamp) row.get("order_date")).getTime()));

                    if (row.get("arrival_time") != null)
                        o.setArrivalTime(new java.util.Date(((java.sql.Timestamp) row.get("arrival_time")).getTime()));

                    if (row.get("date_of_placing_order") != null)
                        o.setDateOfPlacingOrder(
                                new java.util.Date(((java.sql.Timestamp) row.get("date_of_placing_order")).getTime()));

                    // Customer Details
                    o.getCustomer().setName((String) row.get("customer_name"));
                    o.getCustomer().setEmail((String) row.get("email"));
                    o.getCustomer().setPhoneNumber((String) row.get("phone_number"));

                    Object subCode = row.get("subscriber_code");
                    if (subCode != null) {
                        o.getCustomer().setSubscriberCode((Integer) subCode);
                    } else {
                        o.getCustomer().setSubscriberCode(0);
                    }
                    orderData.add(o);
                }
            }
            orderTable.refresh();
        }
    }

    /**
     * Logic for generic success/fail messages (Boolean or String).
     */
    private void handleOperationResult(Object data) {
        if (data instanceof Boolean) {
            boolean success = (Boolean) data;
            if (success) {
                Alarm.showAlert("Success", "Operation completed successfully!", Alert.AlertType.INFORMATION);
                orderLogic.getAllOrders(); // Refresh table
            } else {
                Alarm.showAlert("Failure", "Operation failed.", Alert.AlertType.ERROR);
            }
        } else if (data instanceof String) {
            Alarm.showAlert("Error", (String) data, Alert.AlertType.ERROR);
        }
    }

    /**
     * Logic for legacy string messages.
     */
    private void handleStringMessage(String text) {
        System.out.println("Message from server: " + text);
        if (text.contains("Disconnecting")) {
            clientUi.disconnectClient();
        }
    }

    // --- Button Actions ---

    @FXML
    private void handleAddOrder(ActionEvent event) {
        try {
            ReservationController controller = super.loadScreen("reservation/ReservationScreen", event, clientUi);
            if (controller != null)
                controller.initData(clientUi, null, 0, emp);
            else
                System.out.println("Error: moving screen ReservationController");
        } catch (Exception e) {
            String header = "Navigation Error";
            String context = "Could not load the Add Order screen.";
            Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateOrder(ActionEvent event) {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            Alarm.showAlert("No Selection", "Please select an order to update.", Alert.AlertType.WARNING);
            return;
        }

        try {
            UpdateOrder controller = super.loadScreen("reservation/updateOrder", event, this.clientUi);
            if (controller != null) {
                controller.initData(selectedOrder, orderLogic, this, this.isManager, emp);
            } else {
                System.err.println("Error: Could not load UpdateOrder controller.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alarm.showAlertWithException("Navigation Error", "Could not load the Update Order screen.",
                    Alert.AlertType.ERROR, e);
        }
    }

    @FXML
    private void handleDeleteOrder() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            orderLogic.deleteOrder(selectedOrder.getOrderNumber());
        } else {
            Alarm.showAlert("No Selection", "Please select an order to delete.", Alert.AlertType.WARNING);
        }
    }

    private void setupEditableColumns() {
        DateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
        itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        itemColumn.setOnEditCommit(event -> {
            Order o = event.getRowValue();
            o.setNumberOfGuests(event.getNewValue());
            orderLogic.updateOrder(o);
        });
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
}