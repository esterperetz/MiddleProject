package clientGui.reservation;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;

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
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
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
	@FXML
    private DatePicker filterDatePicker;
    
    // רשימה מסוננת שתהיה מקושרת לטבלה במקום הרשימה הרגילה
    private FilteredList<Order> filteredData;
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
        // --- 2. חיבור הנתונים לטבלה (הקוד הקיים שלך נשאר זהה) ---

        Order_numberColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderNumber()));

        clientNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClientName()));

        clientPhoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClientPhone()));

        clientEmailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getClientEmail()));

        subscriber_idColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getSubscriberId()));

        DateColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderDate()));

        arrivalTimeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getArrivalTime()));

        itemColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getNumberOfGuests()));

        totalPriceColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalPrice()));

        statusColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getOrderStatus()));

        confirmation_codeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getConfirmationCode()));

        date_of_placing_orderColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(cellData.getValue().getDateOfPlacingOrder()));

        setupEditableColumns();

        // -----------------------------------------------------------
        // --- כאן מתחיל הקוד החדש שהוספנו לסינון ---
        // -----------------------------------------------------------

        // 1. עוטפים את הנתונים המקוריים (orderData) ברשימה מסוננת
        filteredData = new FilteredList<>(orderData, p -> true);

        // 2. מגדירים לטבלה להציג את הרשימה המסוננת (במקום orderData ישירות)
        orderTable.setItems(filteredData);

        // 3. בודקים אם ה-DatePicker קיים (כדי למנוע קריסה אם לא הוספת אותו ב-FXML)
        if (filterDatePicker != null) {
            // הוספת מאזין: כל פעם שמשנים תאריך, הקוד הזה ירוץ
        	filterDatePicker.valueProperty().addListener((observable, oldValue, selectedDate) -> {
        	    filteredData.setPredicate(order -> {
        	        // 1. אם לא נבחר תאריך -> תציג הכל (או שתחליט להסתיר, תלוי בך)
        	        if (selectedDate == null) {
        	            return true; 
        	        }

        	        // 2. אם להזמנה אין תאריך -> הסתר אותה
        	        if (order.getOrderDate() == null) {
        	            return false;
        	        }

        	        // 3. המרת תאריך ההזמנה ל-LocalDate
        	        LocalDate orderDate;
        	        if (order.getOrderDate() instanceof java.sql.Date) {
        	            orderDate = ((java.sql.Date) order.getOrderDate()).toLocalDate();
        	        } else {
        	            orderDate = order.getOrderDate().toInstant()
        	                          .atZone(ZoneId.systemDefault())
        	                          .toLocalDate();
        	        }

        	        // 4. --- הגדרת הטווח (מה שבקשת) ---
        	        
        	        LocalDate today = LocalDate.now(); // התאריך של היום

        	        // תנאי א': התאריך חייב להיות "גדול או שווה" לתאריך שבחרת
        	        boolean isAfterSelection = !orderDate.isBefore(selectedDate);

        	        // תנאי ב': התאריך חייב להיות "קטן או שווה" להיום
        	        boolean isBeforeToday = !orderDate.isAfter(today);

        	        // החזר אמת רק אם שני התנאים מתקיימים
        	        return isAfterSelection && isBeforeToday;
        	    });
        	});
        }
    }
    @FXML
    private void handleClearFilter(ActionEvent event) {
        filterDatePicker.setValue(null); // זה יפעיל את ה-Listener ויחזיר את כל הרשימה
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
				case SEND_EMAIL:
					System.out.println((String)res.getData());
					break;
				case CREATE, UPDATE, DELETE:
					Order order = (Order)res.getData();
					Alarm.showAlert("Operation is done!", "We have sent details to your email: " + order.getClientEmail() + " ,Check you spam.", Alert.AlertType.INFORMATION);
					break;

				case GET_BY_ID:
					// Handle single order retrieval
					if (data instanceof Order) {
						System.out.println("Received order: " + data);
					}
					break;

				
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
		// MainNavigator.loadScene("managerTeam/EmployeeOption");
		ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
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