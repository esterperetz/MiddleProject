package clientGui.user;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.UserLogic;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Subscriber Login screen.
 * Handles authentication of subscribers via their code.
 */
public class SubscriberLoginController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField SubscriberCode;
	private ActionEvent currentEvent;
	private int lastEnteredSubCode;
	private CustomerType isSubscriber;
	private Integer subId;
	private Customer customer;

	/**
	 * Initializes the controller.
	 * Sets up a listener to disconnect the client when the window is closed.
	 */
	@FXML
	public void initialize() {
		Platform.runLater(() -> {
			if (SubscriberCode.getScene() != null && SubscriberCode.getScene().getWindow() != null) {
				Stage stage = (Stage) SubscriberCode.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();

				});
			}
		});
	}

	/**
	 * Handles the login attempt.
	 * Validates the subscriber code and sends a request to the server.
	 * 
	 * @param event The action event
	 */
	@FXML
	void performLogin(ActionEvent event) {
		String subscriber_Code = SubscriberCode.getText().trim();

		if (subscriber_Code.isEmpty()) {
			Alarm.showAlert("Input Error", "Please enter a code", Alert.AlertType.WARNING);
			return;
		}

		try {
			this.currentEvent = event;
			this.lastEnteredSubCode = Integer.parseInt(subscriber_Code);
			UserLogic user = new UserLogic(clientUi);
			user.getSubscriberById(Integer.parseInt(subscriber_Code));

		} catch (NumberFormatException e) {
			Alarm.showAlert("Format Error", "Code must be a number", Alert.AlertType.ERROR);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onMessageReceive(Object msg) {
		Platform.runLater(() -> {
			try {
				if (msg instanceof Response) {
					Response res = (Response) msg;

					if (res.getResource() == entities.ResourceType.CUSTOMER) {

						switch (res.getAction()) {
							case GET_BY_ID:
								if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
									if (res.getData() instanceof Customer) {
										System.out.println(res.getStatus().name());
										this.customer = (Customer) res.getData();

										SubscriberOptionController controller = super.loadScreen(
												"user/SubscriberOption",
												currentEvent, clientUi);
										if (controller != null) {
											controller.initData(clientUi, CustomerType.SUBSCRIBER, lastEnteredSubCode,
													customer);
										}
									}
								} else {
									Alarm.showAlert("Invalid Subscriber code", "Please enter a valid code",
											Alert.AlertType.ERROR);
								}
								break;

							default:
								break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void move() {

	}

	/**
	 * Opens the barcode scanner simulation.
	 * 
	 * @param event The action event
	 */
	@FXML
	void openScanner(ActionEvent event) {
		BarcodeScannerController controller = super.loadScreen("user/BarcodeScanner", event, clientUi);

		if (controller != null) {
			controller.initData(clientUi, CustomerType.SUBSCRIBER, lastEnteredSubCode, customer);
		}
	}

	/**
	 * Navigates back to the Selection Screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void goBack(ActionEvent event) {
		// Verify that SelectionScreen.fxml is in the correct folder
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

	/**
	 * Initializes the controller with session data.
	 * 
	 * @param clientUi     The client UI instance
	 * @param isSubscriber The customer type
	 * @param subId        The subscriber ID
	 * @param customer     The customer object
	 */
	public void initData(ClientUi clientUi, CustomerType isSubscriber, Integer subId, Customer customer) {
		this.isSubscriber = isSubscriber;
		this.subId = subId;
		this.customer = customer;

	}

}
