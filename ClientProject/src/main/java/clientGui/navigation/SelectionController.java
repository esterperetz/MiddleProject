package clientGui.navigation;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.managerTeam.RestaurantLoginController;
import clientGui.reservation.OrderUi_controller;
import clientGui.user.SubscriberLoginController;
import clientGui.user.SubscriberOptionController;
import clientLogic.OrderLogic;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the main Selection Screen.
 * Allows the user to choose their login type: Restaurant Representative,
 * Subscriber, or Casual Customer.
 */
public class SelectionController extends MainNavigator implements MessageListener<Object>, Initializable {

    @FXML
    private Button rep_of_the_res;

    @FXML
    private Button subscriber;

    @FXML
    private Button casual_customer;

    /**
     * Initializes the controller.
     * Sets up a close request handler to disconnect the client when the window is
     * closed.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (casual_customer.getScene() != null && casual_customer.getScene().getWindow() != null) {
                Stage stage = (Stage) casual_customer.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    clientUi.disconnectClient();

                });
            }
        });
    }

    /**
     * Navigates to the Restaurant Login screen.
     * 
     * @param event The action event
     */
    @FXML
    void pressRepresentorOfTheResturant(ActionEvent event) {
        System.out.println("Navigating to Restaurant Representative screen...");

        RestaurantLoginController controller = super.loadScreen("managerTeam/RestaurantLogin", event, clientUi);
        if (controller != null) {
            controller.initData(new Employee(), clientUi, null);
        }
    }

    /**
     * Navigates to the Subscriber Login screen.
     * 
     * @param event The action event
     */
    @FXML
    void pressSubscriber(ActionEvent event) {
        System.out.println("Navigating to Subscriber screen...");
        SubscriberLoginController controller = super.loadScreen("user/SubscriberLogin", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, CustomerType.SUBSCRIBER, 0, new Customer());
        }
    }

    /**
     * Navigates to the Casual Customer options screen.
     * 
     * @param event The action event
     */
    @FXML
    void pressCasualCustomer(ActionEvent event) {
        System.out.println("Navigating to Casual Customer screen...");
        SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, CustomerType.REGULAR, 0, new Customer());
        }
    }

    /**
     * Handles incoming messages from the server.
     * Log disconnect messages.
     */
    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof String) {
                String message = (String) msg;
                if (message.contains("Disconnecting")) {
                    System.out.println("Server connection lost");
                    // Can add disconnection handling here if needed
                }
            }
        });
    }
}