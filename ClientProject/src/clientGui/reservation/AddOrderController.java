package clientGui.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import Entities.Alarm;
import Entities.Order;
import Entities.Order.OrderStatus;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddOrderController extends MainNavigator {

	@FXML
	private TextField clientNameField; // במקום fullNameField
	@FXML
	private TextField phoneField; // במקום idDetailsField
	@FXML
	private TextField emailField; // חדש!
	@FXML
	private TextField subscriberIdField;
	@FXML
	private TextField guestsField;
	@FXML
	private DatePicker datePicker;
	@FXML
	private TextField timeField;
	@FXML
	private TextField arrivalTimeField; // חדש!
	@FXML
	private TextField priceField;
	@FXML
	private ComboBox<OrderStatus> statusComboBox;
	private OrderLogic orderLogic;

	@FXML
	public void initialize() {
		statusComboBox.getItems().setAll(OrderStatus.values());
		statusComboBox.setValue(OrderStatus.APPROVED); // Default
		datePicker.setValue(LocalDate.now());
		// הגדרת שעה ברירת מחדל
		timeField.setText("12:00");

	}

	@FXML
	private void handleSave(ActionEvent event) {
		// 1. ולידציה בסיסית - חובה שהשדות לא יהיו ריקים
		if (clientNameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()
				|| guestsField.getText().trim().isEmpty() || datePicker.getValue() == null) {

			Alarm.showAlert("Missing Input", "Please fill in mandatory fields (Name, Phone, Guests, Date).",
					Alert.AlertType.WARNING);
			return;
		}

		try {
			// 2. איסוף נתונים מהשדות
			String clientName = clientNameField.getText().trim();
			String clientPhone = phoneField.getText().trim();
			String clientEmail = emailField.getText().trim(); // יכול להיות ריק

			// המרת מנוי (אם יש)
			Integer subId = null;
			if (!subscriberIdField.getText().trim().isEmpty()) {
				subId = Integer.parseInt(subscriberIdField.getText().trim());
			}

			// המרת מספרים
			int guests = Integer.parseInt(guestsField.getText().trim());
			double price = priceField.getText().trim().isEmpty() ? 0.0
					: Double.parseDouble(priceField.getText().trim());

			// 3. טיפול בתאריך ושעה (Order Date)
			LocalDate localDate = datePicker.getValue();
			String timeString = timeField.getText().trim();

			if (!timeString.matches("\\d{2}:\\d{2}")) {
				Alarm.showAlert("Time Error", "Enter time in HH:mm format (e.g., 18:30)", Alert.AlertType.ERROR);
				return;
			}

			LocalTime localTime = LocalTime.parse(timeString);
			Date orderDate = Date.from(localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());

			// 4. טיפול בשעת הגעה (Arrival Time) - אופציונלי
			Date arrivalDate = null;
			String arrivalString = arrivalTimeField.getText().trim();
			if (!arrivalString.isEmpty()) {
				if (!arrivalString.matches("\\d{2}:\\d{2}")) {
					Alarm.showAlert("Time Error", "Arrival time must be HH:mm format.", Alert.AlertType.ERROR);
					return;
				}
				LocalTime arrivalTime = LocalTime.parse(arrivalString);
				// משתמשים באותו תאריך שנבחר ב-DatePicker עבור שעת ההגעה
				arrivalDate = Date.from(localDate.atTime(arrivalTime).atZone(ZoneId.systemDefault()).toInstant());
			}

			OrderStatus status = statusComboBox.getValue();

			// 5. יצירת האובייקט עם הבנאי החדש והמלא
			Order newOrder = new Order(0, // order_number (אוטומטי ב-DB)
					orderDate, // order_date (תאריך ושעה)
					guests, // number_of_guests
					0, // confirmation_code (נוצר בשרת)
					subId, // subscriber_id
					new Date(), // date_of_placing_order (עכשיו)
					clientName, // client_name (השדה החדש)
					clientEmail, // client_email (השדה החדש)
					clientPhone, // client_Phone (השדה החדש)
					arrivalDate, // ArrivalTime (השדה החדש)
					price, // total_price
					status // order_status
			);

			// 6. שליחה לשרת ומעבר מסך
			if (orderLogic != null) {
				orderLogic.createOrder(newOrder);

				// מעבר למסך הבא
				OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);

				if (controller != null) {
					controller.initData();
				} else {
					System.err.println("Error: Could not load OrderUi_controller.");
				}
			} else {
				System.err.println("Error: OrderLogic is not initialized. Did you call initData?");
			}

		} catch (NumberFormatException e) {
			Alarm.showAlert("Input Error", "Guests and Price must be valid numbers.", Alert.AlertType.ERROR);
		} catch (Exception e) {
			Alarm.showAlert("Error", "An error occurred while saving.", Alert.AlertType.ERROR);
			e.printStackTrace();
		}
	}

	// public void initData(ClientUi clientUi) {
	public void initData() {
		// this.clientUi = clientUi;
		this.orderLogic = new OrderLogic(this.clientUi);
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
		if (controller != null) {
			controller.initData();
		} else {
			System.err.println("Error: Could not load OrderUi_controllerr.");
		}
	}

	// private void closeWindow() {
	// Stage stage = (Stage) fullNameField.getScene().getWindow();
	// stage.close();
	// }
}