package clientGui.reservation;

import java.net.URL;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import entities.ActionType;
import entities.Alarm;
import entities.Order;
import entities.Request;
import entities.Response;
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

public class OrderUi_controller extends MainNavigator implements MessageListener<Object> {

	@FXML
    private TableView<Order> orderTable;
    
    // --- 1. משתנים התואמים ל-FXML החדש ---
    @FXML
    private TableColumn<Order, Integer> Order_numberColumn;
    
    @FXML
    private TableColumn<Order, String> clientNameColumn;  // החליף את fullNameColumn
    
    @FXML
    private TableColumn<Order, String> clientPhoneColumn; // החליף את identificationDetailsColumn
    
    @FXML
    private TableColumn<Order, String> clientEmailColumn; // שדה חדש!
    
    @FXML
    private TableColumn<Order, Integer> subscriber_idColumn;

    @FXML
    private TableColumn<Order, Date> DateColumn; // Order Date
    
    @FXML
    private TableColumn<Order, Date> arrivalTimeColumn;   // שדה חדש!
    
    @FXML
    private TableColumn<Order, Integer> itemColumn; // Guests
    
    @FXML
    private TableColumn<Order, Double> totalPriceColumn;
    
    @FXML
    private TableColumn<Order, Order.OrderStatus> statusColumn;
    
    @FXML
    private TableColumn<Order, Integer> confirmation_codeColumn;
    
    @FXML
    private TableColumn<Order, Date> date_of_placing_orderColumn;

    private ObservableList<Order> orderData = FXCollections.observableArrayList();
    private OrderLogic orderLogic;
    private String ip;
    private boolean isManager;
    public OrderUi_controller() {
    }

