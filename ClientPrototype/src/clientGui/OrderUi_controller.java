package clientGui;

import client.ChatClient;
import client.MessageListener;
import clientLogic.OrderLogic;

import java.awt.Dialog;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import Entities.Order;
import Entities.RequestPath;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DateStringConverter;

public class OrderUi_controller implements Initializable, MessageListener<String> {
	private ChatClient chatClient;

	@FXML
	private TableView<Order> orderTable;
	@FXML
	private TableColumn<Order, Integer> Order_numberColumn;
	@FXML
	private TableColumn<Order, Date> DateColumn;
	@FXML
	private TableColumn<Order, Integer> itemColumn;

	private ObservableList<Order> orderData = FXCollections.observableArrayList();
//	private ObservableSet<Order> orderData = FXCollections.observableSet();

	private String data;
	private ClientUi c;

	private ClientUi clientUi; // שכבת תקשורת בצד לקוח
	private OrderLogic orderLogic; // לוגיקה של הזמנות בצד לקוח

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// 1. הגדרת העמודות בטבלה
		Order_numberColumn.setCellValueFactory(new PropertyValueFactory<>("order_number"));
		itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
		DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));

		// 2. אתחול התקשורת עם השרת
		try {
			// א. ליצור ClientUi אחד
			clientUi = new ClientUi();

			// ב. להירשם כמאזין להודעות מהשרת
			clientUi.addListener(this);

			// ג. ליצור את OrderLogic שעובד מול ClientUi
			orderLogic = new OrderLogic(clientUi);

			System.out.println("Sending request: GET_ALL_ORDERS");
			// ד. לבקש את כל ההזמנות מהשרת
			orderLogic.getAllOrders();

		} catch (IOException e) {
			showAlert("Connection Error", "Could not connect to server or send initial request.",
					Alert.AlertType.ERROR);
			e.printStackTrace();
		}

		// 3. לחבר את ה־ObservableList לטבלה
		orderTable.setItems(orderData);

		// 4. להגדיר עמודות כ־editable וכו'
		setupEditableColumns();

		// (האזהרה / confirmation – אופציונלי, תלוי בדרישה)
		showAlert("Load all reservations", "Generate all orders?", Alert.AlertType.CONFIRMATION);
	}


	
