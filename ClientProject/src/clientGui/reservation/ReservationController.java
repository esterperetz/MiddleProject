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
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import client.MessageListener;

public class ReservationController extends MainNavigator implements  MessageListener<Object>{
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private TextField dinersField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private boolean isSubscriber;
	private int subscriberId;

    @FXML
    public void initialize() {
        // 1. הגבלת התאריכים (היום עד עוד חודש)
        setupDateConstraints();

        // 2. האזנה לשינוי תאריך - ברגע שבוחרים תאריך, נחשב שעות
        datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                updateTimeSlots(newDate);
            }
        });
        
        // ניקוי הודעות שגיאה בעת הקלדה
        phoneField.setOnMouseClicked(e -> errorLabel.setText(""));
    }

    /**
     * פונקציה זו נקראת מבחוץ כדי לאתחל את המסך עם פרטי מנוי
     */
    
    public void initData(ClientUi clientUi, boolean isSubscriberStatus, int subId) {
    	this.clientUi = clientUi;
        this.isSubscriber = isSubscriberStatus;
        this.subscriberId = subId;
        System.out.println("Loaded options for subscriber: " + subId);
        //check what we do with the hidden button for non subscriber.
 

      
    }
//    public void setData(boolean isSubscriber, String phone, String email, String name) {
//        this.isSubscriber = isSubscriber;
//        if (isSubscriber) {
//            phoneField.setText(phone);
//            emailField.setText(email);
//            nameField.setText(name);
//            
//            // אפשר לחסום עריכה למנויים אם רוצים:
//            // phoneField.setDisable(true);
//        }
//    }

    private void setupDateConstraints() {
        // הגדרת "מפעל תאים" שמבטל ימים בעבר או ימים מעבר לחודש מהיום
        Callback<DatePicker, DateCell> dayCellFactory = picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                
                LocalDate today = LocalDate.now();
                LocalDate nextMonth = today.plusMonths(1);

                // ביטול אם זה בעבר או אם זה יותר מחודש קדימה
                if (item.isBefore(today) || item.isAfter(nextMonth)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); // צבע אדמדם
                }
            }
        };
        datePicker.setDayCellFactory(dayCellFactory);
        datePicker.setValue(LocalDate.now()); // ברירת מחדל להיום
    }

    private void updateTimeSlots(LocalDate selectedDate) {
        List<String> validSlots = new ArrayList<>();
        
        // שעות פעילות המסעדה (למשל 12:00 עד 22:00)
        LocalTime openTime = LocalTime.of(12, 0);
        LocalTime closeTime = LocalTime.of(22, 0);
        
        // הזמן הנוכחי + שעה (דרישה: "בין שעה אחת קדימה")
        LocalTime minOrderTime = LocalTime.now().plusHours(1);

        LocalTime slot = openTime;
        while (slot.isBefore(closeTime)) {
            // אם התאריך הוא היום, צריך לבדוק שהשעה גדולה מהמינימום
            // אם התאריך הוא עתידי, כל השעות פנויות
            if (selectedDate.isEqual(LocalDate.now())) {
                if (slot.isAfter(minOrderTime)) {
                    validSlots.add(slot.toString());
                }
            } else {
                validSlots.add(slot.toString());
            }
            
            // קפיצות של 30 דקות
            slot = slot.plusMinutes(30);
        }

        timeComboBox.setItems(FXCollections.observableArrayList(validSlots));
        if (validSlots.isEmpty()) {
            errorLabel.setText("No available slots for today (Closing soon).");
        }
    }

    @FXML
    void submitReservation(ActionEvent event) {
        // בדיקת שדות חובה
        if (datePicker.getValue() == null || timeComboBox.getValue() == null || dinersField.getText().isEmpty()) {
            errorLabel.setText("Please fill Date, Time and Diners.");
            return;
        }

        // לוגיקה ללקוח מזדמן: חייב למלא פרטים
        if (!isSubscriber) {
            if (phoneField.getText().isEmpty() || emailField.getText().isEmpty() || nameField.getText().isEmpty()) {
                errorLabel.setText("Guest must provide Name, Phone and Email.");
                return;
            }
        }

        // --- בדיקת מקום (סימולציה) ---
        boolean isTableAvailable = checkAvailabilityMock(datePicker.getValue(), timeComboBox.getValue());
        
        if (isTableAvailable) {
            System.out.println("Reservation Confirmed!");
            System.out.println("Details: " + nameField.getText() + ", " + datePicker.getValue() + " at " + timeComboBox.getValue());
            // כאן תשלח לשרת...
            MainNavigator.showAlert( "Success", "Table Booked successfully!",Alert.AlertType.INFORMATION);
            super.loadScreen("navigation/SelectionScreen",event,clientUi); // חזרה לראשי
        } else {
            // חלופה: הצגת שעות קרובות
            showAlternativeTimes();
        }
    }

    // פונקציית דמה לבדיקת זמינות (במציאות זה מול השרת)
    private boolean checkAvailabilityMock(LocalDate date, String time) {
        // סתם לצורך הדגמה: נניח ששעה 18:00 תמיד תפוסה
        return !time.equals("18:00"); 
    }

    private void showAlternativeTimes() {
    	MainNavigator.showAlert("The selected time is not available.","Alternative times found: 19:30, 20:00" ,Alert.AlertType.WARNING);
         // כאן תציג לוגיקה אמיתית
       
    }

    @FXML
    void goBack(ActionEvent event) {
        //MainNavigator.loadScene("user/SubscriberOption");
    	SubscriberOptionController controller = 
    	        super.loadScreen("user/SubscriberOption", event,clientUi);
    	if (controller != null) {
            controller.initData(clientUi,isSubscriber,subscriberId);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
    }

	

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
}