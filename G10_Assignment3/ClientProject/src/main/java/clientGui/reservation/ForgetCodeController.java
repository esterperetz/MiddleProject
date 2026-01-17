package clientGui.reservation;

import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import clientLogic.TableLogic;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

/**
 * Controller for the "Forgot Confirmation Code" functionality.
 * Allows users to recover their reservation code via email or phone number.
 */
public class ForgetCodeController extends MainNavigator implements MessageListener<Object> {

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    private ClientUi clientUi;

    private CustomerType isSubscriber;

    private Integer subscriberCode;

    private TableLogic tableLogic;

    private Customer customer;
    private OrderLogic orderLogic;

    /**
     * Initializes the controller with necessary data.
     * 
     * @param clientUi           The client UI instance
     * @param isSubscriberStatus The customer type
     * @param subCode            The subscriber code
     * @param customer           The customer object
     */
    public void initData(ClientUi clientUi, CustomerType isSubscriberStatus, Integer subCode, Customer customer) {
        this.clientUi = clientUi;
        this.isSubscriber = isSubscriberStatus;
        this.subscriberCode = subCode;
        this.tableLogic = new TableLogic(clientUi);
        this.orderLogic = new OrderLogic(clientUi);
        this.customer = customer;

        // this.cusId = cusId;
        if (subCode != 0)
            System.out.println("Loaded options for subscriber: " + subCode);
    }

    // need to think what to do if customer have more than 1 reservation and wants
    // to select which one to recover.
    /**
     * Handles the "Recover" button click.
     * Validates input (Email/Phone) and sends a request to recover the reservation
     * code.
     * 
     * @param event The action event
     */
    @FXML
    void recoverReservationCode(ActionEvent event) {
        String email = txtEmail.getText();
        String phone = txtPhone.getText();

        String contact = "";
        try {
            if (email != null && !email.trim().isEmpty()) {
                contact = email.trim();
            } else if (phone != null && !phone.trim().isEmpty()) {
                contact = phone.trim();
            } else {
                Alarm.showAlert("Missing Input", "Please enter Email or Phone number.", Alert.AlertType.ERROR);
                return;
            }

            orderLogic.getConfirmationCodeByContact(contact);
        } catch (Exception e) {
            Alarm.showAlert("Invalid input!", "Please enter only numbers in phone number field.",
                    Alert.AlertType.ERROR);
        }

    }

    /**
     * Closes the popup window.
     * 
     * @param event The action event
     */
    @FXML
    void closePopup(ActionEvent event) {

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // @Override
    // public void setClientUi(ClientUi clientUi) {
    // this.clientUi = clientUi;
    // }

    /**
     * Handles server responses for the recovery request.
     * Displays a success or failure alert.
     */
    @Override
    public void onMessageReceive(Object msg) {
        javafx.application.Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                if (res.getAction() == entities.ActionType.RESEND_CONFIRMATION) {
                    if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                        Alarm.showAlert("Success", res.getMessage_from_server(), Alert.AlertType.INFORMATION);

                        Stage stage = (Stage) txtEmail.getScene().getWindow();

                        stage.close();
                    } else {
                        Alarm.showAlert("Failed", res.getMessage_from_server(), Alert.AlertType.ERROR);
                    }
                }
            }
        });
    }

}