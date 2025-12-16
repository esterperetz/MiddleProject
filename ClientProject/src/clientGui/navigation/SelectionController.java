package clientGui.navigation;

import java.io.IOException;

import client.MessageListener;
import clientGui.BaseController;
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

public class SelectionController extends MainNavigator implements  MessageListener<Object>, BaseController{
	//private ClientUi clientUi;
 
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
        
        super.loadScreen("managerTeam/RestaurantLogin", event,clientUi); 
    }

    @FXML
    void pressSubscriber(ActionEvent event) {
        System.out.println("Navigating to Subscriber screen...");
        super.loadScreen("user/SubscriberLogin", event,clientUi);
    }

    @FXML
    void pressCasualCustomer(ActionEvent event) {
        System.out.println("Navigating to Casual Customer screen...");
        //MainNavigator.loadReservationScreen(false, "", "", "");
        super.loadScreen("user/SubscriberOption", event,clientUi);
    }
    
    /**
     * Initializes this controller with an existing ClientUi and server IP.
     * Registers this controller as a listener server.
     *
     * @param clientUi The client UI used for server communication.
     * 
     */
 

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
/*
	@Override
	public void setClientUi(ClientUi clientUi) {
		// TODO Auto-generated method stub
		this.clientUi = clientUi;
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
	*/
}