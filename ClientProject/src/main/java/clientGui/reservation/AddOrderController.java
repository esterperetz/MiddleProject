package clientGui.reservation;

import java.net.URL;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import clientLogic.UserLogic;
import entities.ActionType;
import entities.Alarm;
import entities.Order;
import entities.ResourceType;
import entities.Response;
import entities.Subscriber;
import entities.Order.OrderStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AddOrderController extends MainNavigator implements MessageListener<Object>, Initializable {

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
	private ComboBox<OrderStatus> statusComboBox;
	private OrderLogic orderLogic;
	private UserLogic userLogic;
	private Subscriber verifiedSubscriber = null; // משתנה חדש לשמירת האובייקט
	private boolean isSubscriberVerified = false;
	private ActionEvent currentEvent;
	private boolean isManager;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		statusComboBox.getItems().setAll(OrderStatus.values());
		statusComboBox.setValue(OrderStatus.APPROVED); // Default
		datePicker.setValue(LocalDate.now());
		// הגדרת שעה ברירת מחדל
		timeField.setText("12:00");
		try{
			subscriberIdField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
	            // בדיקה 2: האם המאזין עובד?
	          
				if (!isNowFocused) {
	                checkSubscriberId();
	            }
	        });
		}
		catch(Exception e)
		{
            System.out.println("DEBUG: Focus lost from Subscriber ID field"); 
		}
		
	}

	// public void initData(ClientUi clientUi) {
	public void initData(boolean isManager) {
		// this.clientUi = clientUi;
		this.isManager=isManager;
		this.orderLogic = new OrderLogic(this.clientUi);
		this.userLogic = new UserLogic(this.clientUi);
	}

	private void checkSubscriberId() {
		String subIdStr = subscriberIdField.getText().trim();
		// subscriber_id empty
		if (subIdStr.isEmpty()) {
			isSubscriberVerified = false;
			this.verifiedSubscriber = null;
			enableClientFields(); // open fields to edit
			return;
		}

		//

		try {
			int subId = Integer.parseInt(subIdStr);
			// שולח בקשה לשרת. התשובה תגיע ל-onMessageReceive
			userLogic.getSubscriberById(subId);
		} catch (NumberFormatException e) {
			handleInvalidSubscriber("Invalid format. Subscriber ID must be numbers only.");
		}
	}

	@FXML
	private void handleSave(ActionEvent event) {
//		checkSubscriberId();
		this.currentEvent = event;
		valid();

	}

	// input validate
	public void valid() {
		Platform.runLater(() -> {
			if (!subscriberIdField.getText().trim().isEmpty() && !isSubscriberVerified) {
				Alarm.showAlert("Verification Required", "Waiting for subscriber verification...",
						Alert.AlertType.WARNING);
				return;
			}
			if (clientNameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()
					|| guestsField.getText().trim().isEmpty() || datePicker.getValue() == null) {

				Alarm.showAlert("Missing Input", "Please fill in mandatory fields (Name, Phone, Guests, Date).",
						Alert.AlertType.WARNING);
				return;
			}

			try {
				// 2. איסוף נתונים מהשדות
				String clientName;
				String clientPhone;
				String clientEmail;

				if (isSubscriberVerified && verifiedSubscriber != null) {
					// אם זה מנוי מאומת - לוקחים ישר מהמקור (האובייקט) ולא מהשדות!
					clientName = verifiedSubscriber.getSubscriberName();
					clientPhone = verifiedSubscriber.getPhoneNumber();
					clientEmail = verifiedSubscriber.getEmail();
				} else {
					// אם זה לקוח מזדמן - לוקחים מהשדות שהמשתמש הקליד
					clientName = clientNameField.getText().trim();
					clientPhone = phoneField.getText().trim();
					clientEmail = emailField.getText().trim();
				}

				// המרת מנוי (אם יש)
				Integer subId = null;
				if (!subscriberIdField.getText().trim().isEmpty()) {
					subId = Integer.parseInt(subscriberIdField.getText().trim());
				}

				// המרת מספרים
				int guests = Integer.parseInt(guestsField.getText().trim());
				//double price = priceField.getText().trim().isEmpty() ? 0.0
					//	: Double.parseDouble(priceField.getText().trim());

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
				//if (!arrivalString.isEmpty()) {
					//if (!arrivalString.matches("\\d{2}:\\d{2}")) {
						//Alarm.showAlert("Time Error", "Arrival time must be HH:mm format.", Alert.AlertType.ERROR);
						//return;
					//}
					//LocalTime arrivalTime = LocalTime.parse(arrivalString);
					// משתמשים באותו תאריך שנבחר ב-DatePicker עבור שעת ההגעה
					//arrivalDate = Date.from(localDate.atTime(arrivalTime).atZone(ZoneId.systemDefault()).toInstant());
				//}

				OrderStatus status = statusComboBox.getValue();

				// 5. יצירת האובייקט עם הבנאי החדש והמלא
				Order newOrder = new Order(0, // order_number (אוטומטי ב-DB)
						orderDate, // order_date (תאריך ושעה)
						guests, // number_of_guests
						0, // confirmation_code (נוצר בשרת)
						subId, // subscriber_id
						null, new Date(), // date_of_placing_order (עכשיו)
						clientName, // client_name (השדה החדש)
						clientEmail, // client_email (השדה החדש)
						clientPhone, // client_Phone (השדה החדש)
						arrivalDate, // ArrivalTime (השדה החדש)
						null, 0, // total_price
						status // order_status
				);

				// 6. שליחה לשרת ומעבר מסך
				if (orderLogic != null) {
					orderLogic.createOrder(newOrder);

					// מעבר למסך הבא
					OrderUi_controller controller = super.loadScreen("reservation/orderUi", currentEvent, clientUi);

					if (controller != null) {
						controller.initData(this.isManager);
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
				// e.printStackTrace();
			}
		});

	}

	@FXML
	private void handleCancel(ActionEvent event) {
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
		if (controller != null) {
			controller.initData(this.isManager);
		} else {
			System.err.println("Error: Could not load OrderUi_controllerr.");
		}
	}

	@SuppressWarnings({ "unlikely-arg-type" })
	@Override
	public void onMessageReceive(Object msg) {

		Platform.runLater(() -> {
			if (msg instanceof Response) {
				Response res = (Response) msg;
				if (res.getResource() == ResourceType.SUBSCRIBER && res.getAction() == ActionType.GET_BY_ID) {
					// בדיקה האם המנוי נמצא
					if (res.getStatus() == Response.ResponseStatus.SUCCESS && res.getData() instanceof Subscriber) {
						// === מקרה 2: מנוי קיים ותקין ===
						Subscriber sub = (Subscriber) res.getData();
						this.verifiedSubscriber = sub;
						fillAndLockFields(sub);
						isSubscriberVerified = true;
						valid();

					} else {
						// === מקרה 3: מנוי לא קיים (Exception) ===
						// השרת החזיר שאין מנוי כזה (או החזיר null/Error)
						handleInvalidSubscriber(
								"Subscriber ID " + subscriberIdField.getText() + " does not exist in the system.");
					}
				} else {
					System.out.println("Check the if ");
				}
			}
		});
	}

	private void handleInvalidSubscriber(String errorMessage) {
		isSubscriberVerified = false;
		this.verifiedSubscriber = null;
		// 1. הקפצת הודעת שגיאה (Exception למשתמש)
		Alarm.showAlert("Subscriber Not Found", errorMessage, Alert.AlertType.ERROR);

		// 2. ניקוי שדה ה-ID השגוי (כדי לא לאפשר שמירה איתו)
		subscriberIdField.clear();
		subscriberIdField.requestFocus(); // מחזיר את הפוקוס לשדה כדי שינסה שוב

		// 3. איפוס שדות הלקוח (כדי שיוכל להזין ידנית אם ירצה להיות לקוח מזדמן)
		enableClientFields();
	}

	private void fillAndLockFields(Subscriber sub) {
		clientNameField.setText(sub.getSubscriberName());
		phoneField.setText(sub.getPhoneNumber());
		emailField.setText(sub.getEmail());

		// נעילה
		clientNameField.setEditable(false);
		phoneField.setEditable(false);
		emailField.setEditable(false);

		// סימון ויזואלי שהשדות נעולים
		String lockedStyle = "-fx-background-color: #e0e0e0; -fx-background-radius: 5;";
		clientNameField.setStyle(lockedStyle);
		phoneField.setStyle(lockedStyle);
		emailField.setStyle(lockedStyle);
	}

	private void enableClientFields() {
		// אם השדות היו נעולים קודם -> ננקה אותם (כי זה אומר שהיה שם מידע של מנוי)
		if (!clientNameField.isEditable()) {
			clientNameField.clear();
			phoneField.clear();
			emailField.clear();
		}

		// פתיחה לעריכה
		clientNameField.setEditable(true);
		phoneField.setEditable(true);
		emailField.setEditable(true);

		String defaultStyle = "-fx-background-color: white; -fx-background-radius: 5;";
		clientNameField.setStyle(defaultStyle);
		phoneField.setStyle(defaultStyle);
		emailField.setStyle(defaultStyle);
	}

}