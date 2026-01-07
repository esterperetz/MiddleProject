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
import entities.Customer;
import entities.Employee;
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
	private Employee.Role isManager;
	private String employeeName;
	@FXML
	private ComboBox<OrderStatus> statusComboBox;
	private Order o;
	private OrderUi_controller mainController; // Field to hold the main controller reference
	private OrderLogic ol;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Employee emp;

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
	public void initData(Order order, OrderLogic orderLogic, OrderUi_controller mainController,
			Employee.Role isManager,Employee emp) { // FIXED SIGNATURE
		this.emp = emp;
		this.isManager = isManager;
//		this.employeeName = employeeName;
		this.o = order;
		this.ol = orderLogic;
		this.mainController = mainController; // Store main controller reference
		orderIdField.setText(String.valueOf(o.getOrderNumber()));

		if (o.getCustomer().getCustomerId() != null && o.getCustomer().getSubscriberCode() != null && o.getCustomer().getSubscriberCode() != 0) {
			// אם זה מנוי: מציגים ID ונועלים את שדות הלקוח
			subscriberIdField.setText(String.valueOf(o.getCustomer().getSubscriberCode()));
			setClientFieldsEditable(false); // נעילה
		}
//			else if (o.getCustomerId() != null && o.getCustomerId() != 0) {
//			subscriberIdField.setText(String.valueOf(o.getCustomerId()));
//			setClientFieldsEditable(false); //
//		} 
		else {
			// אם זה לקוח מזדמן: משאירים ריק ומאפשרים עריכה
			subscriberIdField.setText("");
			setClientFieldsEditable(true); // פתיחה
		}

		// 2. מילוי פרטי לקוח (Strings)
		clientNameField.setText(o.getCustomer().getName());
		phoneField.setText(o.getCustomer().getPhoneNumber());
		emailField.setText(o.getCustomer().getEmail());

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

	public void loadStudent(Order o1) {
		this.o = o1;
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

		if (controller != null) {
			controller.initData(emp,clientUi,this.isManager);
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

			if (timeField.getText().isEmpty()) {
				throw new IllegalArgumentException("Time is missing");
			}

			LocalDate localDate = datePicker.getValue();
			LocalTime localTime = LocalTime.parse(timeField.getText()); // מצפה ל-HH:mm
			Date newOrderDate = Date.from(localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());

			Date newArrivalTime = o.getArrivalTime(); // ברירת מחדל: הישן
			if (!arrivalTimeField.getText().isEmpty()) {
				LocalTime arrivalT = LocalTime.parse(arrivalTimeField.getText());
				newArrivalTime = Date.from(localDate.atTime(arrivalT).atZone(ZoneId.systemDefault()).toInstant());
			}

			if (ol == null) {
				String header = "Input Error";
				String context = "Order ID is missing.";
				Alarm.showAlert(header, context, Alert.AlertType.ERROR);
			} else {
				Customer updatedCustomer = o.getCustomer();
		        if (updatedCustomer == null) {
		            updatedCustomer = new Customer(); 
		        }
		        updatedCustomer.setName(name);       
		        updatedCustomer.setPhoneNumber(phone); 
		        updatedCustomer.setEmail(email);
				Order updatedOrder = new Order(o.getOrderNumber(),
						newOrderDate, 
						guests,
						o.getConfirmationCode(),
						updatedCustomer,
						null, o.getDateOfPlacingOrder(),
						newArrivalTime, 
						null, price, 
						status 
				);

				ol.updateOrder(updatedOrder);
				OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

				if (controller != null) {
					controller.initData(emp,clientUi,this.isManager);
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