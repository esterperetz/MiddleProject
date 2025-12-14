package clientGui.subscriber;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import clientGui.navigation.MainNavigator; // ודא שיש לך את ה-Import הזה

public class RegisterSubscriberController {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    @FXML
    private Label lblMessage;

    /**
     * Handles the registration process when "Register Now" is clicked.
     */
    @FXML
    void handleRegisterBtn(ActionEvent event) {
        // 1. Get data from fields
        String username = txtUsername.getText();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();

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

        // 3. Send to Server (Simulation)
        // TODO: specific logic to send data to server (e.g., ClientUI.chat.registerSubscriber(...))
        boolean success = mockServerRegistration(username, phone, email);

        if (success) {
            lblMessage.setText("Success! Subscriber registered.");
            lblMessage.setStyle("-fx-text-fill: #51cf66;"); // Green color
            clearFields();
        } else {
            lblMessage.setText("Error: Registration failed (User might exist).");
            lblMessage.setStyle("-fx-text-fill: #ff6b6b;");
        }
    }

    /**
     * Navigates back to the previous menu.
     */
    @FXML
    void handleBackBtn(ActionEvent event) {
        // Change "manager/ManagerOptions" to wherever you want to go back to
        MainNavigator.loadScene("managerTeam/workerOption"); 
    }

    /**
     * Clears the input fields after successful registration.
     */
    private void clearFields() {
        txtUsername.clear();
        txtPhone.clear();
        txtEmail.clear();
    }

    /**
     * Simulation of server response.
     */
    private boolean mockServerRegistration(String name, String phone, String email) {
        System.out.println("Registering: " + name + ", " + phone + ", " + email);
        return true; // Simulate success
    }
}