//	@Override
//	public void initialize(URL location, ResourceBundle resources) {
//
//		Order_numberColumn.setCellValueFactory(new PropertyValueFactory<>("order_number"));
//		itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
//
//		// --- Date column formatting ---
//		DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));
//
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//		
//	    try {
//	        c = new ClientUi();
//	        c.addListener(this);
//
//	        System.out.println("Sending request: GET_ALL_ORDERS");
//	        OrderLogic c1 = new OrderLogic(c);
//	        c1.getAllOrders();
////	        c.sendMessage("GET_ALL_ORDERS"); 
//	        
//	        
//	    } catch (IOException e) {
//	        showAlert("Connection Error", "Could not connect to server or send initial request.", Alert.AlertType.ERROR);
//	        e.printStackTrace();
//	    }
//
//		// Set the data into the table
//		orderTable.setItems(orderData);
//		showAlert("Load all reservations", "Generate all orders?",  Alert.AlertType.CONFIRMATION);
//
//		setupEditableColumns();
//
//	}

	// Method called by ChatClient to display messages from server
	public void displayMessage() {
		int newId = orderData.isEmpty() ? 1 : orderData.get(orderData.size() - 1).getOrder_number() + 1;
// 		Order newOrder = new Order(newId, new Date(), 0, 0, 1, new Date());

	}

	private void setupEditableColumns() {
		// עריכת תאריך
		DateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
		DateColumn.setOnEditCommit(event -> {
			event.getRowValue().setDate_of_placing_order(event.getNewValue());
		});
//
//        // עריכת Guests
//        itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
//        itemColumn.setOnEditCommit(event -> {
//            event.getRowValue().setNumber_of_guests(event.getNewValue());
//        });
		itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

		itemColumn.setOnEditCommit(event -> {
			Integer newValue = event.getNewValue();
			if (newValue != null) {
				event.getRowValue().setNumber_of_guests(newValue);
			}
		});
	}

	@FXML
	private void handleAddOrder() {
		int newId = orderData.isEmpty() ? 1 : orderData.get(orderData.size() - 1).getOrder_number() + 1;

		Order newOrder = new Order(newId, new Date(), 0, 0, 1, new Date());
		orderData.add(newOrder);

		orderTable.getSelectionModel().select(newOrder);
		orderTable.scrollTo(newOrder);
	}

	@FXML
	private void handleDeleteOrder() {

		Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
		if (selectedOrder != null) {
			orderData.remove(selectedOrder);
		} else {
			showAlert("No Selection", "Please select an order to delete.", Alert.AlertType.WARNING);
		}

	}

	@FXML
	private void handleUpdateOrder(ActionEvent event) throws Exception {
		int rowIndex = orderTable.getSelectionModel().getSelectedIndex();
		if (rowIndex < 0) {
			showAlert("No Selection", "Please select an order to update.", Alert.AlertType.WARNING);
			return;
		}
		FXMLLoader loader = new FXMLLoader();
		((Node) event.getSource()).getScene().getWindow().hide(); // hiding primary window
		Stage primaryStage = new Stage();

		loader = new FXMLLoader(getClass().getResource("/clientGui/updateOrder.fxml"));

		Pane root = loader.load();
		Scene scene = new Scene(root);

		primaryStage.setTitle("Update Order");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private void showAlert(String title, String content, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
		ButtonType res = alert.getResult();
		if (res == ButtonType.CANCEL) {
			
			RequestPath rq = new RequestPath(null,"quit");
			clientUi.sendRequest(rq);
			System.out.println("helppppp");
//			Stage stage = (Stage) orderTable.getScene().getWindow(); 
//		    stage.close();

		} else {
			Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
			alert2.setTitle("Loading all orders...");
			alert2.setHeaderText(null);
			alert2.setContentText("Successfully loaded all orders!");
			alert2.showAndWait();
		}
	}

	@Override
	public void onMessageReceive(String msg) {

		System.out.println("----------- Received message from server: " + msg);

		if (msg.equals("Disconnecting the client from the server.")) {
			System.out.println("I am in");
			c.DisconnectClient();
			Stage stage = (Stage) orderTable.getScene().getWindow(); 
		    stage.close();
			
		}

		if (msg == null || msg.trim().isEmpty() || !msg.startsWith("[")) {
			System.out.println("Message is not a valid list format.");
			return;
		}

		String content = msg.substring(1, msg.length() - 1);

		String[] orderBlocks = content.split("], Order \\[");

		Platform.runLater(() -> {
			orderData.clear();

			for (int i = 0; i < orderBlocks.length; i++) {
				String orderString = orderBlocks[i];

				if (i > 0) {
					orderString = "Order [" + orderString;
				}

				if (i < orderBlocks.length - 1) {
					orderString = orderString + "]";
				}

				try {
					int start = orderString.indexOf("[");
					int end = orderString.lastIndexOf("]");
					if (start != -1 && end != -1 && end > start) {
						String cleanContent = orderString.substring(start + 1, end);
						Order order = Order.parseOrder(cleanContent); // נשתמש בפונקציית העזר שלך
						if (order != null) {
							orderData.add(order);
						}
					}

				} catch (Exception e) {
					System.err.println("Error parsing Order object: " + orderString + " Error: " + e.getMessage());
					e.printStackTrace();
				}
			}

			if (!orderData.isEmpty()) {
				orderTable.getSelectionModel().selectFirst();
				orderTable.scrollTo(0);

			}
			System.out.println("Successfully loaded " + orderData.size() + " orders.");
		});
	}
}

