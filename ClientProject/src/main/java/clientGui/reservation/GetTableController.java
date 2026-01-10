package clientGui.reservation;

import javafx.scene.control.Button;

import java.io.IOException;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.TableLogic;
import entities.ActionType;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Response;
import entities.Response.ResponseStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GetTableController extends MainNavigator implements MessageListener<Object> {
	@FXML
	private TextField txtConformationCode;

	@FXML
	private Label lblResult;
	@FXML
	private Button btnLostCode;
	
	private Integer subscriberCode;
	private TableLogic tableLogic;
	private CustomerType isSubscriber;

	private Customer customer;

//	private Integer cusId;

	public void initData(ClientUi clientUi, CustomerType isSubscriberStatus, Integer subCode,Customer customer) {
		this.clientUi = clientUi;
		this.isSubscriber = isSubscriberStatus;
		this.subscriberCode = subCode;
		this.tableLogic = new TableLogic(clientUi);
		this.customer = customer;
//		this.cusId = cusId;
		System.out.println("Loaded options for subscriber: " + subCode);
	}

	/**
	 * Triggered when the "Check Table" button is clicked.
	 */
	@FXML
	void checkTableAvailability(ActionEvent event) {
		String conformationCode = txtConformationCode.getText();

		// 1. Input Validation
		// add if the code incorrect
		if (conformationCode == null || conformationCode.trim().isEmpty()) {
			lblResult.setText("Please enter a valid Order ID.");
			lblResult.setStyle("-fx-text-fill: #ff6b6b;"); // Red color for error
			return;
		}
		try {
			
			tableLogic.getTable(Integer.parseInt(conformationCode),subscriberCode);
			System.out.println(" GOOD From check table : " + customer.getCustomerId());
		}catch(Exception e) {
			System.out.println(" ERROR From check table : " + customer);
		}
	}
	

	@FXML
	void openLostCodePopup(ActionEvent event) {
		
		ForgetCodeController control = super.openPopup("reservation/ForgetCode", "Retrieve Code", clientUi);
		control.initData(clientUi, isSubscriber, subscriberCode, customer);
	}

	/**
	 * Triggered when the "Back" button is clicked. Navigates back to the main
	 * reservation menu.
	 */
	@FXML
	void goBack(ActionEvent event) {
		// MainNavigator.loadScene("user/SubscriberOption");
		SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
		if (controller != null) {
			controller.initData(clientUi, isSubscriber, subscriberCode,customer);
		} else {
			System.err.println("Error: Could not load ManagerOptionsController.");
		}
	}

	@Override
	public void onMessageReceive(Object msg) {
		if (!(msg instanceof Response))
			return;
		Response res = (Response) msg;

		Platform.runLater(() -> {
			try {
				switch (res.getResource()) {
				case TABLE:
					handleTableResponse(res);
					break;

				default:
					System.out.println("Unhandled resource: " + res.getResource());

				}
			} catch (Exception e) {
				e.printStackTrace();
				Alarm.showAlert("System Error", "An error occurred while processing server response.",
						Alert.AlertType.ERROR);
			}
		});

	}

	private void handleTableResponse(Response res) {

		if (res.getAction() == ActionType.GET) {

			if (res.getStatus() == ResponseStatus.SUCCESS) {
				int tableNumber = ((int) res.getData());
				Alarm.showAlert("Success", "Your table number is " + tableNumber, Alert.AlertType.INFORMATION);
			}else
				Alarm.showAlert("faild", res.getMessage_from_server(), Alert.AlertType.ERROR);


		}
	}

}