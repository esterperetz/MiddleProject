package clientGui;

import client.ChatClient;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import Entities.Order;
import clientUi.ClientUi;
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Order_numberColumn.setCellValueFactory(new PropertyValueFactory<>("order_number"));
		itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));

		// --- Date column formatting ---
		DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// Display formatted date in the table (dd/MM/yyyy)
//	    DateColumn.setCellFactory(column -> new TableCell<Order, Date>() {
//	        @Override
//	        protected void updateItem(Date item, boolean empty) {
//	            super.updateItem(item, empty);
//            
//	            if (empty || item == null) {
//	                setText(null);
//	            } else {
//	                setText(item.format(formatter)); // <-- format to dd/MM/yyyy
//	            }
//	        }
//	    });
//		Order newOrder = new Order(1, new Date(), 0, 0, 1, new Date());
//		orderData.add(newOrder);

		try {
			c = new ClientUi();
			c.addListener(this);

			c.sendMessage("111");
//			data = c.getMessage();
			if (data != null) {
				Platform.runLater(() -> {
					System.out.println("data is: " + data);
					orderData.add(parseOrder(data));
					orderTable.getSelectionModel().select(parseOrder(data));
					orderTable.scrollTo(parseOrder(data));
				});
			} else
				System.out.println("data is null");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set the data into the table
		orderTable.setItems(orderData);

		setupEditableColumns();

	}

	// Method called by ChatClient to display messages from server
	public void displayMessage() {
		int newId = orderData.isEmpty() ? 1 : orderData.get(orderData.size() - 1).getOrder_number() + 1;
// 		Order newOrder = new Order(newId, new Date(), 0, 0, 1, new Date());

	}

	public Order parseOrder(String str) {
		// מסירים "Order{" בהתחלה ו- "}" בסוף

		//
//		System.out.println(str);
		str = str.substring(str.indexOf("[") + 1, str.lastIndexOf("]"));
		String[] parts = str.split(", ");

		int id = 0;
		Date date = null;
		int price = 0;
		int amount = 0;
		int status = 0;
		Date deliveryDate = null;

		// פורמט תאריך כמו שמגיע ב-String
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

		for (String part : parts) {
			String[] keyValue = part.split("=");
			String key = keyValue[0];
			String value = keyValue[1];

			switch (key) {
			case "":
				id = Integer.parseInt(value);
				break;

			case "date":
				try {
					date = sdf.parse(value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case "price":
				price = Integer.parseInt(value);
				break;

			case "amount":
				amount = Integer.parseInt(value);
				break;

			case "status":
				status = Integer.parseInt(value);
				break;

			case "deliveryDate":
				try {
					deliveryDate = sdf.parse(value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}

		return new Order(id, date, price, amount, status, deliveryDate);
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
	}

	@Override
	public void onMessageReceive(String msg) {
		System.out.println("----------- GOT CALLED");
		if (msg != null) {
			Platform.runLater(() -> {
				System.out.println("msg is: " + msg + "---------------------------");
				Order order = null;
				try {
					order = Order.parseOrder(msg);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				orderData.add(order);
				orderTable.getSelectionModel().select(order);
				orderTable.scrollTo(order);
			});
		} else {
			System.out.println("data is null");

		}

		// Set the data into the table
		orderTable.setItems(orderData);

		setupEditableColumns();
	}
}