    @FXML
    private void initialize() {
        // --- 2. חיבור הנתונים לטבלה באמצעות Getters של Order החדש ---

        // מספר הזמנה
        Order_numberColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderNumber()));

        // שם לקוח (String)
        clientNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClientName()));

        // טלפון (String)
        clientPhoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClientPhone()));

        // אימייל (String)
        clientEmailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClientEmail()));

        // מזהה מנוי
        subscriber_idColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getSubscriberId()));

        // תאריך הזמנה
        DateColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderDate()));

        // שעת הגעה בפועל
        arrivalTimeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getArrivalTime()));

        // כמות אורחים
        itemColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getNumberOfGuests()));

        // מחיר כולל
        totalPriceColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalPrice()));

        // סטטוס (שים לב: getOrder_status ולא getStatus)
        statusColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderStatus()));

        // קוד אישור
        confirmation_codeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getConfirmationCode()));

        // תאריך יצירת ההזמנה
        date_of_placing_orderColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getDateOfPlacingOrder()));

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
	//public void initData(ClientUi clientUi, String ip) {
	public void initData(boolean isManager) {
		//this.clientUi = clientUi;
		this.isManager=isManager;
		this.ip = clientUi.getIp();

//		clientUi.addListener(this);
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
	 * Refreshes table data by requesting all orders from the server Useful after an
	 * add, update or delete action.
	 */
	
	
	
	
	public void refreshTableData() {
		System.out.println("LOG: Refreshing Order Table data from server.");
		orderLogic.getAllOrders();
	}

	// Called when a message is received from the server.
	@SuppressWarnings("unchecked")
	@Override
	public void onMessageReceive(Object msg) {
		Platform.runLater(() -> {
			if (msg instanceof Response) {
				Response res = (Response) msg;
				Object data = res.getData();
				//understand what the action of the requast
				switch (res.getAction()) {

				case GET_ALL:
					// Upload table with the list of orders
					if (data instanceof List) {
						List<?> list = (List<?>) data;
						if (list.isEmpty() || list.get(0) instanceof Order) {
							// List<Order> incomingOrders = (List<Order>) request.getPayload();
							orderData.clear();
							orderData.addAll((List<Order>) list);
							orderTable.refresh();
							System.out.println("Updated table with " + list.size() + " orders.");
						} else {
							System.out.println("Received a list, but it's not orders.");
						}
					}
					break;

				case GET_BY_ID:
					// Handle single order retrieval
					if (data instanceof Order) {
						System.out.println("Received order: " + data);
					}
					break;

				//case CREATE:
				//case UPDATE:
				//case DELETE:
				default:
					// Handle success or failure notification
					if (data instanceof Boolean) {
						boolean success = (Boolean) data;
						if (success) {
							showAlert("Success", "Operation completed successfully!", Alert.AlertType.INFORMATION);
		                    // Refresh table after success
		                    orderLogic.getAllOrders(); 
		                } else {
		                    showAlert("Failure", "Operation failed.", Alert.AlertType.ERROR);
		                }
					}
					// Handle specific text error from server
					else if (data instanceof String) {
						Alarm.showAlert("Error", (String) data, Alert.AlertType.ERROR);
					}
					break;
				}
			}

			// 2. Handle raw String messages (Legacy/System messages)
			else if (msg instanceof String) {
				String text = (String) msg;
                System.out.println("Message from server: " + text);
                if (text.contains("Disconnecting")) {
                    clientUi.disconnectClient();
				}

			}

		});
		// checking if the requast equal to action GET_ALL
		/*
		 * if (request.getAction() == ActionType.GET_ALL) { // Case 1: GET_ALL response
		 * if ( request.getPayload() instanceof List) { List<?> list = (List<?>)
		 * request.getPayload(); if (list.isEmpty() || list.get(0) instanceof Order) {
		 * List<Order> incomingOrders = (List<Order>) request.getPayload();
		 * orderData.clear(); orderData.addAll(incomingOrders);
		 * System.out.println("Updated table with " + incomingOrders.size() +
		 * " orders."); } else {
		 * System.out.println("Received a list, but it's not orders."); } }
		 * 
		 * // Case 2: Single Order object else if (request.getPayload() instanceof
		 * Order) { Order o = (Order) request.getPayload();
		 * System.out.println("Received single order: " + o); }
		 * 
		 * // Case 3: String message else if (request.getPayload() instanceof String) {
		 * String text = (String) request.getPayload();
		 * System.out.println("Message from server: " + text); if
		 * (text.contains("Disconnecting")) { clientUi.disconnectClient(); } }
		 * 
		 * // Case 4: Boolean (Success/Failure) else if (request.getPayload() instanceof
		 * Boolean) { boolean success = (Boolean) request.getPayload(); if (success) {
		 * String header = "Success"; String context =
		 * "Operation completed successfully!"; Alarm.showAlert(header, context,
		 * Alert.AlertType.INFORMATION); // showAlert("Success",
		 * "Operation completed successfully!", // Alert.AlertType.INFORMATION); //
		 * Refresh table after success orderLogic.getAllOrders(); } else { String header
		 * = "Failure"; String context = "Operation failed."; Alarm.showAlert(header,
		 * context, Alert.AlertType.ERROR); // showAlert("Failure", "Operation failed.",
		 * Alert.AlertType.ERROR); } } }
		 */

	}

	@FXML
	private void handleAddOrder(ActionEvent event) {
		try {
			//FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/addOrder.fxml"));
			//Parent root = loader.load();
			//Stage stage = new Stage();
			//stage.setTitle("Add New Order");
			//stage.setScene(new Scene(root));
			//stage.show();
			AddOrderController controller = 
	        		super.loadScreen("reservation/addOrder", event,this.clientUi);
			if (controller != null) {
	            controller.initData(this.isManager);
	        } else {
	            System.err.println("Error: Could not load AddOrderController.");
	        }
			

		} catch (Exception e) {
			String header = "Navigation Error";
			String context = "Could not load the Add Order screen. Check if addOrder.fxml exists.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			// showAlert("Navigation Error", "Could not load the Add Order screen. Check if
			// addOrder.fxml exists.", Alert.AlertType.ERROR);
			e.printStackTrace();
		}
	}

	@FXML
	private void handleUpdateOrder(ActionEvent event) {
		Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
		if (selectedOrder == null) {
			String header = "No Selection";
			String context = "Please select an order to update.";
			Alarm.showAlert(header, context, Alert.AlertType.WARNING);
			// showAlert("No Selection", "Please select an order to update.",
			// Alert.AlertType.WARNING);
			return;
		}

		try {
			UpdateOrder controller = 
	        		super.loadScreen("reservation/updateOrder", event,this.clientUi);
	    	if (controller != null) {
	            controller.initData(selectedOrder,orderLogic,this,this.isManager);
	        } else {
	            System.err.println("Error: Could not load ManagerOptionsController.");
	        }
	    	//FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/updateOrder.fxml"));
			//Parent root = loader.load();
			//super.loadScreen(, event, this.clientUi);
			//UpdateOrder controller = loader.getController();
			// Passing the selected order, OrderLogic, AND THIS controller reference for
			// refresh
			//controller.initData(selectedOrder, orderLogic, this);

			//Stage stage = new Stage();
			//stage.setTitle("Update Order #" + selectedOrder.getOrder_number());
			//stage.setScene(new Scene(root));
			//stage.show();
		} catch (Exception e) {
			e.printStackTrace();
			String header = "Navigation Error";
			String context = "Could not load the Update Order screen.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			// showAlert("Navigation Error", "Could not load the Update Order screen.",
			// Alert.AlertType.ERROR);
		}
	}

	@FXML
	private void handleDeleteOrder() {
		Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
		if (selectedOrder != null) {
			int orderIdToDelete = selectedOrder.getOrderNumber();
			orderLogic.deleteOrder(orderIdToDelete);
		} else {
			String header = "No Selection";
			String context = "Please select an order to delete.";
			Alarm.showAlert(header, context, Alert.AlertType.WARNING);
			// showAlert("No Selection", "Please select an order to delete.",
			// Alert.AlertType.WARNING);
		}
	}

	/**
	 * Makes some table columns editable and sends updates to the server when the
	 * user changes the number of guests in a row.
	 */
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
		// MainNavigator.loadScene("managerTeam/workerOption");
		ManagerOptionsController controller = super.loadScreen("managerTeam/workerOption", event, clientUi);
		if (controller != null) {
			//controller.initData(clientUi, ManagerOptionsController.isManager());
			controller.initData(clientUi, this.isManager);
		} else {
			System.err.println("Error: Could not load ManagerOptionsController.");
		}
	}

	/*
	 * /**
	 * 
	 * @param title
	 * 
	 * @param content
	 * 
	 * @param type Shows a simple alert dialog with a title, content and type.
	 * 
	 * public void showAlert(String title, String content, Alert.AlertType type) {
	 * Alert alert = new Alert(type); alert.setTitle(title);
	 * alert.setContentText(content); alert.showAndWait(); }
	 */
}