package clientGui.reservation;

import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import client.MessageListener;
import clientLogic.OrderLogic;
import clientLogic.WaitingListLogic;
import entities.ActionType;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Order;
import entities.Response;
import entities.Response.ResponseStatus;
import entities.TimeSlotStatus;
import entities.WaitingList;
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
	private Integer subscriberCode;
	private OrderLogic orderLogic;
	private WaitingListLogic waitingListLogic;
	
	private ActionEvent currentEvent;

	@FXML
	private TilePane timeContainer;

	private String selectedTime = null;
	private Button selectedButton = null;
	private boolean isWaitlist = false;

	@FXML
	public void initialize() {
		datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
			if (newDate != null) {
				loadHours();
			}
		});

		Platform.runLater(() -> {
			if (datePicker.getScene() != null && datePicker.getScene().getWindow() != null) {
				Stage stage = (Stage) datePicker.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();
				});
			}
		});
		datePicker.setValue(LocalDate.now());

		datePicker.setDayCellFactory(picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				LocalDate today = LocalDate.now();
				LocalDate maxDate = today.plusMonths(1);
				if (date.isBefore(today) || date.isAfter(maxDate)) {
					setDisable(true);
					setStyle("-fx-opacity: 0.25;");
				}
			}
		});

//		setupDateConstraints();
//		datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
//			if (newDate != null) {
//				updateTimeSlots(newDate);
//			}
//		});
//		phoneField.setOnMouseClicked(e -> errorLabel.setText(""));
	}

	public void initData(ClientUi clientUi, CustomerType isSubscriberStatus, Integer subscriberCode) {
		this.clientUi = clientUi;
		System.out.println("in reservation "+subscriberCode);
		this.isSubscriber = isSubscriberStatus;
		this.subscriberCode = subscriberCode;
		this.orderLogic = new OrderLogic(clientUi);
		this.waitingListLogic = new WaitingListLogic(clientUi);

		loadHours();
	}

//	private void setupDateConstraints() {
//		Callback<DatePicker, DateCell> dayCellFactory = picker -> new DateCell() {
//			@Override
//			public void updateItem(LocalDate item, boolean empty) {
//				super.updateItem(item, empty);
//				LocalDate today = LocalDate.now();
//				LocalDate nextMonth = today.plusMonths(1);
//				if (item.isBefore(today) || item.isAfter(nextMonth)) {
//					setDisable(true);
//					setStyle("-fx-background-color: #ffc0cb;");
//				}
//			}
//		};
//		datePicker.setDayCellFactory(dayCellFactory);
//		datePicker.setValue(LocalDate.now());
//	}

