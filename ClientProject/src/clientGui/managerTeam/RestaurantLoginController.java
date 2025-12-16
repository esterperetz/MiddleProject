package clientGui.managerTeam;

import Entities.User;
import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;

// <--- השינוי החשוב

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RestaurantLoginController implements  MessageListener<Object>, BaseController{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    private ClientUi clientUi;
    
   
    @FXML
    void performLogin(ActionEvent event) {
    	//ליאל עידו צריך לדבר איתך!!!!!!!!!!!!!!!!!!!!
    	    String username = usernameField.getText();
    	    String password = passwordField.getText();
    	    
    	    User user = new User(username, Integer.parseInt(password));
    	    // בדיקת תקינות בסיסית (לוגיקה שלך)
    	    System.out.println("Login attempt: " + username);
    	    MainNavigator.loadScreen("managerTeam/workerOption",clientUi);
    	    
    	    /*
    	    try {
    	    //moved to MainNavigation (the name of the function:loadOrderTableScreen)
    	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/OrderUi.fxml"));
    	        Parent root = loader.load();

    	        clientGui.reservation.OrderUi_controller controller = loader.getController();

    	         
    	        controller.initData(ClientUi.getInstance(), "localhost"); 

    	        :
    	        Scene scene = new Scene(root); 
    	        Stage stage = (Stage) usernameField.getScene().getWindow();
    	        stage.setScene(scene);
    	        stage.show();

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        System.out.println("Error loading OrderUi: " + e.getMessage());
    	    }
    	    */
    	
        // כאן תוסיף את הלוגיקה
    }

    @FXML
    void goBack(ActionEvent event) {
        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
        MainNavigator.loadScreen("navigation/SelectionScreen" ,clientUi);
    }
	@Override
	public void setClientUi(ClientUi clientUi) {
		// TODO Auto-generated method stub
		this.clientUi = clientUi;
	}
	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
}
