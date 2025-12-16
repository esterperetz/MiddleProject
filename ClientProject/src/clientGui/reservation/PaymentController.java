package clientGui.reservation;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class PaymentController extends MainNavigator implements  MessageListener<Object>, BaseController{

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

	public void setPaymentDetails(double amount, int tableId) {
		this.amountToPay = amount;
		this.tableId = tableId;
	}

	@FXML
	void processPayment(ActionEvent event) {
		// 1. איפוס שגיאות קודמות
		lblError.setVisible(false);

		String cardNum = txtCardNumber.getText().trim();
		String id = txtID.getText().trim();
		String expiry = txtExpiry.getText().trim();
		String cvv = txtCVV.getText().trim();

		// 2. ולידציה בסיסית (Validation)
		if (cardNum.isEmpty() || id.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
			showError("Please fill in all fields.");
			return;
		}

		if (cardNum.length() < 12) { // בדיקה שטחית לאורך כרטיס
			showError("Invalid Card Number.");
			return;
		}

		if (cvv.length() != 3) {
			showError("CVV must be 3 digits.");
			return;
		}

		System.out.println("Processing Credit Card Payment...");
		System.out.println("Card: " + cardNum + " | Amount: " + amountToPay);


		System.out.println("Payment Approved! Table " + tableId + " released.");
		//Alert pay good 
		super.loadScreen("user/SubscriberOption",event,clientUi);

	}

	@FXML
	void cancel(ActionEvent event) {
		closeWindow(event);
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
		
	}
}
