package clientGui.user;

import Entities.Response;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.BaseController;
import clientGui.navigation.MainNavigator;
import clientLogic.UserLogic;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SubscriberLoginController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField SubscriberCode;
	private static ActionEvent event1;

	@FXML
	void performLogin(ActionEvent event) {
		String subscriber_Code = SubscriberCode.getText().trim();
		try {
			event1 = event;
			clientUi.addListener(this);
			UserLogic user = new UserLogic(clientUi);
			user.getSubscriberById(Integer.parseInt(subscriber_Code));// convert subscriber_Code to int
		} catch (Exception e) {
			System.out.println("one ");
		}
		
		//
		Platform.runLater(() -> {
			SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);

			if (controller != null) {
				controller.initData(clientUi, SubscriberOptionController.isSubscriber());
			} else {
				System.err.println("Error: Could not load ManagerOptionsController.");
			}
			System.exit(0);

		});
		System.out.println("fdgdfg");

	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
			if (msg instanceof Response) {

				Response res = (Response) msg;
				if (res.getStatus().getString().equals("SUCCESS")) {
					System.out.println(res.getStatus().getString());
					//// fix the navigation here

				}
			} else
				System.out.println("NOT_FOUND");
		} catch (Exception e) {
			System.out.println("two ");
		}
	}

	public void move() {

	}

	@FXML
	void goBack(ActionEvent event) {
		// וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

}
