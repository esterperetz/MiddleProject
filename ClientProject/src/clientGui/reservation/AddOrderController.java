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

    @FXML private TextField fullNameField;
    @FXML private TextField idDetailsField;
    @FXML private TextField subscriberIdField;
    @FXML private TextField guestsField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField; // For HH:mm
    @FXML private TextField priceField;
    @FXML private ComboBox<OrderStatus> statusComboBox;

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
        if (idDetailsField.getText().trim().isEmpty() || 
            guestsField.getText().trim().isEmpty() || 
            datePicker.getValue() == null) {
            
            Alarm.showAlert("Missing Input", "Please fill in all mandatory fields (ID, Guests, Date).", Alert.AlertType.WARNING);
            return;
        }

        try {
            String fullName = fullNameField.getText();
            String idDetails = idDetailsField.getText();
            
            Integer subId = null;
            if (!subscriberIdField.getText().trim().isEmpty()) {
                subId = Integer.parseInt(subscriberIdField.getText().trim());
            }

            // המרת כמות אורחים ומחיר
            int guests = Integer.parseInt(guestsField.getText().trim());
            double price = priceField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(priceField.getText().trim());
            
            // 3. טיפול בתאריך ושעה
            LocalDate localDate = datePicker.getValue();
            String timeString = timeField.getText().trim();
            
            // בדיקה שפורמט השעה תקין (HH:mm)
            if (!timeString.matches("\\d{2}:\\d{2}")) {
                Alarm.showAlert("Time Error", "Please enter time in HH:mm format (e.g., 18:30)", Alert.AlertType.ERROR);
                return;
            }
            
            LocalTime localTime = LocalTime.parse(timeString); 
            // יצירת אובייקט Date סופי המשלב תאריך ושעה
            Date orderDate = Date.from(localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant());
            
            OrderStatus status = statusComboBox.getValue();

            // 4. יצירת האובייקט
            // שים לב: אנחנו שולחים 0 ב-order_number.
            // ה-MySQL יזהה שזה 0 וייתן אוטומטית את המספר הפנוי הבא (Auto Increment).
            Order newOrder = new Order(
                0,              // order_number (DB will assign this automatically)
                orderDate,      // order_date
                guests,         // number_of_guests
                0,              // confirmation_code (Server generates this)
                subId,          // subscriber_id
                new Date(),     // date_of_placing_order (Current time)
                idDetails,      // identification_details
                fullName,       // full_name
                price,          // total_price
                status          // status
            );

            // 5. send to server and go the screen that show the orders
            if (orderLogic != null) {
                orderLogic.createOrder(newOrder);
                OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
        		if (controller != null) {
        			controller.initData();
        		} else {
        			System.err.println("Error: Could not load OrderUi_controllerr.");
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
    //public void initData(ClientUi clientUi) {
    public void initData() {
        //this.clientUi = clientUi;
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

    private void closeWindow() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }
}