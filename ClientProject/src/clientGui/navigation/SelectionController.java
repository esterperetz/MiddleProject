package clientGui.navigation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SelectionController {

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
}