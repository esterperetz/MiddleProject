package clientGui.reservation;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class ForgetCodeController extends MainNavigator implements MessageListener<Object> {

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    private ClientUi clientUi;

    @FXML
    void recoverReservationCode(ActionEvent event) {
        String email = txtEmail.getText();
        String phone = txtPhone.getText();

        String contact = "";
        if (email != null && !email.trim().isEmpty()) {
            contact = email.trim();
        } else if (phone != null && !phone.trim().isEmpty()) {
            contact = phone.trim();
        } else {
            showError("Missing Input", "Please enter Email or Phone number.");
            return;
        }

        if (clientUi != null) {
            entities.Request req = new entities.Request(
                    entities.ResourceType.ORDER,
                    entities.ActionType.RESEND_CONFIRMATION,
                    null,
                    contact);

            clientUi.addListener(this);
            clientUi.sendRequest(req);
        } else {
            System.err.println("Error: ClientUi is null in ForgetCodeController");
        }
    }

    @FXML
    void closePopup(ActionEvent event) {
        if (clientUi != null)
            clientUi.getInstance().removeAllListeners();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    public void setClientUi(ClientUi clientUi) {
        this.clientUi = clientUi;
    }

    @Override
    public void onMessageReceive(Object msg) {
        javafx.application.Platform.runLater(() -> {
            if (msg instanceof entities.Response) {
                entities.Response res = (entities.Response) msg;
                if (res.getAction() == entities.ActionType.RESEND_CONFIRMATION) {
                    if (res.getStatus() == entities.Response.ResponseStatus.SUCCESS) {
                        showInfo("Success", res.getMessage_from_server());

                        Stage stage = (Stage) txtEmail.getScene().getWindow();
                        if (clientUi != null)
                            clientUi.getInstance().removeAllListeners();
                        stage.close();
                    } else {
                        showError("Failed", res.getMessage_from_server());
                    }
                }
            }
        });
    }

    private void showInfo(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}