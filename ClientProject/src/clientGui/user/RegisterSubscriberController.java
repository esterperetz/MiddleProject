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
import clientGui.navigation.BaseController;
import clientGui.navigation.MainNavigator; // ודא שיש לך את ה-Import הזה
import clientLogic.UserLogic;

public class RegisterSubscriberController extends MainNavigator implements MessageListener<Object>, BaseController {
	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private Label lblMessage;

	private UserLogic UserLogic;
	private boolean work = true;

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
		clientUi.addListener(this);
		UserLogic user = new UserLogic(clientUi);
		user.registerSubscriber(new Subscriber(123456, username, phone, email));
		}catch(Exception e) {
			System.out.println("one ");
		}
		// 3. Send to Server (Simulation)
		// TODO: specific logic to send data to server (e.g.,
		// ClientUI.chat.registerSubscriber(...))
//        boolean success = mockServerRegistration(username, phone, email);

	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
		if (msg instanceof Response) {
			Response res = (Response) msg;
			work = false;
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
		if (!work) {
			ManagerOptionsController controller = super.loadScreen("managerTeam/workerOption", event, clientUi);
			if (controller != null) {
				controller.initData(clientUi, ManagerOptionsController.isManager());
			} else {
				System.err.println("Error: Could not load ManagerOptionsController.");
			}
		}else {
			System.out.println("back does not work");
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
