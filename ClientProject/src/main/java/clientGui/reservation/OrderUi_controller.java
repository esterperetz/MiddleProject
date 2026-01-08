package clientGui.reservation;

import java.time.LocalDate;
import java.time.ZoneId;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    
    private FilteredList<Order> filteredData;
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

    private ObservableList<Order> orderData = FXCollections.observableArrayList();
    private OrderLogic orderLogic;
    private String ip;
    private Employee.Role isManager;
	private Employee emp;
//    private String employeeName;
    public OrderUi_controller() {
    }
 
    @FXML
    private void initialize() {

        Order_numberColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getOrderNumber()));

        clientNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty((cellData.getValue()).getCustomer().getName()));

        clientPhoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty((cellData.getValue()).getCustomer().getPhoneNumber()));

        clientEmailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty((cellData.getValue()).getCustomer().getEmail()));

        customer_idColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getCustomer().getCustomerId()));

        DateColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getOrderDate()));

        arrivalTimeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getArrivalTime()));

        itemColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getNumberOfGuests()));

        totalPriceColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getTotalPrice()));

        statusColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getOrderStatus()));

        confirmation_codeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getConfirmationCode()));

        date_of_placing_orderColumn.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>((cellData.getValue()).getDateOfPlacingOrder()));

        setupEditableColumns();

      
        filteredData = new FilteredList<>(orderData, p -> true);

        orderTable.setItems(filteredData);

        if (filterDatePicker != null) {
        	filterDatePicker.valueProperty().addListener((observable, oldValue, selectedDate) -> {
        	    filteredData.setPredicate(order -> {
        	        if (selectedDate == null) {
        	            return true; 
        	        }

        	        if (((Order)order).getOrderDate() == null) {
        	            return false;
        	        }

        	        LocalDate orderDate;
        	        if (((Order)order).getOrderDate() instanceof java.sql.Date) {
        	            orderDate = ((java.sql.Date) ((Order)order).getOrderDate()).toLocalDate();
        	        } else {
        	            orderDate = ((Order)order).getOrderDate().toInstant()
        	                          .atZone(ZoneId.systemDefault())
        	                          .toLocalDate();
        	        }

        	        
        	        LocalDate today = LocalDate.now(); 

        	        boolean isAfterSelection = !orderDate.isBefore(selectedDate);

        	        boolean isBeforeToday = !orderDate.isAfter(today);

        	        return isAfterSelection && isBeforeToday;
        	    });
        	});
        }
    }
    @FXML
    private void handleClearFilter(ActionEvent event) {
        filterDatePicker.setValue(null); 
    }
	/**
	 * Initializes this controller with an existing ClientUi and server IP.
	 * Registers this controller as a listener and loads all orders from the server.
	 *
	 * @param clientUi The client UI used for server communication.
	 * @param ip       The server IP address.
	 */
	
   
	public void initData(Employee emp, ClientUi clientUi, Role isManager) {
		this.emp = emp;
		this.clientUi = clientUi;
		this.isManager=isManager;
//		this.employeeName = employeeName;
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
					if (data instanceof List) {
				        List<?> list = (List<?>) data;
				        
				        // ניקוי הטבלה
				        orderData.clear();

				        // בדיקה שהרשימה לא ריקה
				        if (list.isEmpty()) {
				            orderTable.refresh();
				            return;
				        }

				        // לולאה שעוברת על ה-Map וממירה ל-Order
				        for (Object obj : list) {
				            if (obj instanceof Map) {
				                Map<String, Object> row = (Map<String, Object>) obj;
				                Order o = new Order();

				                // 1. שליפת נתוני ההזמנה והכנסה ל-Order
				                o.setOrderNumber((Integer) row.get("order_number"));
				                o.getCustomer().setCustomerId((Integer) row.get("customer_id"));
				                o.setNumberOfGuests((Integer) row.get("number_of_guests"));
				                o.setTotalPrice(((Number) row.get("total_price")).doubleValue()); // המרה בטוחה
				                o.setConfirmationCode((Integer) row.get("confirmation_code"));
				                
				                // טיפול ב-Enum סטטוס
				                String statusStr = (String) row.get("order_status");
				                if (statusStr != null) o.setOrderStatus(Order.OrderStatus.valueOf(statusStr));

				                // טיפול בתאריכים (SQL Timestamp -> Java Date)
				                if (row.get("order_date") != null)
				                    o.setOrderDate(new java.util.Date(((java.sql.Timestamp) row.get("order_date")).getTime()));
				                
				                if (row.get("arrival_time") != null)
				                    o.setArrivalTime(new java.util.Date(((java.sql.Timestamp) row.get("arrival_time")).getTime()));

				                if (row.get("date_of_placing_order") != null)
				                    o.setDateOfPlacingOrder(new java.util.Date(((java.sql.Timestamp) row.get("date_of_placing_order")).getTime()));

				                o.getCustomer().setName((String) row.get("customer_name"));
				                o.getCustomer().setEmail((String) row.get("email"));
				                o.getCustomer().setPhoneNumber((String) row.get("phone_number"));
//				                o.getCustomer().setSubscriberCode((Integer) row.get("subscriber_code"));
				                Object subCode = row.get("subscriber_code"); // שולפים כאובייקט כללי
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
					break;
				case SEND_EMAIL:
					System.out.println((String)res.getData());
					break;
				case CREATE, UPDATE, DELETE:
					Order order = (Order)res.getData();
				    //need to be fix maybe we will create method that req the customer id and get the result
//					Alarm.showAlert("Operation is done!", "We have sent details to your email: " + order.getClientEmail() + " ,Check you spam.", Alert.AlertType.INFORMATION);
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
							Alarm.showAlert("Success", "Operation completed successfully!", Alert.AlertType.INFORMATION);
		                    // Refresh table after success
		                    orderLogic.getAllOrders(); 
		                } else {
		                    Alarm.showAlert("Failure", "Operation failed.", Alert.AlertType.ERROR);
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
	            controller.initData(emp,emp.getRole());
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
	            controller.initData(selectedOrder,orderLogic,this,this.isManager,emp);
	        } else {
	            System.err.println("Error: Could not load ManagerOptionsController.");
	        }
	 
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
			Order o =  event.getRowValue();
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
//			controller.AnotherinitData(employeeName);
			controller.initData(emp,clientUi, this.isManager);
			
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