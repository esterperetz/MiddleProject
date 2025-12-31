package clientGui.user;

import javafx.scene.control.Button;
//import java.util.ResourceBundle;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.CheckOutController;
import clientGui.reservation.GetTableController;
import clientGui.reservation.ReservationController;
import entities.CustomerType;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;


import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;

public class SubscriberOptionController extends MainNavigator implements Initializable , MessageListener<Object>{
	private CustomerType isSubscriber;
	//private ClientUi client_ui;

	// Link to the special button in the FXML file
	@FXML
    private Button btnSubscriberSpecial;
	private Integer customerId;
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

        if (isSubscriber == CustomerType.SUBSCRIBER) {
            // If the user is a subscriber, reveal the button and let it take up space in the layout
            btnSubscriberSpecial.setVisible(true);
            System.out.println("HII");
            btnSubscriberSpecial.setManaged(true);
        }
    }
    public void initData(ClientUi clientUi, CustomerType CustomerStatus, Integer cusId) {
    	this.clientUi = clientUi;
        this.isSubscriber = CustomerStatus;
        this.customerId = cusId;
        System.out.println("Loaded options for subscriber: " + cusId);
        // 1. רישום לקבלת הודעות (אם צריך בדף זה)
        /*
        if (this.client_ui != null) {
            this.client_ui.addListener(this);
        }
        */
        // 2. לוגיקה גרפית לפי סוג המשתמש
        if (isSubscriber == CustomerType.SUBSCRIBER) {
        	//isSubscriber=true;
            btnSubscriberSpecial.setVisible(true);
            btnSubscriberSpecial.setManaged(true);
        } else {
            // 
        	//isSubscriber=false;
            btnSubscriberSpecial.setVisible(false);
            btnSubscriberSpecial.setManaged(false);
        }
    }
	@FXML
	 void goBackBtn(ActionEvent event)
	{
		if (isSubscriber == CustomerType.SUBSCRIBER) {
        	//isSubscriber=true;
            btnSubscriberSpecial.setVisible(true);
            btnSubscriberSpecial.setManaged(true);
            super.loadScreen("user/SubscriberLogin",event,clientUi);
        } else {
            // 
        	//isSubscriber=false;
            btnSubscriberSpecial.setVisible(false);
            btnSubscriberSpecial.setManaged(false);
			super.loadScreen("navigation/SelectionScreen",event,clientUi);

        }
		
	}
	@FXML
	void goToReservationBtn(ActionEvent event)
	{
		//fix
		ReservationController controller = super.loadScreen("reservation/ReservationScreen", event,clientUi);

	    //if (isSubscriber) {
//	        controller.setData(isSubscriber, "", "", ""); 
	    	if(controller!= null)	
	    		controller.initData(clientUi, this.isSubscriber, customerId);
	    	else
	    		System.out.println("Error: moving screen ReservationController");
	    		
	    	
	 }
	@FXML
	void goToSeatTableBtn(ActionEvent event)
	{
		//fix
		GetTableController getTableController =super.loadScreen("reservation/RecieveTable",event,clientUi);
		//if(isSubscriber)	
		if(getTableController!=null)
			getTableController.initData(clientUi, this.isSubscriber, customerId);
		else
			System.out.println("Error: moving to GetTableController");
    	//else
    		//getTableController.initData(clientUi, false, subscriberId);
		//getTableController.initData(clientUi, subscriberId, subscriberId);
	}

	/**
     * Action handler for the special subscriber-only button.
     */
    @FXML
    void subscriberActionBtn(ActionEvent event) {
    	//fix
        // Logic specific to subscribers goes here
        System.out.println("Subscriber specific action executed.");
        
        SubscriberHistoryController subHistoryController = super.loadScreen("user/SubscriberHistory",event,clientUi);
        //if(isSubscriber)	
        	subHistoryController.initData(customerId,this.isSubscriber);
    	//else
    		//subHistoryController.initData(subscriberId,false);
        //subHistoryController.initData(subscriberId);

    }

    @FXML
    void CheckOutActionBtn(ActionEvent event) {
    	CheckOutController checkOutController = super.loadScreen("reservation/CheckOutScreen",event,clientUi);
    	 //if(isSubscriber)	
    		 checkOutController.initData(customerId,this.isSubscriber, tableId);
     	//else
     		//checkOutController.initData(subscriberId,false, tableId);
    	//checkOutController.initData(subscriberId, tableId);
    }
	
	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
	
	//public void setSubscriber(boolean isSubscriber) {
		//this.isSubscriber = isSubscriber;
	//}

}
