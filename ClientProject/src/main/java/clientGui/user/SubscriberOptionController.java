package clientGui.user;

import javafx.scene.control.Button;
import javafx.scene.control.Label; // <--- הוספתי את זה
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

    @FXML
    void goToUpdateProfile(ActionEvent event) {
        System.out.println("Navigating to update profile...");
        
        // כאן עלייך ליצור את המסך והקונטרולר של עדכון פרטים
        // לדוגמה:
        UpdateProfileController controller = super.loadScreen("user/UpdateProfile", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, customer);
        }
        
        System.out.println("Update Profile Screen is not ready yet.");
    }
    // ----------------------------------

	@FXML
	void goBackBtn(ActionEvent event) {
		if (isSubscriber == CustomerType.SUBSCRIBER) {
			super.loadScreen("user/SubscriberLogin", event, clientUi);
		} else {
			super.loadScreen("navigation/SelectionScreen", event, clientUi);
		}
	}

	@FXML
	void goToReservationBtn(ActionEvent event) {
		ReservationController controller = super.loadScreen("reservation/ReservationScreen", event, clientUi);

		if (controller != null)
			controller.initData(clientUi, this.isSubscriber, subId, customer);
		else
			System.out.println("Error: moving screen ReservationController");
	}

	@FXML
	void goToSeatTableBtn(ActionEvent event) {
		GetTableController getTableController = super.loadScreen("reservation/RecieveTable", event, clientUi);
		if (getTableController != null)
			getTableController.initData(clientUi, this.isSubscriber, subId, customer);
		else
			System.out.println("Error: moving to GetTableController");
	}

	@FXML
	void subscriberActionBtn(ActionEvent event) {
		System.out.println("Subscriber specific action executed.");
		SubscriberHistoryController subHistoryController = super.loadScreen("user/SubscriberHistory", event, clientUi);
        if(subHistoryController != null) {
		    subHistoryController.initData(subId, this.isSubscriber,null, customer);
        }
	}

	@FXML
	void CheckOutActionBtn(ActionEvent event) {
		CheckOutController checkOutController = super.loadScreen("reservation/CheckOutScreen", event, clientUi);
        if(checkOutController != null) {
		    checkOutController.initData(subId, this.isSubscriber, tableId, customer);
        }
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
	}
}