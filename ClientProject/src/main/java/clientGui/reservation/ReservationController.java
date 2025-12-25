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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import client.MessageListener;
import clientLogic.OrderLogic;
import entities.ActionType;
import entities.Order;
import entities.Response;
import entities.ResourceType;
import javafx.application.Platform;

public class ReservationController extends MainNavigator implements MessageListener<Object> {
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private TextField dinersField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private boolean isSubscriber;
    private int subscriberId;
    private OrderLogic orderLogic;
    private ActionEvent currentEvent;

    @FXML
    public void initialize() {
        setupDateConstraints();
        datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                updateTimeSlots(newDate);
            }
        });
        phoneField.setOnMouseClicked(e -> errorLabel.setText(""));
    }

    public void initData(ClientUi clientUi, boolean isSubscriberStatus, int subId) {
        this.clientUi = clientUi;
        this.isSubscriber = isSubscriberStatus;
        this.subscriberId = subId;
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
        if (datePicker.getValue() == null || timeComboBox.getValue() == null || dinersField.getText().isEmpty()) {
            errorLabel.setText("Please fill Date, Time and Diners.");
            return;
        }

        if (!isSubscriber) {
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

            Order newOrder = new Order(
                0,
                orderDate,
                guests,
                0, 
                isSubscriber ? subscriberId : null,
                null, 
                new Date(),
                nameField.getText(), 
                emailField.getText(), 
                phoneField.getText(),
                null, 
                null, 
                0.0, 
                Order.OrderStatus.APPROVED
            );

            if (orderLogic != null) {
                orderLogic.createOrder(newOrder); 
            }
        } catch (Exception e) {
            errorLabel.setText("Invalid input: check Diners number.");
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, isSubscriber, subscriberId);
        }
    }

    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                if (res.getResource() == ResourceType.ORDER && res.getAction() == ActionType.CREATE) {
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