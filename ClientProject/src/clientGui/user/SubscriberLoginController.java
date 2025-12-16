package clientGui.user;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SubscriberLoginController implements  MessageListener<Object>, BaseController{
	private ClientUi client_ui;
	 @FXML
	    private TextField SubscriberCode;

	  

	    @FXML
	    void performLogin(ActionEvent event) {
	    	//
	        //String username = SubscriberCode.getText();
	        //System.out.println("Login attempt: " + username);
	        //MainNavigator.loadReservationScreen(true, "", "", ""); //add to get the subscriber details from DB
	        //MainNavigator.loadScene("user/SubscriberOption");
	        //System.out.println("asdasdas");
	        // כאן תוסיף את הלוגיקה
	        SubscriberOptionController controller = 
	                MainNavigator.loadScreen("user/SubscriberOption", client_ui);

	            // 2. הפעלת initData על הקונטרולר שקיבלנו
	            if (controller != null) {
	                controller.initData(client_ui,SubscriberOptionController.isSubscriber());
	            } else {
	                System.err.println("Error: Could not load ManagerOptionsController.");
	            }
	    }

	    @FXML
	    void goBack(ActionEvent event) {
	        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
	        MainNavigator.loadScene("navigation/SelectionScreen");
	    }

		@Override
		public void setClientUi(ClientUi clientUi) {
			// TODO Auto-generated method stub
			client_ui=clientUi;
		}

		@Override
		public void onMessageReceive(Object msg) {
			// TODO Auto-generated method stub
			
		}
}
