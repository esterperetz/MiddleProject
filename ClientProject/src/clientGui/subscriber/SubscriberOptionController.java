package clientGui.subscriber;

import javafx.scene.control.Button;
//import java.util.ResourceBundle;

import clientGui.navigation.MainNavigator;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;


import java.net.URL;
import java.util.ResourceBundle;

public class SubscriberOptionController implements Initializable{
	private boolean isSubscriber;
	// Link to the special button in the FXML file
	@FXML
    private Button btnSubscriberSpecial;
	
	
	/**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // By default, the button is hidden in FXML (visible="false", managed="false").
        
        // --- Check if the current user is a subscriber ---
        // TODO: Replace 'true' with your actual logic, e.g., ClientUI.currentUser.isSubscriber()
        isSubscriber = true; 

        if (isSubscriber) {
            // If the user is a subscriber, reveal the button and let it take up space in the layout
            btnSubscriberSpecial.setVisible(true);
            btnSubscriberSpecial.setManaged(true);
        }
    }
	@FXML
	 void goBackBtn()
	{
		int flag=1;
		//when we succeed to log in, we dont go back to navigation screen
		if(flag==1)
		{
			//we need to check if the user is subscriber and if the code is correct
			MainNavigator.loadScene("subscriber/SubscriberLogin");
			flag=0;
		}
		else
	        MainNavigator.loadScene("navigation/SelectionScreen");

	}
	@FXML
	void goToReservationBtn()
	{
		MainNavigator.loadReservationScreen(true, "", "", "");
	}
	
	@FXML
	void goToSeatTableBtn()
	{
		MainNavigator.loadScene("reservation/RecieveTable");
	}
	/**
     * Action handler for the special subscriber-only button.
     */
    @FXML
    void subscriberActionBtn(ActionEvent event) {
        // Logic specific to subscribers goes here
        System.out.println("Subscriber specific action executed.");
    	MainNavigator.loadScene("subscriber/SubscriberHistory");

    }

    @FXML
    void CheckOutActionBtn(ActionEvent event) {
    	MainNavigator.loadScene("reservation/CheckOutScreen");
    }

}
