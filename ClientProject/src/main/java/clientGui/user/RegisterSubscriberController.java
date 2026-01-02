package clientGui.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator; // ודא שיש לך את ה-Import הזה
import clientLogic.EmployeeLogic;
import clientLogic.UserLogic;
import entities.ActionType;
import entities.Response;
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
	private ActionEvent currentEvent; // Added to save the event for async navigation

	/**
	 * Handles the registration process when "Register Now" is clicked.
	 */
	@FXML
	void handleRegisterBtn(ActionEvent event) {
		// 1. Get data from fields
		String username = txtUsername.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();
		
		this.currentEvent = event; // Save current event

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
//		clientUi.addListener(this);//MUST DO NOT FORGER
		EmployeeLogic emp = new EmployeeLogic(clientUi);//MUST DO NOT FORGER
		emp.createSubscriber(new Customer(0,0, username, phone, email,CustomerType.SUBSCRIBER)); //CHANGED FROM 123456 TO 0 (AUTO INC)
		} catch(Exception e) {
			System.out.println("one ");
		}

	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
		if (msg instanceof Response) {
			Response res = (Response) msg;
			System.out.println(res.getStatus().getString());
			
			// Handle successful registration and navigate to Subscriber Options
			if (super.isEquals(res.getStatus(), "SUCCESS")) {
				Platform.runLater(() -> {
//					Employee newEmp = (Employee) res.getData();
					ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", currentEvent, clientUi);
					if (controller != null) {
						controller.initData(this.clientUi,this.isManager);
					}
				});
			} else if (res.getStatus() == Response.ResponseStatus.ERROR) {
				Platform.runLater(() -> lblMessage.setText(res.getMessage_from_server()));
			}
		} else
			System.out.println("nothing works");
		} catch(Exception e) {
			System.out.println("two ");
		}

	}
	public void initData(ClientUi clientUi,Employee.Role isManager)
	{
		this.clientUi=clientUi;
		this.isManager=isManager;
	}

	/**
	 * Navigates back to the previous menu.
	 */
	@FXML
	void handleBackBtn(ActionEvent event) {
		// Fixed: Navigate back to SelectionScreen instead of Manager Dashboard
//		super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
		// Fixed: Navigate back to EmployeeOption maybe the employee has more operation that he want to do before disconnecting 
		ManagerOptionsController controller = 
    	        super.loadScreen("managerTeam/EmployeeOption", event,clientUi);
    	if (controller != null) {
    			controller.initData(clientUi,this.isManager);
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