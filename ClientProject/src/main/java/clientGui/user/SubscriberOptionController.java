package clientGui.user;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.CheckOutController;
import clientGui.reservation.GetTableController;
import clientGui.reservation.ReservationController;
import entities.Customer;
import entities.CustomerType;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;

/**
 * Controller for the Subscriber Options menu.
 * Displays available actions for subscribers and guests.
 */
public class SubscriberOptionController extends MainNavigator implements Initializable, MessageListener<Object> {

	private CustomerType isSubscriber;

	@FXML
	private Button btnSubscriberSpecial;

	@FXML
	private Label lblCustomerName;

	@FXML
	private Button btnEditProfile;
	private Integer subId;
	private int tableId;
	private Customer customer;

	/**
	 * Initializes the controller.
	 * Sets up a listener to disconnect the client when the window is closed.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			if (btnSubscriberSpecial.getScene() != null && btnSubscriberSpecial.getScene().getWindow() != null) {
				Stage stage = (Stage) btnSubscriberSpecial.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();
				});
			}
		});
	}

	/**
	 * Initializes the controller with customer and session data.
	 * Displays specific options if the user is a subscriber.
	 * 
	 * @param clientUi       The client UI instance
	 * @param CustomerStatus The customer type
	 * @param subId          The subscriber ID
	 * @param customer       The customer object
	 */
	public void initData(ClientUi clientUi, CustomerType CustomerStatus, Integer subId, Customer customer) {
		this.clientUi = clientUi;
		this.isSubscriber = CustomerStatus;
		this.subId = subId;
		this.customer = customer;
		if (this.customer != null) {
			lblCustomerName.setText(customer.getName());
		} else {
			lblCustomerName.setText("Guest");
		}

		if (isSubscriber == CustomerType.SUBSCRIBER) {
			btnSubscriberSpecial.setVisible(true);
			btnSubscriberSpecial.setManaged(true);

			btnEditProfile.setVisible(true);
			btnEditProfile.setManaged(true);

		} else {
			btnSubscriberSpecial.setVisible(false);
			btnSubscriberSpecial.setManaged(false);

			btnEditProfile.setVisible(false);
			btnEditProfile.setManaged(false);
		}
	}

	/**
	 * Navigates to the Update Profile screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void goToUpdateProfile(ActionEvent event) {
		System.out.println("Navigating to update profile...");
		UpdateProfileController controller = super.loadScreen("user/UpdateProfile", event, clientUi);
		if (controller != null) {
			controller.initData(clientUi, customer);
		}
	}

	/**
	 * Navigates back to the previous screen (Subscriber Login or Selection Screen).
	 * 
	 * @param event The action event
	 */
	@FXML
	void goBackBtn(ActionEvent event) {
		if (isSubscriber == CustomerType.SUBSCRIBER) {
			super.loadScreen("user/SubscriberLogin", event, clientUi);
		} else {
			super.loadScreen("navigation/SelectionScreen", event, clientUi);
		}
	}

	/**
	 * Navigates to the Reservation screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void goToReservationBtn(ActionEvent event) {
		ReservationController controller = super.loadScreen("reservation/ReservationScreen", event, clientUi);

		if (controller != null)
			controller.initData(clientUi, this.isSubscriber, subId, customer);
		else
			System.out.println("Error: moving screen ReservationController");
	}

	/**
	 * Navigates to the Table Receipt/Get Table screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void goToSeatTableBtn(ActionEvent event) {
		GetTableController getTableController = super.loadScreen("reservation/RecieveTable", event, clientUi);
		if (getTableController != null)
			getTableController.initData(clientUi, this.isSubscriber, subId, customer);
		else
			System.out.println("Error: moving to GetTableController");
	}

	/**
	 * Navigates to the Subscriber History screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void subscriberActionBtn(ActionEvent event) {
		System.out.println("Subscriber specific action executed.");
		SubscriberHistoryController subHistoryController = super.loadScreen("user/SubscriberHistory", event, clientUi);
		if (subHistoryController != null) {
			subHistoryController.initData(subId, this.isSubscriber, null, customer);
		}
	}

	/**
	 * Navigates to the Check Out screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void CheckOutActionBtn(ActionEvent event) {
		CheckOutController checkOutController = super.loadScreen("reservation/CheckOutScreen", event, clientUi);
		if (checkOutController != null) {
			checkOutController.initData(subId, this.isSubscriber, tableId, customer);
		}
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
	}
}