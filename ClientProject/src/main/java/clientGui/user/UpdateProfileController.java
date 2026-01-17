package clientGui.user;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.UserLogic;
import entities.Customer;
import entities.CustomerType;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import entities.Alarm;

/**
 * Controller for the Update Profile screen.
 * Allows subscribers to update their personal information.
 */
public class UpdateProfileController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField txtFirstName;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	private Customer customer;

	private UserLogic userLogic;
	private ActionEvent currentEvent;

	/**
	 * Initializes the controller with the customer's data.
	 * 
	 * @param clientUi The client UI instance
	 * @param customer The customer object to be updated
	 */
	public void initData(ClientUi clientUi, Customer customer) {
		this.clientUi = clientUi;
		this.customer = customer;
		this.userLogic = new UserLogic(clientUi);

		if (customer != null) {
			txtFirstName.setText(customer.getName());
			txtPhone.setText(customer.getPhoneNumber());
			txtEmail.setText(customer.getEmail());
		}
	}

	/**
	 * Saves the changes made to the profile.
	 * Validates input and sends an update request to the server.
	 * 
	 * @param event The action event
	 */
	@FXML
	void saveChanges(ActionEvent event) {
		String fName = txtFirstName.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();

		if (fName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
			Alarm.showAlert("Validation Error", "All fields are required.", Alert.AlertType.WARNING);
			return;
		}

		customer.setName(fName);
		customer.setPhoneNumber(phone);
		customer.setEmail(email);

		currentEvent = event;
		userLogic.updateSubscriber(customer);
	}

	/**
	 * Navigates back to the Subscriber Option screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void goBack(ActionEvent event) {
		SubscriberOptionController controller = (SubscriberOptionController) super.loadScreen("user/SubscriberOption",
				event, clientUi);

		if (controller != null) {
			controller.initData(clientUi, CustomerType.SUBSCRIBER, customer.getSubscriberCode(), customer);
		}
	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
			if (msg instanceof Response) {
				Response res = (Response) msg;
				boolean isSuccess = res.getStatus().name().equals("SUCCESS");

				Platform.runLater(() -> {
					if (isSuccess) {
						if (res.getData() instanceof Customer) {
							customer = (Customer) res.getData();
						}
						Alarm.showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
						goBack(currentEvent);
					} else {

						Alarm.showAlert("Update Failed", res.getMessage_from_server(), Alert.AlertType.ERROR);
					}
				});

			}
		} catch (Exception e) {
			System.out.println("Error in updateController");
			e.printStackTrace();
		}
	}
}