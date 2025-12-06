package clientGui;

	

import java.io.IOException;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtIp;

    @FXML
    private Label lblStatus;

    // החיבור לשרת – דרך ChatClient או ClientUi, לפי מה שנוח לך
    private ClientUi clientUi;

    @FXML
    private void onConnect(ActionEvent event) {
        String ip = txtIp.getText().trim();

        if (ip.isEmpty()) {
            lblStatus.setText("Please enter server IP");
            return;
        }

        try {
            // יצירת ClientUi שמתחבר לשרת לפי ה-IP שהמשתמש הכניס
            clientUi = new ClientUi(ip);

            // אם הכל עבר בלי Exception – הצלחנו
            if (clientUi != null) {
                lblStatus.setText("Login succeeded. Connected to: " + ip);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/orderUi.fxml"));
                Parent root = loader.load();

                // 2) מקבלים את הקונטרולר ש־FXML יצר
                OrderUi_controller controller = loader.getController();

                // 3) מעבירים לו את ה-ClientUi וה-ip
                controller.initData(clientUi, ip);

                // 4) מעבר סצינה
                Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                primaryStage.setTitle("Orders Management");
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
    	        
                // כאן אפשר לעבור למסך הראשי (orders וכו') אם יש לך כזה
            } else {
                lblStatus.setText("Login failed (clientUi is null)");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Login failed: " + e.getMessage());
        }
    }

    /**
     * מאפשר למסכים אחרים לקבל את החיבור לשרת אחרי ה-login
     */
    public ClientUi getClientUi() {
        return clientUi;
    }
}

