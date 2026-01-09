package clientGui.navigation;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.reservation.OrderUi_controller;
import clientGui.user.SubscriberLoginController;
import clientGui.user.SubscriberOptionController;
import clientLogic.OrderLogic;
import entities.Customer;
import entities.CustomerType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SelectionController extends MainNavigator implements  MessageListener<Object>,Initializable{
	//private ClientUi clientUi;
 
    @FXML
    private Button rep_of_the_res;

    @FXML
    private Button subscriber;

    @FXML
    private Button casual_customer;
	
	
    
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
    
    @FXML
    void pressRepresentorOfTheResturant(ActionEvent event) {
        System.out.println("Navigating to Restaurant Representative screen...");
        // כאן אתה מעביר לשם של קובץ ה-FXML הבא (למשל "ResturantScreen")
        
        super.loadScreen("managerTeam/RestaurantLogin", event,clientUi); 
    }

    @FXML
    void pressSubscriber(ActionEvent event) {
        System.out.println("Navigating to Subscriber screen...");
        SubscriberLoginController controller = super.loadScreen("user/SubscriberLogin", event,clientUi);
        if (controller != null) {
            controller.initData(clientUi, CustomerType.SUBSCRIBER, 0,new Customer());
        }
    }

    @FXML
    void pressCasualCustomer(ActionEvent event) {
        System.out.println("Navigating to Casual Customer screen...");
        SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, CustomerType.REGULAR, 0,new Customer());
        }
        //super.loadScreen("user/SubscriberOption", event,clientUi);
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