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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * Controller for the Restaurant Employee Login screen.
 * Handles employee authentication for accessing internal management systems.
 */
public class RestaurantLoginController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;

	private ActionEvent currentEvent;
	@FXML
	private Employee employee;
	private Role isManager;
	private Employee emp;

	/**
	 * Initializes the controller.
	 * Sets up a listener to disconnect the client if the window is closed.
	 */
	@FXML
	public void initialize() {
		Platform.runLater(() -> {
			if (usernameField.getScene() != null && usernameField.getScene().getWindow() != null) {
				Stage stage = (Stage) usernameField.getScene().getWindow();
				stage.setOnCloseRequest(event -> {
					clientUi.disconnectClient();
				});
			}
		});
	}

	/**
	 * Initializes the controller with necessary data.
	 * 
	 * @param emp       The employee object (if applicable)
	 * @param c         The client UI instance
	 * @param isManager The role of the employee
	 */
	public void initData(Employee emp, ClientUi c, Employee.Role isManager) {
		this.emp = emp;
		this.clientUi = c;
		this.isManager = isManager;
	}

	/**
	 * Handles the login action.
	 * Collects username and password, creates an Employee object, and sends a login
	 * request to the server.
	 * 
	 * @param event The action event
	 */
	@FXML
	void performLogin(ActionEvent event) {
		try {
			String username = usernameField.getText();
			String password = passwordField.getText();
			Platform.runLater(() -> {
				employee = new Employee(username, password);

				System.out.println("Login attempt for: " + username);

				EmployeeLogic employeeLogic = new EmployeeLogic(clientUi);
				this.currentEvent = event;
				employeeLogic.loginEmployee(employee);
			});

		} catch (Exception e) {
			System.out.println("Please enter valid input!");
		}
	}

	/**
	 * Navigates back to the Selection Screen.
	 */
	@FXML
	void goBack(ActionEvent event) {
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

	/**
	 * Handles messages received from the server.
	 * Processes login responses, redirects to the appropriate dashboard
	 * (Manager/Employee),
	 * or prompts for password setup if it's a first-time login.
	 */
	@Override
	public void onMessageReceive(Object msg) {
		try {
			if (msg instanceof Response) {
				Response response = (Response) msg;
				Platform.runLater(() -> {
					if (response.getStatus().name().equals("SUCCESS")) {
						Alarm.showAlert("Login Successfully!", "Navigating to Manager Options...",
								AlertType.INFORMATION);
						try {
							Employee emp = (Employee) response.getData();
							// Check if this is a first-time login (placeholder password)
							if (emp.getPassword().equals("newEmployee1234")) {
								SetEmployeePasswordController controller = super.loadScreen(
										"managerTeam/SetEmployeePassword", currentEvent, clientUi);
								controller.initData(emp, clientUi, emp.getRole());
							} else {
								if (emp.getRole().name().equals("MANAGER")) {
									isManager = Employee.Role.MANAGER;
								} else if (emp.getRole().name().equals("REPRESENTATIVE")) {
									isManager = Employee.Role.REPRESENTATIVE;
								}

								ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption",
										currentEvent, clientUi);

								if (controller != null) {
									controller.initData(emp, clientUi, isManager);
								} else {
									System.err.println("Failed to load ManagerOptionsController. Check FXML path.");
								}
							}
						} catch (Exception e) {
							System.out.println("Error: You aren't MANAGER or REPRESENTATIVE");
						}

					} else {
						Alarm.showAlert("Incorrect Input", "Your username or password is invalid!", AlertType.ERROR);
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
}
