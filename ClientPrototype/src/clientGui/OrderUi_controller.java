package clientGui;

import client.ChatClient;

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

//		try {
//			c = new ClientUi();
//			c.addListener(this);
//
//			c.sendMessage("111");
//			c.sendMessage("2");
////			data = c.getMessage();
//			if (data != null) {
//				Platform.runLater(() -> {
//					System.out.println("data is: " + data);
//					orderData.add(parseOrder(data));
//					orderTable.getSelectionModel().select(parseOrder(data));
//					orderTable.scrollTo(parseOrder(data));
//				});
//			} else
//				System.out.println("data is null");
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// --- שינוי קריטי כאן ---
	    try {
	        // 1. אתחול הלקוח והוספת Listener
	        c = new ClientUi();
	        c.addListener(this);

	        // 2. שליחת פקודה מוגדרת לבקשת רשימת הזמנות
	        // בהנחה שהשרת מצפה לפרוטוקול כלשהו, נשתמש במחרוזת "GET_ALL_ORDERS"
	        // אם השרת שלך מצפה לאובייקט Message עטוף, השתמש בפורמט שהגדרת.
	        System.out.println("Sending request: GET_ALL_ORDERS");
	        c.sendMessage("GET_ALL_ORDERS"); 
	        
	        // הערה: אין צורך ב-c.getMessage() כיוון שהתשובה תגיע אסינכרונית דרך onMessageReceive
	        
	    } catch (IOException e) {
	        // טיפול בשגיאת חיבור
	        showAlert("Connection Error", "Could not connect to server or send initial request.", Alert.AlertType.ERROR);
	        e.printStackTrace();
	    }

		// Set the data into the table
		orderTable.setItems(orderData);
		showAlert("Load all reservations", "Generate all orders?",  Alert.AlertType.CONFIRMATION);

		setupEditableColumns();

	}

	// Method called by ChatClient to display messages from server
	public void displayMessage() {
		int newId = orderData.isEmpty() ? 1 : orderData.get(orderData.size() - 1).getOrder_number() + 1;
// 		Order newOrder = new Order(newId, new Date(), 0, 0, 1, new Date());

	}


	
	// שינוי שם המתודה לפונקציית עזר (כדי למנוע בלבול)
	public Order parseOrderFromKeyValue(String contentString) { 
	    // ...
	    String[] parts = contentString.split(", ");

	    int orderNumber = 0;
	    Date orderDate = null;
	    int numberOfGuests = 0;
	    int confirmationCode = 0;
	    int subscriberId = 0;
	    Date placingDate = null;

	    // פורמט תאריך נדרש: 2025-11-30 (כיוון שכך נראה הפורמט מהשרת)
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

	    for (String part : parts) {
	        String[] keyValue = part.split("=");
	        if (keyValue.length != 2) continue; // דילוג על שדות שבורים

	        String key = keyValue[0].trim();
	        String value = keyValue[1].trim();

	        switch (key) {
	            case "order_number":
	                orderNumber = Integer.parseInt(value);
	                break;
	            case "order_date":
	                try {
	                    orderDate = sdf.parse(value);
	                } catch (ParseException e) { /* Handle error */ }
	                break;
	            case "number_of_guests":
	                numberOfGuests = Integer.parseInt(value);
	                break;
	            case "confirmation_code":
	                confirmationCode = Integer.parseInt(value);
	                break;
	            case "subscriber_id":
	                subscriberId = Integer.parseInt(value);
	                break;
	            case "date_of_placing_order":
	                try {
	                    placingDate = sdf.parse(value);
	                } catch (ParseException e) { /* Handle error */ }
	                break;
	        }
	    }
	    
	    // ודא שהקונסטרקטור של Order מקבל את השדות בסדר הנכון!
	    // יצרתי קונסטרקטור דמה לפי השדות החדשים, החלף אותו בפועל
	    return new Order(orderNumber, orderDate, numberOfGuests, confirmationCode, subscriberId, placingDate); 
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
		if (res == ButtonType.CANCEL){
			c.sendMessage("quit");
		
		}
		else {
			Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
			alert2.setTitle("Loading all orders...");
			alert2.setHeaderText(null);
			alert2.setContentText("Successfully loaded all orders!");
			alert2.showAndWait();
		}
	}
 
	@Override
	public void onMessageReceive(String msg) {
//		System.out.println("----------- GOT CALLED");
//		if (msg != null) {
//			Platform.runLater(() -> {
//				System.out.println("msg is: " + msg + "---------------------------");
//				Order order = null;
//				try {
//					order = Order.parseOrder(msg);
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				orderData.add(order);
//				orderTable.getSelectionModel().select(order);
//				orderTable.scrollTo(order);
//			});
//		} else {
//			System.out.println("data is null");
//
//		}
//
//		// Set the data into the table
//		orderTable.setItems(orderData);
//
//		setupEditableColumns();
		
		System.out.println("----------- Received message from server: " + msg);
	    
		if(msg.equals("Disconnecting the client from the server.")) {
	    	c.DisconnectClient();
	    }
		
	    if (msg == null || msg.trim().isEmpty() || !msg.startsWith("[")) {
	        System.out.println("Message is not a valid list format.");
	        return;
	    }
	   
	    
	    // 1. הסרת הסוגריים המרובעים החיצוניים (התווים הראשון והאחרון)
	    String content = msg.substring(1, msg.length() - 1);
	    
	    // 2. פיצול לפי המחרוזת המייצגת הפרדה בין ההזמנות, שהיא ", " (פסיק ורווח)
	    //    אך רק אם היא לא נמצאת בתוך ה-Order עצמו!
	    
	    // הדרך הבטוחה ביותר היא לפצל לפי המחרוזת "], " ואז להוסיף בחזרה את הסוגר המרובע החסר לכל חלק.
	    
	    String[] orderBlocks = content.split("], Order \\[");
	    
	    Platform.runLater(() -> {
	        orderData.clear();
	        
	        for (int i = 0; i < orderBlocks.length; i++) {
	            String orderString = orderBlocks[i];
	            
	            // 3. תיקון מבנה המחרוזת:
	            //    הבלוק הראשון מתחיל יפה: "Order [order_number=2, ..."
	            //    כל הבלוקים שאחריו חסר להם התחילית "Order [" והסופית "]"
	            
	            if (i > 0) {
	                // מוסיף את תחילת מבנה האובייקט החסרה
	                orderString = "Order [" + orderString;
	            }
	            
	            if (i < orderBlocks.length - 1) {
	                // אם זה לא הבלוק האחרון, חסר סוגר מרובע בסוף
	                orderString = orderString + "]";
	            }
	            
	            // 4. ניתוח המחרוזת באמצעות פונקציה חיצונית
	            try {
	                // נניח ש-Order.parseOrder יודעת לנתח מחרוזת כזו: "Order [order_number=X, ...]"
	                // אם פונקציית parseOrder שלך מצפה רק לתוכן שבתוך הסוגריים המרובעים, 
	                // יהיה עליך לשלוח רק את התוכן הפנימי.
	                
	                // הניתוח המדויק תלוי במימוש הפנימי של Order.parseOrder.
	                // אם הניתוח הפנימי מצפה ל-String שנראה כך: "order_number=X, order_date=Y, ..."
	                // נשתמש בקוד הבא:
	                
	                // חילוץ התוכן הפנימי: מחפש את התוכן בין '[' ל-'']'
	                int start = orderString.indexOf("[");
	                int end = orderString.lastIndexOf("]");
	                if (start != -1 && end != -1 && end > start) {
	                    String cleanContent = orderString.substring(start + 1, end);
	                    Order order = parseOrderFromKeyValue(cleanContent); // נשתמש בפונקציית העזר שלך
	                    if (order != null) {
	                        orderData.add(order);
	                    }
	                }
	                
	            } catch (Exception e) {
	                System.err.println("Error parsing Order object: " + orderString + " Error: " + e.getMessage());
	                e.printStackTrace();
	            }
	        }
	        
	        // עדכון חזותי לאחר הטעינה
	        if (!orderData.isEmpty()) {
	            orderTable.getSelectionModel().selectFirst();
	            orderTable.scrollTo(0);
	        }
	        System.out.println("Successfully loaded " + orderData.size() + " orders.");
	    });
	}
}
