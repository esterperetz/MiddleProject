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
 * Controller for the "Update Order" window.
 * Shows the selected order data and sends the updated order to the server.
 */
public class UpdateOrder extends MainNavigator implements Initializable {

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
	 * @param isManager      The role of the employee
	 * @param emp            The employee object
	 */
	public void initData(Order order, OrderLogic orderLogic, OrderUi_controller mainController,
			Employee.Role isManager, Employee emp) {
		this.emp = emp;
		this.isManager = isManager;
		// this.employeeName = employeeName;
		this.o = order;
		this.ol = orderLogic;
		this.mainController = mainController; // Store main controller reference
		orderIdField.setText(String.valueOf(o.getOrderNumber()));

		if (o.getCustomer().getCustomerId() != null && o.getCustomer().getSubscriberCode() != null
				&& o.getCustomer().getSubscriberCode() != 0) {
			// If subscriber: display ID and lock client fields
			subscriberIdField.setText(String.valueOf(o.getCustomer().getSubscriberCode()));
			setClientFieldsEditable(false); // Lock
		} else {
			// If casual customer: leave empty and allow editing
			subscriberIdField.setText("");
			setClientFieldsEditable(true); // Unlock
		}

		// 2. Fill client details (Strings)
		clientNameField.setText(o.getCustomer().getName());
		phoneField.setText(o.getCustomer().getPhoneNumber());
		emailField.setText(o.getCustomer().getEmail());

		// 3. Fill numbers
		guestsField.setText(String.valueOf(o.getNumberOfGuests()));
		priceField.setText(String.valueOf(o.getTotalPrice()));

		// 4. Fill status (ComboBox)
		if (o.getOrderStatus() != null) {
			statusComboBox.setValue(o.getOrderStatus());
		}

		// 5. Special handling for Order Date
		// We split Java's Date into LocalDate (for DatePicker) and LocalTime (for Time
		// Field)
		if (o.getOrderDate() != null) {
			// Convert Date to LocalDateTime
			java.time.LocalDateTime ldt = o.getOrderDate().toInstant().atZone(java.time.ZoneId.systemDefault())
					.toLocalDateTime();

			datePicker.setValue(ldt.toLocalDate()); // Display Date

			// Display Time in HH:mm format (e.g., 14:30)
			timeField.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
		}

		// 6. Handling Arrival Time
		if (o.getArrivalTime() != null) {
			java.time.LocalDateTime arrivalLdt = order.getArrivalTime().toInstant()
					.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

			arrivalTimeField.setText(String.format("%02d:%02d", arrivalLdt.getHour(), arrivalLdt.getMinute()));
		}
	}

	/**
	 * Sets the editability of client fields.
	 * 
	 * @param isEditable true to enable editing, false to disable
	 */
	private void setClientFieldsEditable(boolean isEditable) {
		// Set editable state
		clientNameField.setEditable(isEditable);
		phoneField.setEditable(isEditable);
		emailField.setEditable(isEditable);

		// Change visual style (gray if locked, white if open)
		String style = isEditable ? "-fx-background-color: white; -fx-background-radius: 5;"
				: "-fx-background-color: #e0e0e0; -fx-background-radius: 5;";

		clientNameField.setStyle(style);
		phoneField.setStyle(style);
		emailField.setStyle(style);
	}

	public void loadStudent(Order o1) {
		this.o = o1;
	}

	/**
	 * Cancels the update operation and returns to the Order UI.
	 * 
	 * @param event The action event
	 */
	@FXML
	private void handleCancel(ActionEvent event) {
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

		if (controller != null) {
			controller.initData(emp, clientUi, this.isManager);
		} else {
			Alarm.showAlert("Error Loading", "Could not load OrderUi_controller", AlertType.ERROR);
		}
	}

	/**
	 * Handles the "Update" button click.
	 * Validates input and sends the updated order to the server.
	 * 
	 * @param event The action event
	 */
	@FXML
	private void handleUpdate(ActionEvent event) {
		try {
			if (clientNameField.getText().isEmpty() || guestsField.getText().isEmpty()
					|| datePicker.getValue() == null) {
				Alarm.showAlert("Error", "Name, Guests and Date are required.", Alert.AlertType.WARNING);
				return;
			}

			// 2. Collect data from fields
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
			LocalTime localTime = LocalTime.parse(timeField.getText()); // Expecting HH:mm
			Date newOrderDate = Date.from(localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());

			Date newArrivalTime = o.getArrivalTime(); // Default: Old value
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
						status);

				ol.updateOrder(updatedOrder);
				OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);

				if (controller != null) {
					controller.initData(emp, clientUi, this.isManager);
				} else {
					Alarm.showAlert("Error Loading", "Could not load OrderUi_controller", AlertType.ERROR);
				}
				// Refresh table in main screen (if passed in initData)
				if (mainController != null) {
					mainController.refreshTableData(); // Assuming this function calls GET_ALL
				}
			}

		} catch (NumberFormatException e) {
			String header = "Format Error";
			String context = "Check that Guests and Price are valid numbers.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
		} catch (Exception e) { // Catches Date/Time parsing errors as well
			String header = "Error";
			String context = "Check time format (HH:mm) or connection.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			e.printStackTrace();
		}
	}
}