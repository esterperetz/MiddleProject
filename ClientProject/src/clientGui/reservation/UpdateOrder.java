package clientGui.reservation;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import entities.Alarm;
import entities.Order;
import entities.Order.OrderStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * * Controller for the "Update Order" window. Shows the selected order data and
 * sends the updated order to the server.
 */
public class UpdateOrder extends MainNavigator implements Initializable {

	@FXML
	private TextField orderIdField; // במקום txtId
	@FXML
	private TextField subscriberIdField;
	@FXML
	private TextField clientNameField; // במקום txtName
	@FXML
	private TextField phoneField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField guestsField; // במקום txtName
	@FXML
	private DatePicker datePicker; // במקום txtName1
	@FXML
	private TextField timeField; // HH:mm
	@FXML
	private TextField arrivalTimeField; // HH:mm
	@FXML
	private TextField priceField;
	@FXML
	private ComboBox<OrderStatus> statusComboBox;
	private Order o;
	private OrderUi_controller mainController; // Field to hold the main controller reference
	private OrderLogic ol;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		statusComboBox.getItems().setAll(OrderStatus.values());
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
		orderIdField.setText(String.valueOf(o.getOrderNumber()));

		if (o.getSubscriberId() != null && o.getSubscriberId() != 0) {
			// אם זה מנוי: מציגים ID ונועלים את שדות הלקוח
			subscriberIdField.setText(String.valueOf(o.getSubscriberId()));
			setClientFieldsEditable(false); // נעילה
		} else {
			// אם זה לקוח מזדמן: משאירים ריק ומאפשרים עריכה
			subscriberIdField.setText("");
			setClientFieldsEditable(true); // פתיחה
		}

		// 2. מילוי פרטי לקוח (Strings)
		clientNameField.setText(o.getClientName());
		phoneField.setText(o.getClientPhone());
		emailField.setText(o.getClientEmail());

		// 3. מילוי מספרים
		guestsField.setText(String.valueOf(o.getNumberOfGuests()));
		priceField.setText(String.valueOf(o.getTotalPrice()));

		// 4. מילוי סטטוס (ComboBox)
		if (o.getOrderStatus() != null) {
			statusComboBox.setValue(o.getOrderStatus());
		}

		// 5. טיפול מיוחד בתאריך ושעה (Order Date)
		// אנחנו מפרקים את ה-Date של ג'אווה ל-LocalDate (לתאריכון) ו-LocalTime (לשדה
		// השעה)
		if (o.getOrderDate() != null) {
			// המרה מ-Date ל-LocalDateTime
			java.time.LocalDateTime ldt = o.getOrderDate().toInstant().atZone(java.time.ZoneId.systemDefault())
					.toLocalDateTime();

			datePicker.setValue(ldt.toLocalDate()); // הצגת התאריך

			// הצגת השעה בפורמט HH:mm (למשל 14:30)
			timeField.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
		}

		// 6. טיפול בשעת הגעה (Arrival Time)
		if (o.getArrivalTime() != null) {
			java.time.LocalDateTime arrivalLdt = order.getArrivalTime().toInstant()
					.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

			arrivalTimeField.setText(String.format("%02d:%02d", arrivalLdt.getHour(), arrivalLdt.getMinute()));
		}
	}

	private void setClientFieldsEditable(boolean isEditable) {
		// הגדרת מצב עריכה
		clientNameField.setEditable(isEditable);
		phoneField.setEditable(isEditable);
		emailField.setEditable(isEditable);

		// שינוי עיצוב ויזואלי (אפור אם נעול, לבן אם פתוח)
		String style = isEditable ? "-fx-background-color: white; -fx-background-radius: 5;"
				: "-fx-background-color: #e0e0e0; -fx-background-radius: 5;";

		clientNameField.setStyle(style);
		phoneField.setStyle(style);
		emailField.setStyle(style);
	}

//asdassadasd
	public void loadStudent(Order o1) {
		this.o = o1;
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

		if (controller != null) {
			controller.initData();
		} else {
			Alarm.showAlert("Error Loading", "Could not load OrderUi_controllerr", AlertType.ERROR);
			// System.err.println("Error: Could not load OrderUi_controllerr.");
		}
	}

	@FXML
	private void handleUpdate(ActionEvent event) {
		try {
			if (clientNameField.getText().isEmpty() || guestsField.getText().isEmpty()
					|| datePicker.getValue() == null) {
				Alarm.showAlert("Error", "Name, Guests and Date are required.", Alert.AlertType.WARNING);
				return;
			}

			// 2. איסוף נתונים מהשדות שלך
			String name = clientNameField.getText();
			String phone = phoneField.getText();
			String email = emailField.getText();
			int guests = Integer.parseInt(guestsField.getText());
			double price = Double.parseDouble(priceField.getText());
			OrderStatus status = statusComboBox.getValue();

			// 3. הרכבת תאריך ההזמנה (LocalDate + String -> Date)
			if (timeField.getText().isEmpty()) {
				throw new IllegalArgumentException("Time is missing");
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
			if (ol == null) {
				String header = "Input Error";
				String context = "Order ID is missing.";
				Alarm.showAlert(header, context, Alert.AlertType.ERROR);
			} else {

				// 4. יצירת האובייקט המעודכן
				// שים לב: אנחנו לוקחים את הנתונים החדשים מהמשתנים שיצרנו למעלה,
				// ואת הנתונים הקבועים (כמו ID וקוד אישור) מהאובייקט המקורי (selectedOrder/o)
				Order updatedOrder = new Order(o.getOrderNumber(), // ID מקורי
						newOrderDate, // תאריך חדש
						guests, // אורחים חדש
						o.getConfirmationCode(), // קוד מקורי
						o.getSubscriberId(), // מנוי מקורי
						o.getDateOfPlacingOrder(), // תאריך יצירה מקורי
						name, // שם חדש
						email, // אימייל חדש
						phone, // טלפון חדש
						newArrivalTime, // הגעה חדש
						null,
						price, // מחיר חדש
						status // סטטוס חדש
				);

				// 5. שליחה ועדכון
				// if (ol != null) {
				ol.updateOrder(updatedOrder);
				OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

				if (controller != null) {
					controller.initData();
				} else {
					Alarm.showAlert("Error Loading", "Could not load OrderUi_controllerr", AlertType.ERROR);
				}
				// רענון הטבלה במסך הראשי (אם העברנו אותו ב-initData)
				if (mainController != null) {
					mainController.refreshTableData(); // הנחה שיש פונקציה כזו שקוראת ל-GET_ALL
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
}