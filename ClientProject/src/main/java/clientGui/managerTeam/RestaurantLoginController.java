package clientGui.managerTeam;

import client.MessageListener;
import clientGui.navigation.MainNavigator;
import clientLogic.EmployeeLogic;
import entities.Alarm;
import entities.Employee;
import entities.Response;
import javafx.application.Platform;

// <--- השינוי החשוב

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class RestaurantLoginController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;

	private ActionEvent currentEvent;
	@FXML
	private Employee employee;
	private boolean isManager;

	// if clicking in X add that server will knows and disccount this client.
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

	public void initData() {

	}

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

	@FXML
	void goBack(ActionEvent event) {
		// וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		try {

			if (msg instanceof Response) {
				Response response = (Response) msg;
				Platform.runLater(() -> {

					if (response.getStatus() == Response.ResponseStatus.SUCCESS) {
						Alarm.showAlert("Login Succsesfully!", "Navigating to Manager Options...",
								AlertType.INFORMATION);
						// check if manager or regular worker
						try {
							// Response r=(Response)msg;
							Employee emp = (Employee) response.getData();
							// System.out.println(""+e.getRole());
							if (emp.getPassword().equals("newEmployee1234")) {

								SetEmployeePasswordController controller = super.loadScreen(
										"managerTeam/SetEmployeePassword", currentEvent, clientUi);
								controller.initData(clientUi, emp);

							} else {
								if (emp.getRole() == (Employee.Role.MANAGER)) {
									isManager = true;
								} else if (emp.getRole() == (Employee.Role.REPRESENTATIVE)) {
									isManager = false;

								}
								ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption",
										currentEvent, clientUi);

								// 2. אתחול הנתונים במסך החדש
								if (controller != null) {
									controller.AnotherinitData(emp);
									controller.initData(clientUi, emp.getRole());
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
