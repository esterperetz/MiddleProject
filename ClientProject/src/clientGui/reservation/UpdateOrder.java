package clientGui.reservation;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import Entities.Alarm;
import Entities.Order;
import Entities.Order.OrderStatus;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * * Controller for the "Update Order" window. Shows the selected order data and
 * sends the updated order to the server.
 */
public class UpdateOrder extends MainNavigator implements Initializable {

	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtName1;
	@FXML
	private TextField orderIdField;
	@FXML
	private TextField subscriberIdField;
	@FXML
	private TextField clientNameField;
	@FXML
	private TextField phoneField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField guestsField;
	@FXML
	private DatePicker datePicker;
	@FXML
	private TextField timeField; // HH:mm for Order Date
	@FXML
	private TextField arrivalTimeField; // HH:mm for Arrival
	@FXML
	private TextField priceField;
	@FXML
	private ComboBox<Order.OrderStatus> statusComboBox;
	private Order o;
	private OrderUi_controller mainController; // Field to hold the main controller reference
	private OrderLogic ol;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Initialization is done in initData
	}

	/**
	 * Initializes data, OrderLogic, and the main controller reference.
	 * 
	 * @param order          The Order object to be updated.
	 * @param orderLogic     The logic object for server communication.
	 * @param mainController The reference to the main UI controller for data
	 *                       refresh.
	 */
	public void initData(Order order, OrderLogic orderLogic, OrderUi_controller mainController) { // FIXED SIGNATURE
		this.o = order;
		this.ol = orderLogic;
		this.mainController = mainController; // Store main controller reference
		orderIdField.setText(String.valueOf(order.getOrder_number()));

		orderIdField.setText(String.valueOf(order.getOrder_number()));

		if (order.getSubscriber_id() != null && order.getSubscriber_id() != 0) {
			subscriberIdField.setText(String.valueOf(order.getSubscriber_id()));
		} else {
			subscriberIdField.setText("Guest"); // או להשאיר ריק
		}

		// 2. מילוי פרטי לקוח (Strings)
		clientNameField.setText(order.getClient_name());
		phoneField.setText(order.getClient_Phone());
		emailField.setText(order.getClient_email());

		// 3. מילוי מספרים
		guestsField.setText(String.valueOf(order.getNumber_of_guests()));
		priceField.setText(String.valueOf(order.getTotal_price()));

		// 4. מילוי סטטוס (ComboBox)
		if (order.getOrder_status() != null) {
			statusComboBox.setValue(order.getOrder_status());
		}

		// 5. טיפול מיוחד בתאריך ושעה (Order Date)
		// אנחנו מפרקים את ה-Date של ג'אווה ל-LocalDate (לתאריכון) ו-LocalTime (לשדה
		// השעה)
		if (order.getOrder_date() != null) {
			// המרה מ-Date ל-LocalDateTime
			java.time.LocalDateTime ldt = order.getOrder_date().toInstant().atZone(java.time.ZoneId.systemDefault())
					.toLocalDateTime();

			datePicker.setValue(ldt.toLocalDate()); // הצגת התאריך

			// הצגת השעה בפורמט HH:mm (למשל 14:30)
			timeField.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
		}

		// 6. טיפול בשעת הגעה (Arrival Time)
		if (order.getArrivalTime() != null) {
			java.time.LocalDateTime arrivalLdt = order.getArrivalTime().toInstant()
					.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

			arrivalTimeField.setText(String.format("%02d:%02d", arrivalLdt.getHour(), arrivalLdt.getMinute()));
		}
	}

	public void loadStudent(Order o1) {
		this.o = o1;
	}
	@FXML
	private void onUpdate(ActionEvent event) {
	    try {
	        // 1. איסוף נתונים מהשדות החדשים
	        String OrderNum = orderIdField.getText().trim(); // שדה נעול
	        
	        // נתונים מהטופס (במקום להשתמש בנתונים הישנים)
	        String newName = clientNameField.getText().trim();
	        String newPhone = phoneField.getText().trim();
	        String newEmail = emailField.getText().trim();
	        int guests = Integer.parseInt(guestsField.getText().trim());
	        double price = Double.parseDouble(priceField.getText().trim());
	        OrderStatus status = statusComboBox.getValue();

	        // 2. טיפול בתאריך (שילוב DatePicker עם שדה שעה)
	        if (datePicker.getValue() == null || timeField.getText().isEmpty()) {
	             throw new IllegalArgumentException("Date or Time is missing");
	        }
	        
	        LocalDate localDate = datePicker.getValue();
	        LocalTime localTime = LocalTime.parse(timeField.getText()); // מצפה ל-HH:mm
	        Date newOrderDate = Date.from(localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());

	        // טיפול בשעת הגעה (אם יש)
	        Date newArrivalTime = o.getArrivalTime(); // ברירת מחדל: הישן
	        if (!arrivalTimeField.getText().isEmpty()) {
	             LocalTime arrivalT = LocalTime.parse(arrivalTimeField.getText());
	             newArrivalTime = Date.from(localDate.atTime(arrivalT).atZone(ZoneId.systemDefault()).toInstant());
	        }

	        // 3. בדיקה שה-ID קיים (למרות שהוא נעול)
	        if (OrderNum.isEmpty()) {
	            String header = "Input Error";
	            String context = "Order ID is missing.";
	            Alarm.showAlert(header, context, Alert.AlertType.ERROR);
	        } else {
	            
	            // 4. יצירת האובייקט המעודכן
	            // שים לב: אנחנו לוקחים את הנתונים החדשים מהמשתנים שיצרנו למעלה,
	            // ואת הנתונים הקבועים (כמו ID וקוד אישור) מהאובייקט המקורי (selectedOrder/o)
	            Order updatedOrder = new Order(
	                    Integer.parseInt(OrderNum),           // מזהה (לא משתנה)
	                    newOrderDate,                         // תאריך מעודכן
	                    guests,                               // כמות אורחים מעודכנת
	                    o.getConfirmation_code(), // קוד אישור (לא משתנה)
	                    o.getSubscriber_id(),     // מזהה מנוי (לא משתנה)
	                    o.getDate_of_placing_order(), // תאריך יצירה (לא משתנה)
	                    newName,                              // שם מעודכן!
	                    newEmail,                             // אימייל מעודכן!
	                    newPhone,                             // טלפון מעודכן!
	                    newArrivalTime,                       // שעת הגעה מעודכנת!
	                    price,                                // מחיר מעודכן!
	                    status                                // סטטוס מעודכן!
	            );

	            // 5. שליחה ועדכון
	            if (ol != null) {
	            	ol.updateOrder(updatedOrder);
	                OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

					if (controller != null) {
						controller.initData();
					} else {
						System.err.println("Error: Could not load OrderUi_controllerr.");
					}
	                // רענון הטבלה במסך הראשי (אם העברנו אותו ב-initData)
	                if (mainController != null) {
	                    mainController.refreshTableData(); // הנחה שיש פונקציה כזו שקוראת ל-GET_ALL
	                }

	                // סגירת החלון הנוכחי (הכי נכון ל-Popup)
	                //closeWindow();
	                
	                // הודעת הצלחה (אופציונלי, כי לרוב ה-ClientUi יקפיץ הודעה מהשרת)
	                // Alarm.showAlert("Success", "Update request sent.", Alert.AlertType.INFORMATION);
	            } 
	        }

	    } catch (NumberFormatException e) {
	        String header = "Format Error";
	        String context = "Check that Guests and Price are valid numbers.";
	        Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
	    } catch (Exception e) { // תופס גם שגיאות תאריך (DateTimeParseException)
	        String header = "Error";
	        String context = "Check time format (HH:mm) or connection.";
	        Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
	        e.printStackTrace();
	    }
	}
