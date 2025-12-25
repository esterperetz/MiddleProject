package clientGui.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator; // ודא שיש לך את ה-Import הזה
import clientLogic.UserLogic;
import entities.ActionType;
import entities.Response;
import entities.Subscriber;
import javafx.application.Platform; // Added import for Platform

public class RegisterSubscriberController extends MainNavigator implements MessageListener<Object>{
	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private Label lblMessage;
	private boolean isManager;
	private UserLogic UserLogic;
	private ActionEvent currentEvent; // Added to save the event for async navigation

	/**
	 * Handles the registration process when "Register Now" is clicked.
	 */
	@FXML
	void handleRegisterBtn(ActionEvent event) {
		// 1. Get data from fields
		String username = txtUsername.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();
		
		this.currentEvent = event; // Save current event

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
		user.registerSubscriber(new Subscriber(0, username, phone, email)); //CHANGED FROM 123456 TO 0 (AUTO INC)
		} catch(Exception e) {
			System.out.println("one ");
		}

	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
		if (msg instanceof Response) {
			Response res = (Response) msg;
			System.out.println(res.getStatus().getString());
			
			// Handle successful registration and navigate to Subscriber Options
			if (res.getAction() == ActionType.REGISTER_SUBSCRIBER && res.getStatus() == Response.ResponseStatus.SUCCESS) {
				Platform.runLater(() -> {
					Subscriber newSub = (Subscriber) res.getData();
					SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", currentEvent, clientUi);
					if (controller != null) {
						controller.initData(clientUi, true, newSub.getSubscriberId());
					}
				});
			} else if (res.getStatus() == Response.ResponseStatus.ERROR) {
				Platform.runLater(() -> lblMessage.setText(res.getMessage_from_server()));
			}
		} else
			System.out.println("nothing works");
		} catch(Exception e) {
			System.out.println("two ");
		}

	}
	public void initData(ClientUi clientUi,boolean isManager)
	{
		this.clientUi=clientUi;
		this.isManager=isManager;
	}

	/**
	 * Navigates back to the previous menu.
	 */
	@FXML
	void handleBackBtn(ActionEvent event) {
		// Fixed: Navigate back to SelectionScreen instead of Manager Dashboard
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
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