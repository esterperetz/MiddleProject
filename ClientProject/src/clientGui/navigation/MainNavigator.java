package clientGui.navigation;
import clientGui.ClientUi;
import clientGui.reservation.ReservationController;
import Entities.Alarm;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class MainNavigator {

    private static Stage mainStage;
   
    // שמירת ה-Stage הראשי בפעם הראשונה שהאפליקציה עולה
    public static void setStage(Stage stage) {
        mainStage = stage;
        
    }

    /**
     * פונקציה לטעינת מסך חדש
     * @param fxmlFileName שם קובץ ה-FXML (ללא סיומת .fxml)
     */
    public static void loadScene(String fxmlFileName) {
        try {
        	
            FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlFileName + ".fxml"));
            
            // הערה: בדרך כלל קבצי FXML מתחילים ב-AnchorPane ולא ב-Scene
            // הקוד כאן מניח שהשורש הוא Parent (כמו AnchorPane/VBox)
            Parent root = loader.load(); 
            
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load FXML: " + fxmlFileName);
        }
    }
    
    public static void loadReservationScreen(boolean isSubscriber, String phone, String email, String name) {
        try {
            // 1. טעינת ה-Loader
            FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/reservation/ReservationScreen.fxml"));
            Parent root = loader.load();

            // 2. קבלת הקונטרולר מתוך ה-Loader
            ReservationController controller = loader.getController();
            
            // 3. הזרקת הנתונים לקונטרולר
            controller.initData(isSubscriber, phone, email, name);

            // 4. הצגת המסך
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadOrderTableScreen(boolean isSubscriber, String phone, String email, String name)
    {
    	try {
	        FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/reservation/orderUi.fxml"));
	        Parent root = loader.load();

	        clientGui.reservation.OrderUi_controller controller = loader.getController();

	         
	        controller.initData(ClientUi.getInstance(), "localhost"); 

	        
	        Scene scene = new Scene(root); 
	        //Stage stage = (Stage) usernameField.getScene().getWindow();
	        mainStage.setScene(scene);
	        mainStage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error loading OrderUi: " + e.getMessage());
	    }
    }
    
    //ask liel about this function(ido)
    public static void showAlert(String header_text,String context_text,Alert.AlertType type) {
    	
    	Alarm.showAlert(header_text, context_text, type);
    }
}
