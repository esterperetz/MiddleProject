package clientGui.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import Entities.Response;
import Entities.Subscriber;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator; // ודא שיש לך את ה-Import הזה
import clientLogic.UserLogic;

public class RegisterSubscriberController extends MainNavigator implements MessageListener<Object>{
	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private Label lblMessage;

	private UserLogic UserLogic;

	/**
	 * Handles the registration process when "Register Now" is clicked.
	 */
	@FXML
	void handleRegisterBtn(ActionEvent event) {
		// 1. Get data from fields
		String username = txtUsername.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();

		// 2. Validate Input (Basic checks)
		if (username.isEmpty() || phone.isEmpty() || email.isEmpty()) {
			lblMessage.setText("Error: All fields are required!");
			lblMessage.setStyle("-fx-text-fill: #ff6b6b;"); // Red color
			return;
		}

		if (!email.contains("@")) {
			lblMessage.setText("Error: Invalid email format.");
			lblMessage.setStyle("-fx-text-fill: #ff6b6b;");
			return;
		}
		try {
//		clientUi.addListener(this);//MUST DO NOT FORGER
		UserLogic user = new UserLogic(clientUi);//MUST DO NOT FORGER
		user.registerSubscriber(new Subscriber(123456, username, phone, email));
		}catch(Exception e) {
			System.out.println("one ");
		}

	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
		if (msg instanceof Response) {
			Response res = (Response) msg;
			System.out.println(res.getStatus().getString());
		} else
			System.out.println("nothing works");
		}catch(Exception e) {
			System.out.println("two ");
		}

	}

	/**
	 * Navigates back to the previous menu.
	 */
	@FXML
	void handleBackBtn(ActionEvent event) {
		// Change "manager/ManagerOptions" to wherever you want to go back to
		// MainNavigator.loadScene("managerTeam/workerOption");
			ManagerOptionsController controller = super.loadScreen("managerTeam/workerOption", event, clientUi);
			if (controller != null) {
				controller.initData(clientUi, ManagerOptionsController.isManager());
			} else {
				System.err.println("Error: Could not load ManagerOptionsController.");
			}
	}

	/**
	 * Clears the input fields after successful registration.
	 */
	private void clearFields() {
		txtUsername.clear();
		txtPhone.clear();
		txtEmail.clear();
	}


}
