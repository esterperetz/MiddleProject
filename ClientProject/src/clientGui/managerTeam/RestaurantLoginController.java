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

public class RestaurantLoginController extends MainNavigator implements MessageListener<Object>, BaseController {

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	void performLogin(ActionEvent event) {
		// ליאל עידו צריך לדבר איתך!!!!!!!!!!!!!!!!!!!!
		String username = usernameField.getText();
		String password = passwordField.getText();
		
		if (this.clientUi == null) {
			System.err.println("Error: No connection to server (clientUi is null). Cannot login.");
			return;
		}

		// TODO: לוגיקת בדיקת סיסמה מול השרת (שלח בקשה לשרת)
		// User user = new User(username, Integer.parseInt(password));
		System.out.println("Login attempt for: " + username);

		// --- מעבר מסך ---

		// 1. שימוש בפונקציה הגנרית וקבלת הקונטרולר
		ManagerOptionsController controller = super.loadScreen("managerTeam/workerOption", event,clientUi);

		// 2. אתחול הנתונים במסך החדש
		if (controller != null) {
			controller.initData(clientUi,ManagerOptionsController.isManager());
		} else {
			System.err.println("Failed to load ManagerOptionsController. Check FXML path.");
		}
		/*
		 * try { //moved to MainNavigation (the name of the
		 * function:loadOrderTableScreen) FXMLLoader loader = new
		 * FXMLLoader(getClass().getResource("/clientGui/reservation/OrderUi.fxml"));
		 * Parent root = loader.load();
		 * 
		 * clientGui.reservation.OrderUi_controller controller = loader.getController();
		 * 
		 * 
		 * controller.initData(ClientUi.getInstance(), "localhost");
		 * 
		 * : Scene scene = new Scene(root); Stage stage = (Stage)
		 * usernameField.getScene().getWindow(); stage.setScene(scene); stage.show();
		 * 
		 * } catch (Exception e) { e.printStackTrace();
		 * System.out.println("Error loading OrderUi: " + e.getMessage()); }
		 */

		// כאן תוסיף את הלוגיקה
	}

	@FXML
	void goBack(ActionEvent event) {
		// וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
		super.loadScreen("navigation/SelectionScreen", event,clientUi);
	}

	
	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub

	}
}
