package clientGui.user;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.managerTeam.SubscriberManagementController;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import entities.Order;
import entities.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for viewing a subscriber's order history.
 */
public class SubscriberHistoryController extends MainNavigator implements MessageListener<Object>, Initializable {

    @FXML
    private TableView<OrderHistoryItem> ordersTable;
    @FXML
    private TableColumn<OrderHistoryItem, Integer> colOrderId;
    @FXML
    private TableColumn<OrderHistoryItem, String> colDate;
    @FXML
    private TableColumn<OrderHistoryItem, String> colTime;
    @FXML
    private TableColumn<OrderHistoryItem, String> colTotal;
    @FXML
    private TableColumn<OrderHistoryItem, String> colStatus;
    @FXML
    private DatePicker filterDatePicker;

    private ObservableList<OrderHistoryItem> fullDataList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private OrderLogic orderLogic;
    private int currentSubscriberId;
    private CustomerType isSubscriber;
    private Employee employee;
    private Customer customer;

    /**
     * Initializes the table columns and filter listener.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleDateFilter(null);
        });
    }

    /**
     * Initializes data for the controller and requests history from the server.
     * 
     * @param subscriberId The ID of the subscriber
     * @param isSubscriber The customer type
     * @param employee     The employee object (if accessed by an employee)
     * @param customer     The customer object
     */
    public void initData(int subscriberId, CustomerType isSubscriber, Employee employee, Customer customer) {
        this.isSubscriber = isSubscriber;
        this.currentSubscriberId = subscriberId;
        this.orderLogic = new OrderLogic(clientUi);
        this.customer = customer;
        this.employee = employee;
        System.out.println("Fetching history for subscriber: " + subscriberId);
        orderLogic.getOrdersBySubscriberId(subscriberId);
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        filterDatePicker.setValue(null);
        ordersTable.setItems(fullDataList);
    }

    @FXML
    void handleDateFilter(ActionEvent event) {
        LocalDate selectedDate = filterDatePicker.getValue();

        if (selectedDate == null) {
            ordersTable.setItems(fullDataList);
            return;
        }

        ObservableList<OrderHistoryItem> filteredList = fullDataList.stream()
                .filter(item -> {
                    if (item.getDate() == null || item.getDate().isEmpty())
                        return false;
                    try {
                        LocalDate itemDate = LocalDate.parse(item.getDate(), formatter);
                        return itemDate.isEqual(selectedDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(javafx.collections.FXCollections::observableArrayList,
                        javafx.collections.ObservableList::add,
                        javafx.collections.ObservableList::addAll);

        ordersTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            ordersTable.setPlaceholder(
                    new javafx.scene.control.Label("No orders found after " + selectedDate.format(formatter)));
        }
    }

    /**
     * Navigates back.
     * 
     * @param event The action event
     */
    @FXML
    void goBackBtn(ActionEvent event) {
        if (employee != null) {
            SubscriberManagementController controller = super.loadScreen("managerTeam/SubscriberManagement", event,
                    clientUi);
            if (controller != null) {
                controller.initData(employee, clientUi, employee.getRole());
            } else
                System.out.println("Error in loading Manager options");
        } else {
            SubscriberOptionController subscriberOptionController = super.loadScreen("user/SubscriberOption", event,
                    clientUi);
            if (subscriberOptionController != null)
                subscriberOptionController.initData(clientUi, isSubscriber, currentSubscriberId, customer);
            else
                System.err.println("Error loading subscriber option");
        }
    }

    @Override
    public void onMessageReceive(Object msg) {
        try {
            Platform.runLater(() -> {
                if (msg instanceof Response) {
                    Response res = (Response) msg;

                    if (res.getAction() == ActionType.GET_ALL_BY_SUBSCRIBER_ID) {
                        if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                            if (res.getData() instanceof List) {
                                List<?> rawData = (List<?>) res.getData();
                                updateTable(rawData);
                            }
                        } else {
                            System.err.println("Error fetching history: " + res.getMessage_from_server());
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTable(List<?> rawData) {
        fullDataList.clear();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Object obj : rawData) {
            try {
                int orderId = 0;
                Date orderDate = null;
                double totalPrice = 0.0;
                String status = "";

                if (obj instanceof Order) {
                    Order o = (Order) obj;
                    orderId = o.getOrderNumber();
                    orderDate = o.getOrderDate();
                    totalPrice = o.getTotalPrice();
                    status = (o.getOrderStatus() != null) ? o.getOrderStatus().toString() : "UNKNOWN";
                } else if (obj instanceof Map) {
                    Map<String, Object> row = (Map<String, Object>) obj;

                    if (row.get("orderNumber") != null)
                        orderId = (Integer) row.get("orderNumber");
                    else if (row.get("order_number") != null)
                        orderId = (Integer) row.get("order_number");

                    Object dateObj = row.get("orderDate");
                    if (dateObj == null)
                        dateObj = row.get("order_date");

                    if (dateObj instanceof java.sql.Timestamp) {
                        orderDate = new Date(((java.sql.Timestamp) dateObj).getTime());
                    } else if (dateObj instanceof Date) {
                        orderDate = (Date) dateObj;
                    }

                    Object priceObj = row.get("totalPrice");
                    if (priceObj == null)
                        priceObj = row.get("total_price");

                    if (priceObj instanceof Number) {
                        totalPrice = ((Number) priceObj).doubleValue();
                    }

                    Object statusObj = row.get("orderStatus");
                    if (statusObj == null)
                        statusObj = row.get("order_status");
                    status = (statusObj != null) ? statusObj.toString() : "UNKNOWN";
                }

                if (orderDate != null) {
                    LocalDate localDate = orderDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    String dateStr = localDate.format(formatter);
                    String timeStr = orderDate.toInstant().atZone(ZoneId.systemDefault()).format(timeFormatter);
                    String priceStr = String.format("%.2f ₪", totalPrice);

                    fullDataList.add(new OrderHistoryItem(orderId, dateStr, timeStr, priceStr, status));
                }

            } catch (Exception e) {
                System.err.println("Error parsing row: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (filterDatePicker.getValue() != null) {
            handleDateFilter(null);
        } else {
            ordersTable.setItems(fullDataList);
        }
    }

    /**
     * Inner class representing a row in the history table.
     */
    public static class OrderHistoryItem {
        private int orderId;
        private String date;
        private String time;
        private String total;
        private String status;

        public OrderHistoryItem(int orderId, String date, String time, String total, String status) {
            this.orderId = orderId;
            this.date = date;
            this.time = time;
            this.total = total;
            this.status = status;
        }

        public int getOrderId() {
            return orderId;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getTotal() {
            return total;
        }

        public String getStatus() {
            return status;
        }
    }
}