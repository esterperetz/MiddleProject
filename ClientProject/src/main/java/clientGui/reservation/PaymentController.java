package clientGui.reservation;

import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.OrderLogic;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Order;
import entities.Order.OrderStatus;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class PaymentController extends MainNavigator implements MessageListener<Object>,Initializable {

	@FXML
	private TextField txtCardNumber;
	@FXML
	private TextField txtID;
	@FXML
	private TextField txtExpiry;
	@FXML
	private TextField txtCVV;
	@FXML
	private Label lblError;

	// משתנה לשמירת הסכום לתשלום (אופציונלי, כדי להציג בלוג)
	private double amountToPay;
	private int tableId;
	private int subscriberId;
	private CustomerType isSubscriber;
	private Order order;
	private double totalPrice;
	private OrderLogic orderLogic;
	private Customer customer;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			if (txtCVV.getScene() != null && txtCVV.getScene().getWindow() != null) {
				Stage stage = (Stage) txtCVV.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();

				});
			}
		});
	}
	public void setPaymentDetails(double amount, int tableId) {
		this.amountToPay = amount;
		this.tableId = tableId;
	}

	public void initData(Order order,double originalTotal, int subId, CustomerType isSubscriber, int tableId,Customer customer) {
		this.tableId = tableId;
		this.subscriberId = subId;
		// itemsList.setItems(items);
		this.isSubscriber = isSubscriber;
		this.order = order;
		this.totalPrice = originalTotal;
		this.orderLogic = new OrderLogic(clientUi);
		this.customer = customer;
	}

	@FXML
	void processPayment(ActionEvent event) {
		lblError.setVisible(false);

		String cardNum = txtCardNumber.getText().trim();
		String id = txtID.getText().trim();
		String expiry = txtExpiry.getText().trim();
		String cvv = txtCVV.getText().trim();

		if (cardNum.isEmpty() || id.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
			showError("Please fill in all fields.");
			return;
		}

		if (cardNum.length() < 12) { 
			showError("Invalid Card Number.");
			return;
		}

		if (cvv.length() != 3) {
			showError("CVV must be 3 digits.");
			return;
		}

		System.out.println("Processing Credit Card Payment...");
		System.out.println("Card: " + cardNum + " | Amount: " + totalPrice);

		System.out.println("Payment Approved! Table " + tableId + " released.");
		
//		order.setOrderStatus(OrderStatus.PAID);
		orderLogic.updateOrderCheckOut(order);
		
		Alarm.showAlert("Payment Sucssesfully!","You paid "+totalPrice + " to Bistro, Thank you!", AlertType.INFORMATION);
//		System.out.println("Order closed at: " + order.getLeavingTime());
		
		// Alert pay good
		SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);

		// if(isSub)
		controller.initData(clientUi, isSubscriber, subscriberId,customer);
		// else
		// controller.initData(clientUi, false, subscriberId);

		// public void initData(ClientUi clientUi, boolean isSubscriberStatus, Integer
		// subId)
	}
	
	
	@FXML
	void cancel(ActionEvent event) {
		BillController billController = super.loadScreen("reservation/Bill", event, clientUi);
		// if(isSub) String orderId, Integer subscriberId, CustomerType customerType, int tableId
		billController.initData(order, subscriberId, isSubscriber, tableId,customer);
		// else
		// billController.initData(amountToPay, subscriberId ,false, tableId);

	}

	private void showError(String msg) {
		lblError.setText(msg);
		lblError.setVisible(true);
	}

	private void closeWindow(ActionEvent event) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.close();
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		Response res = (Response)msg;
		Order o = (Order)res.getData();
		o.setTableNumber(null);
		this.order = o;
	}
	
}
