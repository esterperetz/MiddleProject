package clientGui.reservation;

import java.net.URL;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import Entities.Alarm;
import Entities.Order;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class OrderUi_controller implements  MessageListener<Object> {

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> Order_numberColumn;
    @FXML private TableColumn<Order, Date> DateColumn;
    @FXML private TableColumn<Order, Integer> itemColumn; // Guests
    @FXML private TableColumn<Order, Integer> confirmation_codeColumn;
    @FXML private TableColumn<Order, Integer> subscriber_idColumn;
    @FXML private TableColumn<Order, Date> date_of_placing_orderColumn;
    private ObservableList<Order> orderData = FXCollections.observableArrayList();
    private ClientUi clientUi;
    private OrderLogic orderLogic;
    private String ip;
    public OrderUi_controller() {
    }

    @FXML
    private void initialize() {
        Order_numberColumn.setCellValueFactory(new PropertyValueFactory<>("order_number"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));
        confirmation_codeColumn.setCellValueFactory(new PropertyValueFactory<>("confirmation_code"));
        subscriber_idColumn.setCellValueFactory(new PropertyValueFactory<>("subscriber_id"));

        date_of_placing_orderColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_placing_order"));
        
        setupEditableColumns();
        orderTable.setItems(orderData);
    }

    /**
     * Initializes this controller with an existing ClientUi and server IP.
     * Registers this controller as a listener and loads all orders from the server.
     *
     * @param clientUi The client UI used for server communication.
     * @param ip       The server IP address.
     */
    public void initData(ClientUi clientUi, String ip) {
        this.clientUi = clientUi;
        this.ip = ip;

        clientUi.addListener(this);
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
 
    
    /**
     * Sets the ClientUi instance used by this controller.
     *
     * @param c The ClientUi to use.
     */
    public void setClient(ClientUi c)
    {
    	clientUi=c;
    }

    
    /**
     * Refreshes table data by requesting all orders from the server
     * Useful after an add, update or delete action.
     */
    public void refreshTableData() {
        System.out.println("LOG: Refreshing Order Table data from server.");
        orderLogic.getAllOrders(); 
    }

   
     //Called when a message is received from the server.
    @SuppressWarnings("unchecked")
    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            
        	// Case 1: GET_ALL response
            if (msg instanceof List) {
                List<?> list = (List<?>) msg;
                if (list.isEmpty() || list.get(0) instanceof Order) {
                    List<Order> incomingOrders = (List<Order>) msg;
                    orderData.clear();
                    orderData.addAll(incomingOrders);
                    System.out.println("Updated table with " + incomingOrders.size() + " orders.");
                } else {
                    System.out.println("Received a list, but it's not orders.");
                }
            }
            
        	// Case 2: Single Order object
            else if (msg instanceof Order) {
                Order o = (Order) msg;
                System.out.println("Received single order: " + o);
            }
            
        	// Case 3: String message
            else if (msg instanceof String) {
                String text = (String) msg;
                System.out.println("Message from server: " + text);
                if (text.contains("Disconnecting")) {
                    clientUi.disconnectClient();
                }
            }
            
        	// Case 4: Boolean (Success/Failure)
            else if (msg instanceof Boolean) {
                boolean success = (Boolean) msg;
                if (success) {
                	String header="Success";
                	String context="Operation completed successfully!";
                	Alarm.showAlert(header,context,Alert.AlertType.INFORMATION);
                    //showAlert("Success", "Operation completed successfully!", Alert.AlertType.INFORMATION);
                    // Refresh table after success
                    orderLogic.getAllOrders(); 
                } else {
                	String header="Failure";
                	String context="Operation failed.";
                	Alarm.showAlert(header,context,Alert.AlertType.ERROR);
                    //showAlert("Failure", "Operation failed.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleAddOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/addOrder.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Order");
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
        	String header="Navigation Error";
        	String context="Could not load the Add Order screen. Check if addOrder.fxml exists.";
        	Alarm.showAlertWithException(header,context,Alert.AlertType.ERROR,e);
            //showAlert("Navigation Error", "Could not load the Add Order screen. Check if addOrder.fxml exists.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateOrder(ActionEvent event) {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
        	String header="No Selection";
        	String context="Please select an order to update.";
        	Alarm.showAlert(header,context,Alert.AlertType.WARNING);
            //showAlert("No Selection", "Please select an order to update.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/updateOrder.fxml"));
            Parent root = loader.load();
            UpdateOrder controller = loader.getController();
            // Passing the selected order, OrderLogic, AND THIS controller reference for refresh
            controller.initData(selectedOrder, orderLogic, this); 

            Stage stage = new Stage();
            stage.setTitle("Update Order #" + selectedOrder.getOrder_number());
            stage.setScene(new Scene(root));
            stage.show();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            String header="Navigation Error";
        	String context="Could not load the Update Order screen.";
        	Alarm.showAlertWithException(header,context,Alert.AlertType.ERROR,e);
            //showAlert("Navigation Error", "Could not load the Update Order screen.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteOrder() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            int orderIdToDelete = selectedOrder.getOrder_number();
            orderLogic.deleteOrder(orderIdToDelete); 
        } else {
        	String header="No Selection";
        	String context="Please select an order to delete.";
        	Alarm.showAlert(header,context,Alert.AlertType.WARNING);
            //showAlert("No Selection", "Please select an order to delete.", Alert.AlertType.WARNING);
        }
    }

    /**
     *  Makes some table columns editable and sends updates to the server
     * when the user changes the number of guests in a row.
     */
    private void setupEditableColumns() {
        DateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
        itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        
        itemColumn.setOnEditCommit(event -> {
            Order o = event.getRowValue();
            o.setNumber_of_guests(event.getNewValue());
            orderLogic.updateOrder(o); 
        });
    }
    @FXML
    void handleBackBtn()
    {
    	MainNavigator.loadScene("managerTeam/workerOption");
    }

    /*
    /**
     * @param title
     * @param content
     * @param type
     * Shows a simple alert dialog with a title, content and type.
     
    public void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    */
}