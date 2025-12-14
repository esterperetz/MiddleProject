package clientGui.managerTeam;

import clientGui.navigation.MainNavigator;

// <--- השינוי החשוב

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RestaurantLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    void performLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Login attempt: " + username);
        // כאן תוסיף את הלוגיקה
    }

    @FXML
    void goBack(ActionEvent event) {
        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
        MainNavigator.loadScene("/navigation/SelectionScreen");
    }
}