/*
	@FXML
	private void onUpdate(ActionEvent event) {
		try { // 1. פתיחת TRY
			String OrderNum = txtId.getText().trim();
			String Number_Of_Guests = txtName.getText().trim();
			String OrderDate = txtName1.getText();
			int guests = Integer.parseInt(guestsField.getText());
            double price = Double.parseDouble(priceField.getText());
            OrderStatus status = statusComboBox.getValue();
			Date date = dateFormat.parse(OrderDate);

			if (OrderNum.isEmpty()) {
				String header = "Input Error";
				String context = "Please Enter Order ID (This field is now locked).";
				Alarm.showAlert(header, context, Alert.AlertType.ERROR);
			} else { // פתיחת ELSE
				// יצירת האובייקט
				Order updatedOrder = new Order(Integer.parseInt(OrderNum), // order_number (מהטופס)
						date, // order_date (מהטופס)
						Integer.parseInt(Number_Of_Guests), // number_of_guests (מהטופס)
						o.getConfirmation_code(), // שמירה על הקוד המקורי
						o.getSubscriber_id(), // שמירה על המנוי המקורי
						o.getDate_of_placing_order(), // שמירה על תאריך היצירה
						o.getClient_name(), // שמירה על השם (השדה החדש)
						o.getClient_email(), // שמירה על האימייל (השדה החדש)
						o.getClient_Phone(), // שמירה על הטלפון (השדה החדש)
						o.getArrivalTime(), // שמירה על שעת ההגעה (השדה החדש)
						o.getTotal_price(), // שמירה על המחיר
						o.getOrder_status() // שמירה על הסטטוס (השדה החדש)
				);

				if (ol != null) {
					ol.updateOrder(updatedOrder);
					OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

					if (controller != null) {
						controller.initData();
					} else {
						System.err.println("Error: Could not load OrderUi_controllerr.");
					}

					if (mainController != null) {
						mainController.refreshTableData();
					}

					// אופציונלי: סגירת החלון
					// ((Node) event.getSource()).getScene().getWindow().hide();
				} // סגירת IF (ol != null)
			} // סגירת ELSE

		} catch (NumberFormatException | ParseException e) { // <--- כאן היה חסר סוגר סוגר של ה-TRY לפני ה-CATCH
			String header = "Format Error";
			String context = "Please verify that Order ID and Guests are valid numbers, and Date is 'yyyy-MM-dd'.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			e.printStackTrace();
		} catch (Exception e) {
			String header = "Error";
			String context = "An unexpected error occurred during update.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			e.printStackTrace();
		}
	}
*/
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