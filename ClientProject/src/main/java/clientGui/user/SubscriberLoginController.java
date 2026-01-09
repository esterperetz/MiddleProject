package clientGui.user;

import client.MessageListener;
import clientGui.navigation.MainNavigator;
import clientLogic.UserLogic;
import entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SubscriberLoginController extends MainNavigator implements MessageListener<Object> {

    @FXML
    private TextField SubscriberCode;
    private ActionEvent currentEvent;
    private int lastEnteredSubId;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (SubscriberCode.getScene() != null && SubscriberCode.getScene().getWindow() != null) {
                Stage stage = (Stage) SubscriberCode.getScene().getWindow();
                stage.setOnCloseRequest(event -> clientUi.disconnectClient());
            }
        });
    }

    @FXML
    void performLogin(ActionEvent event) {
        String code = SubscriberCode.getText().trim();
        if (code.isEmpty()) {
            Alarm.showAlert("Input Error", "Please enter a code", Alert.AlertType.WARNING);
            return;
        }
        try {
            this.currentEvent = event;
            this.lastEnteredSubId = Integer.parseInt(code);
            new UserLogic(clientUi).getSubscriberById(lastEnteredSubId);
        } catch (NumberFormatException e) {
            Alarm.showAlert("Format Error", "Code must be a number", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleForgotCode(ActionEvent event) {
        String id = SubscriberCode.getText().trim();
        if (id.isEmpty()) {
            Alarm.showAlert("Input Error", "Enter your ID to resend the code.", Alert.AlertType.WARNING);
            return;
        }
        Request req = new Request(ResourceType.CUSTOMER, ActionType.FORGOT_CODE, null, id);
        clientUi.sendRequest(req);
    }

    @Override
    public void onMessageReceive(Object msg) {
        if (msg instanceof Response) {
            Response res = (Response) msg;
            Platform.runLater(() -> {
                if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                    SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", currentEvent, clientUi);
                    if (controller != null) controller.initData(clientUi, CustomerType.SUBSCRIBER, lastEnteredSubId);
                } else {
                    Alarm.showAlert("Invalid Code", "Please enter a valid subscriber code.", Alert.AlertType.ERROR);
                }
            });
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        super.loadScreen("navigation/SelectionScreen", event, clientUi);
    }
}