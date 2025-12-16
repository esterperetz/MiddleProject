package clientGui.reservation;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class ForgetCodeController extends MainNavigator implements  MessageListener<Object>, BaseController{

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    void recoverReservationCode(ActionEvent event) {
        String email = txtEmail.getText();
        String phone = txtPhone.getText();
        
        // כאן תוסיפי את הלוגיקה לשליחת הבקשה לשרת
        System.out.println("Recovering code for: " + email + ", " + phone);
        
        // בסיום הפעולה - סגירת החלון
        closePopup(event);
    }

    @FXML
    void closePopup(ActionEvent event) {
        // השגת ה-Stage (החלון) הנוכחי וסגירתו
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
    }

	@Override
	public void setClientUi(ClientUi clientUi) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
}