//	private void updateTimeSlots(LocalDate selectedDate) {
//		List<String> validSlots = new ArrayList<>();
//		LocalTime openTime = LocalTime.of(12, 0);
//		LocalTime closeTime = LocalTime.of(22, 0);
//		LocalTime minOrderTime = LocalTime.now().plusHours(1);
//
//		LocalTime slot = openTime;
//		while (slot.isBefore(closeTime)) {
//			if (selectedDate.isEqual(LocalDate.now())) {
//				if (slot.isAfter(minOrderTime)) {
//					validSlots.add(slot.toString());
//				}
//			} else {
//				validSlots.add(slot.toString());
//			}
//			slot = slot.plusMinutes(30);
//		}
//		timeComboBox.setItems(FXCollections.observableArrayList(validSlots));
//	}



	@FXML
	void submitReservation(ActionEvent event) {
		this.currentEvent = event;

		if (selectedTime == null) {
			errorLabel.setText("Please select a time!");
			errorLabel.setStyle("-fx-text-fill: red;");
			return;
		}

		if (isSubscriber != CustomerType.SUBSCRIBER) {
			if (phoneField.getText().isEmpty() || emailField.getText().isEmpty() || nameField.getText().isEmpty()) {
				errorLabel.setText("Guest must provide Name, Phone and Email.");
				return;
			}
		}

		try {
			if (dinersField.getText().isEmpty()) {
				errorLabel.setText("Guest must provide Name, Phone and Email.");
				return;
			}
			int guests = Integer.parseInt(dinersField.getText().trim());

			LocalDate localDate = datePicker.getValue();
			LocalTime localTime = LocalTime.parse(selectedTime);
			LocalDateTime ldt = LocalDateTime.of(localDate, localTime);

			java.sql.Timestamp finalReservationTime = java.sql.Timestamp.valueOf(ldt);
			java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
			String customerEmail = emailField.getText();
			String customerName = nameField.getText();
			String customerNumber = phoneField.getText();

			System.out.println("Booking for: " + finalReservationTime);
			Customer newCustomer = new Customer(subscriberCode, customerName, customerNumber, customerEmail);
			Order newOrder = new Order(0, finalReservationTime, guests, 0, newCustomer, null, now, null, null, 0.0,
					null);
			
			Map<String, Object> requestData = new HashMap<>();
			requestData.put("order", newOrder);
			newOrder.setDateOfPlacingOrder(now);

			requestData.put("order", newOrder);
			Customer customer;
			if (isSubscriber != CustomerType.SUBSCRIBER) {
				 customer = new Customer(null, null, nameField.getText(), phoneField.getText(),
						emailField.getText(), CustomerType.REGULAR);
				requestData.put("guest", customer);
			}else {
				customer = new Customer(null, subscriberCode, null,null,
						null, null);
			}
			
			WaitingList waitingList = new WaitingList(0,0,guests,finalReservationTime,0,customer);
			if (isWaitlist) {
			    // 1. שומרים את התוצאה שחזרה מה-Alarm
				Optional<ButtonType> result = Alarm.showAlertAndConformation(
				        "Fully Booked", 
			        "This slot is full. Join waiting list for " + finalReservationTime + "?",
			        Alert.AlertType.CONFIRMATION
			    );
			    if (result.isPresent() && result.get() == ButtonType.OK) {
			        waitingListLogic.enterToWaitingList(waitingList);
			    } else {
			        System.out.println("User cancelled joining the waiting list");
			    }
			    return;
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
			controller.initData(clientUi, isSubscriber, subscriberCode);
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

	private void loadHours() {
		timeContainer.getChildren().clear();
		selectedTime = null;

		if (datePicker.getValue() == null)
			return;

		int guests = 0;
		try {
			String dinersText = dinersField.getText().trim();
			if (!dinersText.isEmpty()) {
				guests = Integer.parseInt(dinersText);
			}
		} catch (NumberFormatException e) {
			System.out.println("exception in load hours");
		}

		Order checkReq = new Order();
		java.sql.Date sqlDate = java.sql.Date.valueOf(datePicker.getValue());

		checkReq.setOrderDate(sqlDate);
		checkReq.setDateOfPlacingOrder(sqlDate);
		checkReq.setNumberOfGuests(guests);

		if (orderLogic != null) {
			orderLogic.checkAvailability(checkReq);
		}
	}

	private void selectTime(Button btn, String time, boolean isWaitlist) {

		if (selectedButton != null) {
			selectedButton.getStyleClass().remove("selected-time");
		}

		selectedButton = btn;
		selectedTime = time;
		btn.getStyleClass().add("selected-time");

		if (isWaitlist) {
			errorLabel.setText("You joined the waitlist for " + time);
			errorLabel.setStyle("-fx-text-fill: orange;");
		} else {
			errorLabel.setText("");
		}
	}

	private void updateTimeButtons(List<TimeSlotStatus> slots) {
		timeContainer.getChildren().clear();
		selectedTime = null;

		for (TimeSlotStatus slot : slots) {
			Button btn = new Button(slot.getTime());
			btn.getStyleClass().add("time-button");
			btn.setMinWidth(80);

			if (slot.isFull()) {
				btn.getStyleClass().add("waitlist");
				isWaitlist = true;
				btn.setOnAction(e -> selectTime(btn, slot.getTime(), true));
			} else {
				btn.getStyleClass().add("available");
				isWaitlist = false;
				btn.setOnAction(e -> selectTime(btn, slot.getTime(), false));
			}

			timeContainer.getChildren().add(btn);
		}
	}
	////////////////////////////////////

	private void handleOrderResponse(Response res) {

		if (res.getAction() == ActionType.CREATE) {

			if (res.getStatus() == ResponseStatus.SUCCESS) {
				Alarm.showAlert("Success", "Table Booked successfully!", Alert.AlertType.INFORMATION);
				super.loadScreen("navigation/SelectionScreen", currentEvent, clientUi);
			}

			else if (res.getStatus() == ResponseStatus.ERROR) {

				Alarm.showAlert("Fully Booked", res.getMessage_from_server(), Alert.AlertType.WARNING);
				errorLabel.setText(res.getMessage_from_server());

				if (res.getData() instanceof List) {
					try {

						List<TimeSlotStatus> slots = (List<TimeSlotStatus>) res.getData();

						updateTimeButtons(slots);

					} catch (ClassCastException e) {
						System.out.println("Data received was not a list of TimeSlotStatus");
					}
				}
			}
		}

		else if (res.getAction() == ActionType.CHECK_AVAILABILITY) {
			if (res.getData() instanceof List) {
				updateTimeButtons((List<TimeSlotStatus>) res.getData());
			}
		}
	}

}