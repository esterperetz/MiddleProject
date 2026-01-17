package clientGui.reservation;

import java.util.List;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.TableLogic;
import clientLogic.OrderLogic; // Assuming you have this
import entities.ActionType;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Order;
import entities.Response;
import entities.Response.ResponseStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for retrieving a table using a confirmation code.
 * Also displays active orders for subscribers.
 */
public class GetTableController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField txtConformationCode;
	@FXML
	private Label lblResult;
	@FXML
	private Button btnLostCode;

	// New UI Elements
	@FXML
	private VBox ordersContainer;
	@FXML
	private Label lblYourOrders;

	private Integer subscriberCode;
	private TableLogic tableLogic;
	private OrderLogic orderLogic; // New logic for fetching orders
	private CustomerType isSubscriber;
	private Customer customer;

	/**
	 * Initializes the controller with customer and session data.
	 * 
	 * @param clientUi           The client UI instance
	 * @param isSubscriberStatus The customer type
	 * @param subCode            The subscriber code
	 * @param customer           The customer object
	 */
	public void initData(ClientUi clientUi, CustomerType isSubscriberStatus, Integer subCode, Customer customer) {
		this.clientUi = clientUi;
		this.isSubscriber = isSubscriberStatus;
		this.subscriberCode = subCode;
		this.customer = customer;

		this.tableLogic = new TableLogic(clientUi);
		this.orderLogic = new OrderLogic(clientUi); // Initialize OrderLogic

		System.out.println("Loaded options for subscriber: " + subCode);

		// Logic: Show/Hide list based on subscriber status
		if (isSubscriber == CustomerType.SUBSCRIBER && customer != null) {
			lblYourOrders.setVisible(true);
			ordersContainer.setVisible(true);
			loadSubscriberOrders();
		} else {
			// Hide the bottom part if not a subscriber
			lblYourOrders.setVisible(false);
			ordersContainer.setVisible(false);
			lblYourOrders.setText("Please enter code manually");
		}
	}

	/**
	 * Loads active orders for the subscriber.
	 */
	private void loadSubscriberOrders() {
		Order reqOrder = new Order();
		reqOrder.setCustomer(customer);
		orderLogic.getSubscriberOrders(subscriberCode);
	}

	/**
	 * Handles the "Get Table" button click.
	 * Validates the confirmation code and attempts to retrieve the table.
	 * 
	 * @param event The action event
	 */
	@FXML
	void checkTableAvailability(ActionEvent event) {
		processCodeCheck(txtConformationCode.getText());
	}

	/**
	 * Processes the table retrieval request using the provided confirmation code.
	 * 
	 * @param codeStr The confirmation code as a string
	 */
	private void processCodeCheck(String codeStr) {
		if (codeStr == null || codeStr.trim().isEmpty()) {
			lblResult.setText("Please enter a valid Order ID.");
			lblResult.setStyle("-fx-text-fill: #ff6b6b;");
			return;
		}
		try {
			int code = Integer.parseInt(codeStr.trim());
			tableLogic.getTable(code, subscriberCode);
		} catch (NumberFormatException e) {
			lblResult.setText("Code must be numbers only.");
		}
	}

	/**
	 * Opens a popup to help the user retrieve a lost confirmation code.
	 * 
	 * @param event The action event
	 */
	@FXML
	void openLostCodePopup(ActionEvent event) {
		ForgetCodeController control = super.openPopup("reservation/ForgetCode", "Retrieve Code", clientUi);
		control.initData(clientUi, isSubscriber, subscriberCode, customer);
	}

	/**
	 * Navigates back to the Subscriber Option screen.
	 * 
	 * @param event The action event
	 */
	@FXML
	void goBack(ActionEvent event) {
		SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
		if (controller != null) {
			controller.initData(clientUi, isSubscriber, subscriberCode, customer);
		}
	}

	/**
	 * Handles messages from the server.
	 * Processes table retrieval responses and lists of orders.
	 */
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
					case ORDER: // Handle the list of orders coming back
						handleOrderResponse(res);
						break;
					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Handles the response containing a list of subscriber orders.
	 */
	private void handleOrderResponse(Response res) {
		// Assuming ActionType.GET_ORDERS_FOR_ENTRY or similar
		if (res.getStatus() == ResponseStatus.SUCCESS) {
			if (res.getAction() == ActionType.GET_USER_ORDERS) {
				if (res.getData() instanceof List) {
					List<Order> orders = (List<Order>) res.getData();
					ordersContainer.getChildren().clear(); // Clear previous

					if (orders.isEmpty()) {
						Label emptyLbl = new Label("No active reservations found for now.");
						emptyLbl.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
						ordersContainer.getChildren().add(emptyLbl);
						return;
					}

					for (Order o : orders) {
						Button orderBtn = createOrderButton(o);
						ordersContainer.getChildren().add(orderBtn);
					}
				}
			}
		}
	}

	/**
	 * Handles the response for table retrieval.
	 * Displays the table number if successful.
	 */
	private void handleTableResponse(Response res) {
		if (res.getAction() == ActionType.GET) {
			if (res.getStatus() == ResponseStatus.SUCCESS) {
				int tableNumber = ((int) res.getData());
				Alarm.showAlert("Welcome!", "Your table number is " + tableNumber, Alert.AlertType.INFORMATION);
				// Optionally navigate to next screen or clear selection
			} else {
				Alarm.showAlert("Failed", res.getMessage_from_server(), Alert.AlertType.ERROR);
			}
		}
	}

	/**
	 * Creates a button for each order in the list.
	 * Clicking the button automatically fills in the confirmation code.
	 * 
	 * @param o The order object
	 * @return The created button
	 */
	private Button createOrderButton(Order o) {
		// Design the button text
		String btnText = String.format("Time: %s | Guests: %d\nCode: %d", o.getOrderDate().toString().substring(11, 16), // Extract
																															// HH:MM
				o.getNumberOfGuests(), o.getConfirmationCode());

		Button btn = new Button(btnText);
		btn.setPrefWidth(280);
		btn.setPrefHeight(60);
		btn.setStyle(
				"-fx-background-color: #383838; -fx-text-fill: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-alignment: LEFT; -fx-font-size: 16px;");
		btn.getStyleClass().add("order-card-button");

		btn.setOnMouseEntered(evt -> btn.setStyle(
				"-fx-background-color: #555; -fx-text-fill: white; -fx-border-color: #F4C430; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-alignment: LEFT; -fx-font-size: 16px;"));
		btn.setOnMouseExited(evt -> btn.setStyle(
				"-fx-background-color: #383838; -fx-text-fill: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-alignment: LEFT; -fx-font-size: 16px;"));

		btn.setOnAction(e -> {
			processCodeCheck(String.valueOf(o.getConfirmationCode()));
		});

		return btn;
	}

}