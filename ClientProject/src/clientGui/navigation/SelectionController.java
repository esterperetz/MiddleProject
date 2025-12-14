package clientGui.navigation;

import java.io.IOException;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.reservation.OrderUi_controller;
import clientLogic.OrderLogic;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SelectionController implements  MessageListener<Object> {
	private ClientUi clientUi;
    private String ip;
    @FXML
    private Button rep_of_the_res;

    @FXML
    private Button subscriber;

    @FXML
    private Button casual_customer;
	
	

    @FXML
    void pressRepresentorOfTheResturant(ActionEvent event) {
        System.out.println("Navigating to Restaurant Representative screen...");
        // כאן אתה מעביר לשם של קובץ ה-FXML הבא (למשל "ResturantScreen")
        MainNavigator.loadScene("managerTeam/RestaurantLogin"); 
    }

    @FXML
    void pressSubscriber(ActionEvent event) {
        System.out.println("Navigating to Subscriber screen...");
        MainNavigator.loadScene("subscriber/SubscriberLogin");
    }

    @FXML
    void pressCasualCustomer(ActionEvent event) {
        System.out.println("Navigating to Casual Customer screen...");
        MainNavigator.loadReservationScreen(false, "", "", "");
    }
    
    /**
     * Initializes this controller with an existing ClientUi and server IP.
     * Registers this controller as a listener and loads all orders from the server.
     *
     * @param clientUi The client UI used for server communication.
     * @param ip       The server IP address.
     */
    public void initData(ClientUi clientUi, String ip) {
    	
    	this.clientUi = clientUi;
        this.ip = ip;
        
        // נרשמים להאזנה כדי לטפל במקרי ניתוק מהשרת (אם רוצים)
        clientUi.addListener(this);

        // שימוש בכפתור קיים כדי לקבל את החלון ולטפל בסגירה
        Platform.runLater(() -> {
            if (subscriber.getScene() != null) {
                Stage stage = (Stage) subscriber.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Closing client...");
                    clientUi.disconnectClient();
                    System.exit(0);
                });
            }
        });
    }

	@Override
	public void onMessageReceive(Object msg) {
		Platform.runLater(() -> {
            if (msg instanceof String) {
                String message = (String) msg;
                if (message.contains("Disconnecting")) {
                    System.out.println("Server connection lost");
                    // כאן אפשר להוסיף טיפול בניתוק אם צריך
                }
            }
        });
		
	}
}