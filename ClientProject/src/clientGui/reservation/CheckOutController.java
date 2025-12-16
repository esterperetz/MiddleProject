package clientGui.reservation;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CheckOutController implements  MessageListener<Object>, BaseController{

	@FXML
	private TextField txtOrderId;

	@FXML
	private Label lblResult;

	private ClientUi clientUi;

	/**
	 * Triggered when the "Check out" button is clicked.
	 */
	@FXML
	void getPaymentBil(ActionEvent event) {
		String orderId = txtOrderId.getText();

		// 1. Input Validation
		// add if the code incorrect
		if (orderId == null || orderId.trim().isEmpty()) {
			lblResult.setText("Please enter a valid Order ID.");
			lblResult.setStyle("-fx-text-fill: #ff6b6b;"); // Red color for error
			return;
		}
		MainNavigator.loadScreen("reservation/Bill", clientUi);


		// 2. Server Simulation (Replace this with real server call later)
		// Example: int tableNumber = ClientUI.chat.getTableRequest(orderId);
		// int tableNumber = mockServerCheck(orderId);

		// 3. Process Result
		// if (tableNumber != -1) {
		// Success: Table is free
		// lblResult.setText("Table is ready! Table Number: " + tableNumber);
		// lblResult.setStyle("-fx-text-fill: #51cf66; -fx-font-size: 16px;
		// -fx-font-weight: bold;"); // Green color
		// } else {
		// Failure: Order not found or table occupied
		// lblResult.setText("Order not found or table is not ready yet.");
		// lblResult.setStyle("-fx-text-fill: #ff6b6b;"); // Red color
		// }

	}



	/**
	 * Triggered when the "Back" button is clicked. Navigates back to the main
	 * reservation menu.
	 */
	@FXML
	void goBack(ActionEvent event) {
		//MainNavigator.loadScreen("user/SubscriberOption", clientUi);
		SubscriberOptionController controller = 
    	        MainNavigator.loadScreen("user/SubscriberOption", clientUi);
    	if (controller != null) {
            controller.initData(clientUi,SubscriberOptionController.isSubscriber());
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
	}



	@Override
	public void setClientUi(ClientUi clientUi) {
		// TODO Auto-generated method stub
		this.clientUi = clientUi;
	}



	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * A temporary mock function to simulate server response.
	 * 
	 * @param id The Order ID
	 * @return Table number if found/ready, or -1 if not found.
	 * 
	 *         private int mockServerCheck(String id) { // Simulation logic if
	 *         (id.equals("100")) return 5; if (id.equals("123")) return 10; return
	 *         -1; // Not found }
	 */
}
