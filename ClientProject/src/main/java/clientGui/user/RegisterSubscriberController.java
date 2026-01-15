package clientGui.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator; // ודא שיש לך את ה-Import הזה
import clientLogic.EmployeeLogic;
import clientLogic.UserLogic;
import entities.Response;
import entities.Response.ResponseStatus;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import javafx.application.Platform; // Added import for Platform

public class RegisterSubscriberController extends MainNavigator implements MessageListener<Object>{
	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private Label lblMessage;
	private Employee.Role isManager;
	private UserLogic UserLogic;
	private ActionEvent currentEvent; 
	private EmployeeLogic employeeLogic;
	// Added to save the event for async navigation

	private Employee emp;
	
	public void initData(Employee emp, ClientUi clientUi,Employee.Role isManager)
	{
		this.emp = emp;
		this.clientUi=clientUi;
		this.isManager=isManager;
		employeeLogic = new EmployeeLogic(clientUi);//MUST DO NOT FORGER

	}

	/**
	 * Handles the registration process when "Register Now" is clicked.
	 */
	@FXML
	void handleRegisterBtn(ActionEvent event) {
		// 1. Get data from fields
		String username = txtUsername.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();
		
		 // Save current event

		// 2. Validate Input (Basic checks)
		if (username.isEmpty() || phone.isEmpty() || email.isEmpty()) {
			lblMessage.setText("Error: All fields are required!");
			lblMessage.setStyle("-fx-text-fill: #ff6b6b;"); // Red color
			return;
		}

		if (!email.contains("@")) {
			lblMessage.setText("Error: Invalid email format.");
			lblMessage.setStyle("-fx-text-fill: #ff6b6b;");
			return;
		}
		try {
			
		employeeLogic.createSubscriber(new Customer(0,0, username, phone, email,CustomerType.SUBSCRIBER)); //CHANGED FROM 123456 TO 0 (AUTO INC)
		this.currentEvent = event;
		} catch(Exception e) {
			System.out.println("one ");
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
	            case EMPLOYEE: 
	                handleUserResponse(res);
	                break;
	            default:
	                break;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	}


	private void handleUserResponse(Response res) {
	    if (res.getStatus() == ResponseStatus.SUCCESS) {
	        try {
	            ManagerOptionsController controller = super.loadScreen(
	                "managerTeam/EmployeeOption", 
	                currentEvent, 
	                clientUi
	            );
	            
	            if (controller != null) {
		        	Alarm.showAlert("SUCCESS", "subscriber add succesfully", AlertType.INFORMATION);
	                controller.initData(emp, this.clientUi, this.isManager);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    } 
	    else if (res.getStatus() == ResponseStatus.ERROR) {
	        if (lblMessage != null) {
	            lblMessage.setText(res.getMessage_from_server());
	        }
	    }
	}

	/**
	 * Navigates back to the previous menu.
	 */
	@FXML
	void handleBackBtn(ActionEvent event) {
		ManagerOptionsController controller = 
    	        super.loadScreen("managerTeam/EmployeeOption", event,clientUi);
    	if (controller != null) {
    			controller.initData(emp,clientUi,this.isManager);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
	}
	

	/**
	 * Clears the input fields after successful registration.
	 */
	private void clearFields() {
		txtUsername.clear();
		txtPhone.clear();
		txtEmail.clear();
	}


}