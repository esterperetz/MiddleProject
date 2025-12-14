package clientGui.navigation;
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
    private static Alarm alarm;
    // שמירת ה-Stage הראשי בפעם הראשונה שהאפליקציה עולה
    public static void setStage(Stage stage) {
        mainStage = stage;
        try {
			alarm = new Alarm();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
    
    public static void showAlert(String header_text,String context_text,Alert.AlertType type) {
    	
    	alarm.showAlert(header_text, context_text, type);
    }
}
