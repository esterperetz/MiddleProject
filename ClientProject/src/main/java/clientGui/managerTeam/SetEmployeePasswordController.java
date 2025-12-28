package clientGui.managerTeam;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.EmployeeLogic;
import entities.Alarm;
import entities.Employee;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;

public class SetEmployeePasswordController extends MainNavigator implements MessageListener<Object> {
	@FXML
	private PasswordField txtPassword;

	@FXML
	private PasswordField txtConfirmPassword;

	@FXML
	private Label lblMessage;
	
	private ActionEvent currentEvent;
	
	private Employee emp;
	
	
	
	public void initData(ClientUi clientUi, Employee emp) {
		this.clientUi = clientUi;
		this.emp = emp;
	}

	@FXML
	void handleSaveBtn(ActionEvent event) {
	    String pass = txtPassword.getText();
	    String confirmPass = txtConfirmPassword.getText();

	    // 1. בדיקה שהשדות לא ריקים
	    if (pass.isEmpty() || confirmPass.isEmpty()) {
	        lblMessage.setText("Please fill in both password fields.");
	        return;
	    }

	    // 2. בדיקה שהסיסמאות תואמות
	    if (!pass.equals(confirmPass)) {
	        lblMessage.setText("Passwords do not match!");
	        // אופציונלי: לנקות את השדות
	        txtPassword.clear();
	        txtConfirmPassword.clear();
	        return;
	    }
	    
	    // 3. (אופציונלי) בדיקת אורך סיסמה
	    if (pass.length() < 6) {
	        lblMessage.setText("Password must be at least 6 characters.");
	        return;
	    }
	    this.currentEvent = event;
	    EmployeeLogic employee = new EmployeeLogic(clientUi);
		//employee.updatePassword(new Employee(emp.getUserName(), confirmPass,emp.getPhoneNumber(), emp.getEmail(), emp.getRole())); // CHANGED FROM
		emp.setPassword(confirmPass);
	    employee.updatePassword(emp); // CHANGED FROM
	    

	    System.out.println("Password set successfully: " + pass);
	    // כאן תכתבי את הקוד ששומר את העובד למסד הנתונים
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		try {
			
			if (msg instanceof Response) {
				Response response = (Response) msg;
				Platform.runLater(() -> {

					if (response.getStatus() == Response.ResponseStatus.SUCCESS) {
						Alarm.showAlert("Login Succsesfully!", "Navigating to Employee Login...",
								AlertType.INFORMATION);
						// check if manager or regular worker
						try {
							// Response r=(Response)msg;
							Employee e = (Employee) response.getData();
							//Add   alert
							
							
						} catch (Exception e) {
							System.out.println(response.getMessage_from_server());
						}
						RestaurantLoginController controller = super.loadScreen("managerTeam/RestaurantLogin",
								currentEvent, clientUi);

						// 2. אתחול הנתונים במסך החדש
						
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
	
	
	@FXML
	void goToBackBtn(ActionEvent event) {
		// וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

}
