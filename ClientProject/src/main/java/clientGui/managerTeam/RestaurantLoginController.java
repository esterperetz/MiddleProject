package clientGui.managerTeam;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.EmployeeLogic;
import entities.Alarm;
import entities.Employee;
import entities.Response;
import javafx.application.Platform;

// <--- השינוי החשוב

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
				employeeLogic.getManagerByEmployee(employee);
			});

		} catch (Exception e) {
			System.out.println("Please enter valid input!");
		}

		// 1. שימוש בפונקציה הגנרית וקבלת הקונטרולר
//		ManagerOptionsController controller = super.loadScreen("managerTeam/workerOption", event,clientUi);
//
//		// 2. אתחול הנתונים במסך החדש
//		if (controller != null) {
//			controller.initData(clientUi,ManagerOptionsController.isManager());
//		} else {
//			System.err.println("Failed to load ManagerOptionsController. Check FXML path.");
//		}
		/*
		 * try { //moved to MainNavigation (the name of the
		 * function:loadOrderTableScreen) FXMLLoader loader = new
		 * FXMLLoader(getClass().getResource("/clientGui/reservation/OrderUi.fxml"));
		 * Parent root = loader.load();
		 * 
		 * clientGui.reservation.OrderUi_controller controller = loader.getController();
		 * 
		 * 
		 * controller.initData(ClientUi.getInstance(), "localhost");
		 * 
		 * : Scene scene = new Scene(root); Stage stage = (Stage)
		 * usernameField.getScene().getWindow(); stage.setScene(scene); stage.show();
		 * 
		 * } catch (Exception e) { e.printStackTrace();
		 * System.out.println("Error loading OrderUi: " + e.getMessage()); }
		 */

		// כאן תוסיף את הלוגיקה
	}

	@FXML
	void goBack(ActionEvent event) {
		// וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

	public void initData() {

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
							//Response r=(Response)msg;
							Employee e = (Employee) response.getData();
							//System.out.println(""+e.getRole());
							if (e.getRole() == (Employee.Role.MANAGER)) {
								isManager=true;
							} else if (e.getRole() == (Employee.Role.REPRESENTATIVE)) {
								isManager=false;
							}
						} catch (Exception e) {
							System.out.println("Error: You aren't MANAGER or REPRESENTATIVE");
						}
						ManagerOptionsController controller = super.loadScreen("managerTeam/workerOption", currentEvent,
								clientUi);

						// 2. אתחול הנתונים במסך החדש
						if (controller != null) {
							controller.initData(clientUi, isManager);
						} else {
							System.err.println("Failed to load ManagerOptionsController. Check FXML path.");
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
