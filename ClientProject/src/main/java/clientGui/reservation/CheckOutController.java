package clientGui.reservation;

import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.OrderLogic;
import entities.Alarm;
import entities.CustomerType;
import entities.Order;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CheckOutController extends MainNavigator implements  Initializable , MessageListener<Object>{

	@FXML
	private TextField txtConfirmationCode;

	@FXML
	private Label lblResult;

	private Integer currentSubscriberCode;
	private CustomerType isSubsriber;
	private OrderLogic orderLogic;
	private int tableId;

	private ActionEvent currentEvent;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			if (txtConfirmationCode.getScene() != null && txtConfirmationCode.getScene().getWindow() != null) {
				Stage stage = (Stage) txtConfirmationCode.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();

				});
			}
		});
	}
	public void initData(Integer subscriberCode,CustomerType isSubsriber, int tableId) {
		// this.clientUi = clientUi;
//		this.clientUi.addListener(this);
		this.isSubsriber=isSubsriber;
		this.currentSubscriberCode= subscriberCode;
		this.orderLogic = new OrderLogic(clientUi);
		System.out.println("Fetching history for subscriber: " + subscriberCode);
//		orderLogic.getOrdersBySubscriberCode(subscriberId);
	
	}
	
	@FXML
	void getPaymentBil(ActionEvent event) {
	    String ConfirmationCode = txtConfirmationCode.getText();

	    if (ConfirmationCode == null || ConfirmationCode.trim().isEmpty()) {
	        lblResult.setText("Please enter a valid Confirmation Code.");
	        lblResult.setStyle("-fx-text-fill: #ff6b6b;");
	        return;
	    }
	    this.currentEvent = event;
	    try {
	    	orderLogic.getOrderByConfirmationCode(Integer.parseInt(ConfirmationCode),currentSubscriberCode);
	    }
	    catch(NumberFormatException e) {
	    	Alarm.showAlert("Code Format Error", "Code should be numeric!", Alert.AlertType.ERROR);
	    }
	   
	}



	/**
	 * Triggered when the "Back" button is clicked. Navigates back to the main
	 * reservation menu.
	 */
	@FXML
	void goBack(ActionEvent event) {
		//MainNavigator.loadScreen("user/SubscriberOption", clientUi);
		SubscriberOptionController controller = 
    	       super.loadScreen("user/SubscriberOption", event,clientUi);
    	//if (isSubsriber)
		if(controller!=null)
		{
    		controller.initData(clientUi,isSubsriber, currentSubscriberCode);
        } else {
            System.err.println("Error: Could not load SubscriberOption.");
        }
	}
	
	


	


	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
	    if (!(msg instanceof Response)) return;
	    Response res = (Response) msg;

	    Platform.runLater(() -> {
	        try {
	            switch (res.getAction()) {
	                case GET_BY_CODE:
	                    handleOrderResponse(res);
	                    break;
	                default:
	                    System.out.println("Unhandled Action: " + res.getAction());
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            Alarm.showAlert("System Error", "An error occurred while processing server response.", Alert.AlertType.ERROR);
	        }
	    });
	}
	
	private void handleOrderResponse(Response res) {
		if (res.getStatus().name().equals("SUCCESS")) {
			
			 Order order = (Order)res.getData();   
			 if(order.getTableNumber() != null) { 
				 order.setLeavingTime(new java.util.Date());
				 BillController bill_controller = super.loadScreen("reservation/Bill", currentEvent, clientUi);				 
				 bill_controller.initData(order, currentSubscriberCode, this.isSubsriber,order.getTableNumber());
			 }
			 else {
				 Alarm.showAlert("Order Error", "Table Number is Invalid/Not found", Alert.AlertType.ERROR);
			 }
		}
		else {
			 Alarm.showAlert("Order Error", "Confirmation Code is Invalid/Not found", Alert.AlertType.ERROR);
		}
	
		
		
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
