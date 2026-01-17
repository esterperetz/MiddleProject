package clientGui.managerTeam;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.EmployeeLogic;
import entities.Alarm;
import entities.Employee;
import entities.Employee.Role;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;

/**
 * Controller for setting a new password for an employee.
 * Usually invoked when an employee logs in for the first time with a temporary
 * password.
 */
public class SetEmployeePasswordController extends MainNavigator implements MessageListener<Object> {
	@FXML
	private PasswordField txtPassword;

	@FXML
	private PasswordField txtConfirmPassword;

	@FXML
	private Label lblMessage;

	private ActionEvent currentEvent;

	private Employee emp;

	private Role isManager;

	/**
	 * Initializes the controller with the current employee's session data.
	 * 
	 * @param emp       The employee whose password is being set
	 * @param c         The client UI instance
	 * @param isManager The role of the employee
	 */
	public void initData(Employee emp, ClientUi c, Employee.Role isManager) {
		this.emp = emp;
		this.clientUi = c;
		this.isManager = isManager;
	}

	/**
	 * Handles the "Save" button click.
	 * Validates that the password fields match and meet requirements, then updates
	 * the password.
	 * 
	 * @param event The action event
	 */
	@FXML
	void handleSaveBtn(ActionEvent event) {
		String pass = txtPassword.getText();
		String confirmPass = txtConfirmPassword.getText();

		// 1. Check if fields are empty
		if (pass.isEmpty() || confirmPass.isEmpty()) {
			lblMessage.setText("Please fill in both password fields.");
			return;
		}

		// 2. Check if passwords match
		if (!pass.equals(confirmPass)) {
			lblMessage.setText("Passwords do not match!");
			txtPassword.clear();
			txtConfirmPassword.clear();
			return;
		}

		// 3. Password length validation
		if (pass.length() < 6) {
			lblMessage.setText("Password must be at least 6 characters.");
			return;
		}

		this.currentEvent = event;
		EmployeeLogic employeeLogic = new EmployeeLogic(clientUi);
		emp.setPassword(confirmPass);
		employeeLogic.updatePassword(emp);

		System.out.println("Password set successfully for user.");
	}

	/**
	 * Handles server responses for the update password request.
	 * If successful, redirects the user to the login screen.
	 */
	@Override
	public void onMessageReceive(Object msg) {
		try {
			if (msg instanceof Response) {
				Response response = (Response) msg;
				Platform.runLater(() -> {

					if (response.getStatus().name().equals("SUCCESS")) {
						Alarm.showAlert("Password Set Successfully!", "Navigating to Employee Login...",
								AlertType.INFORMATION);
						try {
							Employee e = (Employee) response.getData();
							RestaurantLoginController controller = super.loadScreen("managerTeam/RestaurantLogin",
									currentEvent, clientUi);
							controller.initData(e, clientUi, isManager);

						} catch (Exception e) {
							System.out.println(response.getMessage_from_server());
						}

					} else {
						Alarm.showAlert("Error", "Failed to update password.", AlertType.ERROR);
					}

				});
				if (msg instanceof String && "quit".equals(msg)) {
					clientUi.disconnectClient();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Navigates back to the Selection Screen.
	 */
	@FXML
	void goToBackBtn(ActionEvent event) {
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}
}
