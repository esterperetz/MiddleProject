package clientGui.reservation;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.OrderLogic;
import clientLogic.UserLogic;
import clientLogic.WaitingListLogic;
import entities.*;
import entities.Order.OrderStatus;
import entities.Response.ResponseStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class ReservationController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private DatePicker datePicker;
	@FXML
	private TextField guestsField;
	@FXML
	private TextField nameField;
	@FXML
	private TextField phoneField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField subscriberIdField;
	@FXML
	private Label lblSubCode;
	@FXML
	private ComboBox<OrderStatus> statusComboBox;
	@FXML
	private Label errorLabel;
	@FXML
	private TilePane timeContainer;

	private OrderLogic orderLogic;
	private UserLogic userLogic;
	private WaitingListLogic waitingListLogic;

	private Employee connectedEmployee;
	private Customer connectedCustomer;
	private int subCode;
	private boolean isEmployeeMode = false;
	private CustomerType isSubscriber;

	private Customer verifiedSubscriber = null;
	private boolean isSubscriberVerified = false;
	private String selectedTime = null;
	private Button selectedButton = null;
	private boolean isWaitlistSlot = false;
	private final int TABLE_CAPACITY = 20;

	private ActionEvent currentEvent;

	@FXML
	public void initialize() {
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

		datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
			if (newDate != null)
				loadHours();
		});

		guestsField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal)
				loadHours(); // Load on focus lost
		});

		subscriberIdField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (!isNowFocused && isEmployeeMode) {
				checkSubscriberId();
			}
		});
	}

	/**
	 * Unified Init Data. Can be called by Employee Dashboard OR Customer Dashboard.
	 */
	public void initData(ClientUi clientUi, CustomerType isSubscriber, Integer subCode, Object user) {
		this.clientUi = clientUi;
		this.isSubscriber = isSubscriber;
		this.orderLogic = new OrderLogic(clientUi);
		this.userLogic = new UserLogic(clientUi);
		this.waitingListLogic = new WaitingListLogic(clientUi);

		if (user instanceof Employee) {
			// --- MODE: EMPLOYEE ---
			this.connectedEmployee = (Employee) user;
			this.isEmployeeMode = true;
			setupUIForEmployee();
		} else if (user instanceof Customer) {
			// --- MODE: CLIENT (Subscriber or Regular) ---
			this.subCode = subCode;
			this.connectedCustomer = (Customer) user;
			this.isEmployeeMode = false;
			setupUIForClient();
		}

		loadHours();
	}

	private void setupUIForEmployee() {
		if (subscriberIdField != null) {
			subscriberIdField.setVisible(true);
			subscriberIdField.setManaged(true);
			subscriberIdField.setEditable(true);
		}

		if (lblSubCode != null) {
			lblSubCode.setVisible(true);
			lblSubCode.setManaged(true);
		}

		enableClientFields();
	}

	private void setupUIForClient() {
		if (subscriberIdField != null) {
			subscriberIdField.setVisible(false);
			subscriberIdField.setManaged(false);
		}

		if (lblSubCode != null) {
			lblSubCode.setVisible(false);
			lblSubCode.setManaged(false);
		}
		System.out.println("hello " + connectedCustomer);

		if (this.isSubscriber == CustomerType.SUBSCRIBER) {
			System.out.println("in the if 1");
			this.verifiedSubscriber = connectedCustomer;
			this.isSubscriberVerified = true;
			checkSubscriberId();

			// fillAndLockFields(connectedCustomer);
		} else {
			enableClientFields();
		}
	}

	// --- Logic Methods ---

	private void loadHours() {
		timeContainer.getChildren().clear();
		selectedTime = null;
		if (datePicker.getValue() == null)
			return;

		int guests = 0;
		try {
			String gText = guestsField.getText().trim();
			if (!gText.isEmpty())
				guests = Integer.parseInt(gText);
		} catch (NumberFormatException e) {
			/* ignore */ }

		Order checkReq = new Order();
		java.sql.Date sqlDate = java.sql.Date.valueOf(datePicker.getValue());
		checkReq.setOrderDate(sqlDate);
		checkReq.setNumberOfGuests(guests);
		orderLogic.checkAvailability(checkReq);
	}

	private void checkSubscriberId() {
		System.out.println("in method ");
		String idStr = subscriberIdField.getText().trim();
		if (idStr.isEmpty() && !isSubscriberVerified) {
			System.out.println("sdfsdf");
			isSubscriberVerified = false;
			verifiedSubscriber = null;
			enableClientFields();
			return;
		}
		try {
			System.out.println("in the try");
			int subCode = this.subCode;
			if (isEmployeeMode) {
				subCode = Integer.parseInt(idStr);
			}
			userLogic.getSubscriberById(subCode);
		} catch (NumberFormatException e) {
			errorLabel.setText("ID must be numbers only");
		}
	}

	@FXML
	void submitReservation(ActionEvent event) {
		this.currentEvent = event;
		errorLabel.setText("");

		if (selectedTime == null) {
			Alarm.showAlert("Missing Input", "Please select a time!", Alert.AlertType.WARNING);
			return;
		}
		if (guestsField.getText().isEmpty() || nameField.getText().isEmpty() || phoneField.getText().isEmpty()) {
			Alarm.showAlert("Missing Input", "Please fill all fields.", Alert.AlertType.WARNING);
			return;
		}

		try {
			int guests = Integer.parseInt(guestsField.getText().trim());
			if (guests > TABLE_CAPACITY) {
				Alarm.showAlert("Capacity Error", "Max capacity is " + TABLE_CAPACITY, Alert.AlertType.ERROR);
				return;
			}

			// הכנת נתוני זמן
			LocalDate localDate = datePicker.getValue();
			LocalTime localTime = LocalTime.parse(selectedTime);
			LocalDateTime ldt = LocalDateTime.of(localDate, localTime);
			java.sql.Timestamp finalResTime = java.sql.Timestamp.valueOf(ldt);
			java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

			// זיהוי הלקוח
			Customer customerForOrder;

			if (isSubscriberVerified && verifiedSubscriber != null) {
				customerForOrder = verifiedSubscriber;
			} else {
				customerForOrder = new Customer(null, null, nameField.getText(), phoneField.getText(),
						emailField.getText(), CustomerType.REGULAR);

				// בדיקת null לפני שימוש בשדה המנוי
				if (isEmployeeMode && subscriberIdField != null && !subscriberIdField.getText().isEmpty()) {
					try {
						customerForOrder.setSubscriberCode(Integer.parseInt(subscriberIdField.getText()));
					} catch (Exception e) {
					}
				}
			}

			Order newOrder = new Order(0, finalResTime, guests, 0, customerForOrder, null, now, null, null, 0.0, null);
			newOrder.setDateOfPlacingOrder(now);

			if (isWaitlistSlot) {
				WaitingList wlItem = new WaitingList(0, 0, guests, finalResTime, 0, customerForOrder);
				Optional<ButtonType> result = Alarm.showAlertAndConformation("Fully Booked",
						"Slot is full. Join waiting list?", Alert.AlertType.CONFIRMATION);
				if (result.isPresent() && result.get() == ButtonType.OK) {
					wlItem.setReservationDate(newOrder.getOrderDate());
					waitingListLogic.enterToWaitingList(wlItem);
					if (isEmployeeMode == true) {
						ManagerOptionsController m = super.loadScreen("managerTeam/EmployeeOption", event,
								this.clientUi);
						if (m != null)
							m.initData(connectedEmployee,clientUi, connectedEmployee.getRole());
						else
							System.out.println(
									"Error  move to to screen  fromReservationController to ManagerOptionsController after get order to waitingList");

					} else {
						SubscriberOptionController sub = super.loadScreen("user/SubscriberOption", event,
								this.clientUi);
						if (sub != null)
							sub.initData(clientUi, isSubscriber, subCode, connectedCustomer);
						else
							System.out.println(
									"Error  move to to screen  fromReservationController to SubscriberOptionController after get order to waitingList");
					}
				}
				return;
			}

			if (!isSubscriberVerified && isEmployeeMode) {
				this.pendingOrder = newOrder;
				userLogic.createCustomer(customerForOrder);
			} else {
				orderLogic.createOrder(newOrder);
			}

		} catch (NumberFormatException e) {
			errorLabel.setText("Invalid number format.");
		}
	}

	// --- Message Handling ---

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
				case CUSTOMER:
					handleCustomerResponse(res);
					break;
				case WAITING_LIST:
					handleWaitingListResponse(res);
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void handleWaitingListResponse(Response res) {
		if (res.getAction() == ActionType.ENTER_WAITING_LIST) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				Alarm.showAlert("Waiting List", "Added to waiting list successfuly!", Alert.AlertType.INFORMATION);
			} else {
				Alarm.showAlert("Waiting List", res.getMessage_from_server(), Alert.AlertType.ERROR);
			}
		}

	}

	// Temp storage for order while waiting for customer creation
	private Order pendingOrder;

	private void handleOrderResponse(Response res) {
		if (res.getAction() == ActionType.CHECK_AVAILABILITY) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				if (res.getData() instanceof List) {
					updateTimeButtons((List<TimeSlotStatus>) res.getData());
				}
			} else if (res.getStatus() == ResponseStatus.ERROR) {
				Alarm.showAlert("Error", res.getMessage_from_server(), Alert.AlertType.ERROR);
				guestsField.setText("");
				loadHours();
				updateTimeButtons((List<TimeSlotStatus>) res.getData());

			}

		} else if (res.getAction() == ActionType.CREATE) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				Alarm.showAlert("Success", "Order created successfully!", Alert.AlertType.INFORMATION);
				goBack(currentEvent);
			} else {
				Alarm.showAlert("Error", res.getMessage_from_server(), Alert.AlertType.ERROR);
			}
		}
	}

	private void handleCustomerResponse(Response res) {
		if (res.getAction() == ActionType.GET_BY_ID) {
			Customer customer = (Customer) res.getData();
			if (res.getStatus() == ResponseStatus.SUCCESS && customer != null) {
				this.verifiedSubscriber = customer;
				this.isSubscriberVerified = true;
				fillAndLockFields(customer);
				errorLabel.setText("Subscriber found: " + customer.getName());
				errorLabel.setStyle("-fx-text-fill: green;");
			} else {
				isSubscriberVerified = false;
				verifiedSubscriber = null;
				enableClientFields();
				errorLabel.setText("Subscriber ID not found.");
				errorLabel.setStyle("-fx-text-fill: red;");
			}
		} else if (res.getAction() == ActionType.REGISTER_CUSTOMER) {
			// Logic for when Employee creates a new user, then we immediately place the
			// order
			if (res.getStatus() == ResponseStatus.SUCCESS && pendingOrder != null) {
				Customer createdCus = (Customer) res.getData();
				pendingOrder.getCustomer().setCustomerId(createdCus.getCustomerId());
				orderLogic.createOrder(pendingOrder);
				pendingOrder = null; // Clear
			}
		}
	}

	// --- Helper UI Methods ---

	private void updateTimeButtons(List<TimeSlotStatus> slots) {
		timeContainer.getChildren().clear();
		if (slots == null) {
			return;
		}
		for (TimeSlotStatus slot : slots) {
			Button btn = new Button(slot.getTime());
			btn.setMinWidth(80);
			btn.setMinHeight(30);

			btn.getStyleClass().add("time-button");

			if (slot.getCurrentDiners() >= slot.getMaxCapacity()) {
				btn.getStyleClass().add("waiting-list-button");
				btn.setOnAction(e -> selectTime(btn, slot.getTime(), true));
			} else {
				btn.getStyleClass().add("available-button");
				btn.setOnAction(e -> selectTime(btn, slot.getTime(), false));
			}
			timeContainer.getChildren().add(btn);
		}
	}

	private void selectTime(Button btn, String time, boolean isWaitlist) {
		if (selectedButton != null)
			selectedButton.setStyle(""); // Reset previous
		selectedButton = btn;
		selectedTime = time;
		btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // Highlight
		this.isWaitlistSlot = isWaitlist;

		if (isWaitlist)
			errorLabel.setText("Note: You are selecting a Waiting List slot.");
		else
			errorLabel.setText("");
	}

	private void fillAndLockFields(Customer cus) {
		nameField.setText(cus.getName());
		phoneField.setText(cus.getPhoneNumber());
		emailField.setText(cus.getEmail());

		nameField.setEditable(false);
		phoneField.setEditable(false);
		emailField.setEditable(false);

		// Visual feedback for locked fields
		String locked = "-fx-background-color: #2A2A2A; -fx-text-fill: #AAAAAA; -fx-border-color: #444;";
		nameField.setStyle(locked);
		phoneField.setStyle(locked);
		emailField.setStyle(locked);
	}

	private void enableClientFields() {
		if (!nameField.isEditable()) {
			nameField.clear();
			phoneField.clear();
			emailField.clear();
		}
		nameField.setEditable(true);
		phoneField.setEditable(true);
		emailField.setEditable(true);

		nameField.setStyle("");
		phoneField.setStyle("");
		emailField.setStyle("");
	}

	@FXML
	void goBack(ActionEvent event) {
		if (isEmployeeMode) {
			OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);
			if (controller != null) {
				controller.initData(connectedEmployee, clientUi, connectedEmployee.getRole());
			} else {
				System.err.println("Error: Could not load orderUiController.");
			}
		} else {
			SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, this.clientUi);
			if (controller != null) {
				controller.initData(clientUi, isSubscriber, subCode, connectedCustomer);
			} else {
				System.err.println("Error: Could not load SubscriberController.");
			}
		}

	}
}