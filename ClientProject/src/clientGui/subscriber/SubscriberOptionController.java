package clientGui.subscriber;

import clientGui.navigation.MainNavigator;
import javafx.fxml.FXML;

public class SubscriberOptionController {
    //MainNavigator.loadReservationScreen(true, "", "", "");
	@FXML
	 void goBackBtn()
	{
		MainNavigator.loadScene("subscriber/SubscriberLogin");
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


}
