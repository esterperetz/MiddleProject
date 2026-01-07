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
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import entities.Order.OrderStatus;
import entities.Response.ResponseStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AddOrderController extends MainNavigator implements MessageListener<Object>, Initializable {

	private Order newOrder;
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
	private Customer verifiedSubscriber = null; // משתנה חדש לשמירת האובייקט
	private boolean isSubscriberVerified = false;
	private ActionEvent currentEvent;
	private Employee.Role isManager;
	private String employeeName;
	private Employee emp;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		statusComboBox.getItems().setAll(OrderStatus.values());
		statusComboBox.setValue(OrderStatus.APPROVED); // Default
		datePicker.setValue(LocalDate.now());
		// הגדרת שעה ברירת מחדל
		timeField.setText("12:00");
		try {
			subscriberIdField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
				// בדיקה 2: האם המאזין עובד?

				if (!isNowFocused) {
					checkSubscriberId();
				}
			});
		} catch (Exception e) {
			System.out.println("DEBUG: Focus lost from Subscriber ID field");
		}

	}

	// public void initData(ClientUi clientUi) {
	public void initData(Employee emp, Employee.Role isManager) {
		this.emp = emp;
//		this.employeeName = employeeName;
		this.isManager = isManager;
		this.orderLogic = new OrderLogic(this.clientUi);
		this.userLogic = new UserLogic(this.clientUi);
	}

	private void checkSubscriberId() {
		String cusIdStr = subscriberIdField.getText().trim();
		// subscriber_id empty
		if (cusIdStr.isEmpty()) {
			isSubscriberVerified = false;
			this.verifiedSubscriber = null;
			enableClientFields(); // open fields to edit
			return;
		}

		//

		try {
			int cusId = Integer.parseInt(cusIdStr);
			// שולח בקשה לשרת. התשובה תגיע ל-onMessageReceive
			userLogic.getSubscriberById(cusId);
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
					clientName = verifiedSubscriber.getName();
					clientPhone = verifiedSubscriber.getPhoneNumber();
					clientEmail = verifiedSubscriber.getEmail();
				} else {
					clientName = clientNameField.getText().trim();
					clientPhone = phoneField.getText().trim();
					clientEmail = emailField.getText().trim();
				}

				Integer cusId = null;
				if (!subscriberIdField.getText().trim().isEmpty()) {
					cusId = Integer.parseInt(subscriberIdField.getText().trim());
				}

				int guests = Integer.parseInt(guestsField.getText().trim());
				// double price = priceField.getText().trim().isEmpty() ? 0.0
				// : Double.parseDouble(priceField.getText().trim());

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
				// if (!arrivalString.isEmpty()) {
				// if (!arrivalString.matches("\\d{2}:\\d{2}")) {
				// Alarm.showAlert("Time Error", "Arrival time must be HH:mm format.",
				// Alert.AlertType.ERROR);
				// return;
				// }
				// LocalTime arrivalTime = LocalTime.parse(arrivalString);
				// משתמשים באותו תאריך שנבחר ב-DatePicker עבור שעת ההגעה
				// arrivalDate =
				// Date.from(localDate.atTime(arrivalTime).atZone(ZoneId.systemDefault()).toInstant());
				// }
				OrderStatus status = statusComboBox.getValue();
				Customer customerForOrder;

				if (isSubscriberVerified && verifiedSubscriber != null) {
					customerForOrder = verifiedSubscriber;
				} else {
					customerForOrder = new Customer(null, // ID (יתקבל מהשרת או יישאר null בינתיים)
							null, clientName, clientPhone, clientEmail, CustomerType.REGULAR);

					if (cusId != null) {
						customerForOrder.setSubscriberCode(cusId);
					}
				}

				this.newOrder = new Order(0, orderDate, guests, 0, customerForOrder, 
																						
						null, new Date(), arrivalDate, null, 0, status);

				System.out.println("------------ " + cusId);

				if (cusId == null) {
					System.out.println("Creating new customer...");
					userLogic.createCustomer(customerForOrder);
				} else {
					System.out.println("ME HERE");
					newOrder.getCustomer().setCustomerId(cusId);
					orderLogic.createOrder(newOrder);
				}
				
			} catch (NumberFormatException e) {
				Alarm.showAlert("Input Error", "Guests and Price must be valid numbers.", Alert.AlertType.ERROR);
			} catch (Exception e) {
				Alarm.showAlert("Error", "An error occurred while saving.", Alert.AlertType.ERROR);
//				 e.printStackTrace();
			}
			
		});

	}


	@FXML
	private void handleCancel(ActionEvent event) {
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
		if (controller != null) {
			controller.initData(emp, clientUi, this.isManager);
		} else {
			System.err.println("Error: Could not load OrderUi_controllerr.");
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
				case CUSTOMER:
					handleCustomerResponse(res);
					break;
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

	private void handleCustomerResponse(Response res) {
		Customer cus = (Customer) res.getData();

		if (res.getAction() == ActionType.GET_BY_ID) {
			if (res.getStatus() == ResponseStatus.SUCCESS && cus != null) {
				this.verifiedSubscriber = cus;
				isSubscriberVerified = true;
				fillAndLockFields(cus);
				// כאן לא עוברים מסך - רק נותנים למשתמש לראות שהמנוי זוהה
			} else {
				handleInvalidSubscriber("Subscriber ID not found.");
			}
		} else if (res.getAction() == ActionType.REGISTER_CUSTOMER) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				// הלקוח נוצר בהצלחה, עכשיו אפשר ליצור את ההזמנה עבורו
				this.newOrder.getCustomer().setCustomerId(cus.getCustomerId());
				orderLogic.createOrder(newOrder);
				// שים לב: אין כאן loadScreen! מחכים לתשובה של ה-ORDER
			} else {
				Alarm.showAlert("Error", "Could not register new customer: " + res.getMessage_from_server(),
						Alert.AlertType.ERROR);
			}
		}
	}

	private void handleOrderResponse(Response res) {
		if (res.getAction() == ActionType.CREATE) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				Alarm.showAlert("Success", "Order placed successfully!", Alert.AlertType.INFORMATION);

				// רק כאן - אחרי שהכל הסתיים - עוברים מסך
				navigateToOrdersList();
			} else {
				Alarm.showAlert("Error", "Failed to place order: " + res.getMessage_from_server(),
						Alert.AlertType.ERROR);
			}
		}
	}

	private void navigateToOrdersList() {
		// שימוש באחד השדות כדי לקבל את ה-Scene, זה יותר בטוח מ-ActionEvent
		if (clientNameField.getScene() == null) {
			System.err.println("Error: Scene is null, cannot navigate.");
			return;
		}

		// טעינת המסך הבא
		// אם ה-loadScreen שלך דורש ActionEvent, ננסה להעביר את ה-currentEvent המקורי
		// אבל עדיף לעדכן את המעבר שישתמש ב-Node כלשהו:
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", currentEvent, clientUi);

		if (controller != null) {
			controller.initData(emp, clientUi, this.isManager);
		}
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

	private void fillAndLockFields(Customer cus) {
		clientNameField.setText(cus.getName());
		phoneField.setText(cus.getPhoneNumber());
		emailField.setText(cus.getEmail());

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