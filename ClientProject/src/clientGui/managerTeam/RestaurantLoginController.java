package clientGui.managerTeam;

import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;

// <--- השינוי החשוב

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RestaurantLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    void performLogin(ActionEvent event) {
    	//ליאל עידו צריך לדבר איתך!!!!!!!!!!!!!!!!!!!!
    	    String username = usernameField.getText();
    	    String password = passwordField.getText();

    	    // בדיקת תקינות בסיסית (לוגיקה שלך)
    	    System.out.println("Login attempt: " + username);

    	    try {
    	        // 1. טעינת ה-FXML ידנית
    	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/OrderUi.fxml"));
    	        Parent root = loader.load();

    	        // 2. קבלת הקונטרולר של המסך החדש
    	        clientGui.reservation.OrderUi_controller controller = loader.getController();

    	        // 3. העברת ה-ClientUi וה-IP לקונטרולר (קריטי!)
    	        // הנחה: יש לך גישה ל-clientUi הנוכחי (אולי דרך משתנה סטטי או שהועבר ללוגין)
    	        // אם אין לך אותו כאן, צריך לוודא מאיפה משיגים אותו.
    	        // דוגמה: 
    	        controller.initData(ClientUi.getInstance(), "localhost"); 

    	        // 4. שימוש ב-MainNavigator רק כדי להחליף את הסצנה (אם יש לו פונקציה כזו)
    	        // או החלפה ידנית:
    	        Scene scene = new Scene(root); 
    	        Stage stage = (Stage) usernameField.getScene().getWindow();
    	        stage.setScene(scene);
    	        stage.show();

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        System.out.println("Error loading OrderUi: " + e.getMessage());
    	    }
    	
        // כאן תוסיף את הלוגיקה
    }

    @FXML
    void goBack(ActionEvent event) {
        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
        MainNavigator.loadScene("navigation/SelectionScreen");
    }
}
