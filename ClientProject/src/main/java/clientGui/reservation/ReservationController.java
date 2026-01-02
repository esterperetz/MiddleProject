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
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.MessageListener;
import clientLogic.OrderLogic;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Order;
import entities.Response;
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

			System.out.println("Customer ID used for order: " + customerId);

			// יצירת אובייקט ההזמנה
			Order newOrder = new Order(0, orderDate, guests, 0, customerId, // יהיה null או 0 אם זה אורח
					null, new Date(), null, null, 0.0, Order.OrderStatus.APPROVED);

			// הכנת המידע לשליחה (Map)
			Map<String, Object> requestData = new HashMap<>();
			requestData.put("order", newOrder);

			// אם זה אורח - יוצרים אובייקט לקוח ומוסיפים למפ
			if (isSubscriber != CustomerType.SUBSCRIBER) {
				Customer guest = new Customer(null, null, nameField.getText(), phoneField.getText(),
						emailField.getText(), CustomerType.REGULAR);
				requestData.put("guest", guest);
			}

			// --- שליחת הבקשה לשרת ---
			if (orderLogic != null) {
				// שינוי קריטי: שולחים את המפ (requestData) ולא רק את ההזמנה!
				orderLogic.createOrder(requestData);
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
		Platform.runLater(() -> {
			if (msg instanceof Response) {
				Response res = (Response) msg;
				if (res.getResource() == ResourceType.ORDER && res.getAction() == ActionType.CREATE) {
					System.out.println("hereee!");
					if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
						MainNavigator.showAlert("Success", "Table Booked successfully!", Alert.AlertType.INFORMATION);
						super.loadScreen("navigation/SelectionScreen", currentEvent, clientUi);
					} else {
						errorLabel.setText("Error: " + res.getMessage_from_server());
					}
				}
			}
		});
	}
}