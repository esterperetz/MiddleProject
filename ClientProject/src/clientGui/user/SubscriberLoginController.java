package clientGui.user;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SubscriberLoginController extends MainNavigator implements  MessageListener<Object>, BaseController{
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
	                super.loadScreen("user/SubscriberOption", event,clientUi);

	            // 2. הפעלת initData על הקונטרולר שקיבלנו
	            if (controller != null) {
	                controller.initData(clientUi,SubscriberOptionController.isSubscriber());
	            } else {
	                System.err.println("Error: Could not load ManagerOptionsController.");
	            }
	    }

	    @FXML
	    void goBack(ActionEvent event) {
	        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
	        super.loadScreen("navigation/SelectionScreen",event,clientUi);
	    }

		

		@Override
		public void onMessageReceive(Object msg) {
			// TODO Auto-generated method stub
			
		}
}
