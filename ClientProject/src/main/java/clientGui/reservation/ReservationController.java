package clientGui.reservation;

import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import client.MessageListener;
import clientLogic.OrderLogic;
import entities.ActionType;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Order;
import entities.Response;
import entities.Response.ResponseStatus;
import entities.ResourceType;
import javafx.application.Platform;

public class ReservationController extends MainNavigator implements MessageListener<Object> {
	@FXML
	private DatePicker datePicker;
	@FXML
	private ComboBox<String> timeComboBox;
	@FXML
	private TextField dinersField;
	@FXML
	private TextField nameField;
	@FXML
	private TextField phoneField;
	@FXML
	private TextField emailField;
	@FXML
	private Label errorLabel;

	private CustomerType isSubscriber;
	private Integer customerId;
	private OrderLogic orderLogic;
	private ActionEvent currentEvent;

	@FXML
	public void initialize() {

		Platform.runLater(() -> {
			if (datePicker.getScene() != null && datePicker.getScene().getWindow() != null) {
				Stage stage = (Stage) datePicker.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();

				});
			}
		});
		setupDateConstraints();
		datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
			if (newDate != null) {
				updateTimeSlots(newDate);
			}
		});
		phoneField.setOnMouseClicked(e -> errorLabel.setText(""));
	}

	public void initData(ClientUi clientUi, CustomerType isSubscriberStatus, Integer cusId) {
		this.clientUi = clientUi;
		this.isSubscriber = isSubscriberStatus;
		this.customerId = cusId;
		this.orderLogic = new OrderLogic(clientUi);
	}

	private void setupDateConstraints() {
		Callback<DatePicker, DateCell> dayCellFactory = picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				LocalDate today = LocalDate.now();
				LocalDate nextMonth = today.plusMonths(1);
				if (item.isBefore(today) || item.isAfter(nextMonth)) {
					setDisable(true);
					setStyle("-fx-background-color: #ffc0cb;");
				}
			}
		};
		datePicker.setDayCellFactory(dayCellFactory);
		datePicker.setValue(LocalDate.now());
	}

	private void updateTimeSlots(LocalDate selectedDate) {
		List<String> validSlots = new ArrayList<>();
		LocalTime openTime = LocalTime.of(12, 0);
		LocalTime closeTime = LocalTime.of(22, 0);
		LocalTime minOrderTime = LocalTime.now().plusHours(1);

		LocalTime slot = openTime;
		while (slot.isBefore(closeTime)) {
			if (selectedDate.isEqual(LocalDate.now())) {
				if (slot.isAfter(minOrderTime)) {
					validSlots.add(slot.toString());
				}
			} else {
				validSlots.add(slot.toString());
			}
			slot = slot.plusMinutes(30);
		}
		timeComboBox.setItems(FXCollections.observableArrayList(validSlots));
	}

	@FXML
	void submitReservation(ActionEvent event) {
		this.currentEvent = event;

		// 1. בדיקות בסיסיות (תאריך, שעה, סועדים)
		if (datePicker.getValue() == null || timeComboBox.getValue() == null || dinersField.getText().isEmpty()) {
			errorLabel.setText("Please fill Date, Time and Diners.");
			return;
		}

		// 2. בדיקה ספציפית לאורחים (לא מנויים) - רק הם חייבים למלא פרטים אישיים
		if (isSubscriber != CustomerType.SUBSCRIBER) {
			if (phoneField.getText().isEmpty() || emailField.getText().isEmpty() || nameField.getText().isEmpty()) {
				errorLabel.setText("Guest must provide Name, Phone and Email.");
				return;
			}
		}

		try {
			LocalDate localDate = datePicker.getValue();
			LocalTime localTime = LocalTime.parse(timeComboBox.getValue());
			Date orderDate = Date.from(localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());
			int guests = Integer.parseInt(dinersField.getText().trim());
			String customerEmail = emailField.getText();
			String customerName = nameField.getText();
			String customerNumber = phoneField.getText();

			System.out.println("Customer ID used for order: " + customerId);

			Order newOrder = new Order(0, orderDate, guests, 0, customerId, // יהיה null או 0 אם זה אורח
					null, new Date(), null, null, 0.0, null);
			newOrder.setTempClientEmail(customerEmail);
			newOrder.setCustomerName(customerName);
			newOrder.setTempClientPhone(customerNumber);
			Map<String, Object> requestData = new HashMap<>();
			requestData.put("order", newOrder);

			if (isSubscriber != CustomerType.SUBSCRIBER) {
				Customer guest = new Customer(null, null, nameField.getText(), phoneField.getText(),
						emailField.getText(), CustomerType.REGULAR);
				requestData.put("guest", guest);
			}

			if (orderLogic != null) {
				orderLogic.createOrder(newOrder);
			}

		} catch (NumberFormatException e) {
			errorLabel.setText("Invalid input: Diners must be a number.");
		} catch (Exception e) {
			errorLabel.setText("Error creating reservation.");
			e.printStackTrace();
		}
	}

	@FXML
	void goBack(ActionEvent event) {
		SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
		if (controller != null) {
			controller.initData(clientUi, isSubscriber, customerId);
		}
	}

	@Override
	public void onMessageReceive(Object msg) {
		if (!(msg instanceof Response))
			return;
		Response res = (Response) msg;

		Platform.runLater(() -> {
			try {
				switch (res.getResource()) {
				case ORDER:
					handleOrderResponse(res);
					break;

				default:
					System.out.println("Unhandled resource: " + res.getResource());

			
				}
			} catch (Exception e) {
				e.printStackTrace();
				Alarm.showAlert("System Error", "An error occurred while processing server response.",
						Alert.AlertType.ERROR);
			}
		});

	}
	
	public void showSimpleTimePopup(List<String> alternatives) {
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.setTitle("Restaurant Full");
	    alert.setHeaderText("The requested time is fully booked.");
	    alert.setContentText("We found nearby available slots. Please choose a time:");

	    List<ButtonType> buttons = new ArrayList<>();

	    // Create a button for each time string (e.g., "19:30")
	    for (String timeStr : alternatives) {
	        ButtonType timeBtn = new ButtonType(timeStr);
	        buttons.add(timeBtn);
	    }

	    // Add a Cancel button
	    ButtonType cancelBtn = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	    buttons.add(cancelBtn);

	    // Set the custom buttons to the alert
	    alert.getButtonTypes().setAll(buttons);

	    // Show the dialog and wait for user selection
	    Optional<ButtonType> result = alert.showAndWait();

	    // If the user clicked a time button (and not Cancel)
	    if (result.isPresent() && result.get() != cancelBtn) {
	        String selectedTime = result.get().getText();
	        System.out.println("User selected: " + selectedTime);
	        
	        // Update the UI immediately
//	        txtTime.setText(selectedTime); 
	    }
	}
	private void handleOrderResponse(Response res) {
		if (res.getAction() == ActionType.CREATE) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				Alarm.showAlert("Success", "Table Booked successfully!", Alert.AlertType.INFORMATION);
				super.loadScreen("navigation/SelectionScreen", currentEvent, clientUi);
			} else {
				errorLabel.setText("Error: " + res.getMessage_from_server());

			}
		} else if (res.getAction() == ActionType.CHECK_AVAILABILITY) {
			if (res.getStatus() == ResponseStatus.ERROR) {
				
				Alarm.showAlert("Error", res.getMessage_from_server(), Alert.AlertType.ERROR);
				List<String> ls = new ArrayList<>();
				ls.add("19:00");
				ls.add("20:00");
				showSimpleTimePopup(ls);
			}
		}
	}
}