package clientGui.user;

import javafx.scene.control.Button;
//import java.util.ResourceBundle;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.CheckOutController;
import clientGui.reservation.GetTableController;
import clientGui.reservation.ReservationController;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;


import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;

public class SubscriberOptionController extends MainNavigator implements Initializable , MessageListener<Object>{
	private static boolean isSubscriber=true;
	//private ClientUi client_ui;

	// Link to the special button in the FXML file
	@FXML
    private Button btnSubscriberSpecial;
	private int subscriberId;
	private int tableId; //check how to initialize it
	
	
	/**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // By default, the button is hidden in FXML (visible="false", managed="false").
        
        // --- Check if the current user is a subscriber ---
        // TODO: Replace 'true' with your actual logic, e.g., ClientUI.currentUser.isSubscriber()
        //isSubscriber = true; 

        if (isSubscriber) {
            // If the user is a subscriber, reveal the button and let it take up space in the layout
            btnSubscriberSpecial.setVisible(true);
            System.out.println("HII");
            btnSubscriberSpecial.setManaged(true);
        }
    }
    public void initData(ClientUi clientUi, boolean isSubscriberStatus, int subId) {
    	this.clientUi = clientUi;
        this.isSubscriber = isSubscriberStatus;
        this.subscriberId = subId;
        System.out.println("Loaded options for subscriber: " + subId);
        // 1. רישום לקבלת הודעות (אם צריך בדף זה)
        /*
        if (this.client_ui != null) {
            this.client_ui.addListener(this);
        }
        */

        // 2. לוגיקה גרפית לפי סוג המשתמש
        if (this.isSubscriber) {
            // אם הוא מנוי - מציגים את הכפתור
            btnSubscriberSpecial.setVisible(true);
            btnSubscriberSpecial.setManaged(true);
        } else {
            // אם הוא אורח - מסתירים
            btnSubscriberSpecial.setVisible(false);
            btnSubscriberSpecial.setManaged(false);
        }
    }
	@FXML
	 void goBackBtn(ActionEvent event)
	{
		int flag=1;
		//when we succeed to log in, we dont go back to navigation screen
		if(flag==1)
		{
			//we need to check if the user is subscriber and if the code is correct
			super.loadScreen("user/SubscriberLogin",event,clientUi);
			flag=0;
		}
		else
	        super.loadScreen("navigation/SelectionScreen" ,event,clientUi);

	}
	@FXML
	void goToReservationBtn(ActionEvent event)
	{
		ReservationController controller = super.loadScreen("reservation/ReservationScreen", event,clientUi);

	    if (controller != null) {
//	        controller.setData(isSubscriber, "", "", ""); 
	    	controller.initData(clientUi, isSubscriber, subscriberId);
	    }	
	 }
	
	@FXML
	void goToSeatTableBtn(ActionEvent event)
	{
		GetTableController getTableController =super.loadScreen("reservation/RecieveTable",event,clientUi);
		getTableController.initData(clientUi, isSubscriber, subscriberId);
	}
	/**
     * Action handler for the special subscriber-only button.
     */
    @FXML
    void subscriberActionBtn(ActionEvent event) {
        // Logic specific to subscribers goes here
        System.out.println("Subscriber specific action executed.");
        
        SubscriberHistoryController subHistoryController = super.loadScreen("user/SubscriberHistory",event,clientUi);
        subHistoryController.initData(subscriberId);

    }

    @FXML
    void CheckOutActionBtn(ActionEvent event) {
    	CheckOutController checkOutController = super.loadScreen("reservation/CheckOutScreen",event,clientUi);
    	checkOutController.initData(subscriberId, tableId);
    }
	
	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
	public static boolean isSubscriber() {
		return isSubscriber;
	}
	public void setSubscriber(boolean isSubscriber) {
		this.isSubscriber = isSubscriber;
	}

}
