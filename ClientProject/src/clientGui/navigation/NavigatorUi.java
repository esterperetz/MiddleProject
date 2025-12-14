package clientGui.navigation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigatorUi extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
    	
    	MainNavigator.setStage(primaryStage);
//    	String fxmlPath = "/clientGui/navigation/SelectionScreen.fxml";
    	String fxmlPath = "/clientGui/logInServer.fxml";
    	java.net.URL location = getClass().getResource(fxmlPath);
    	
    	
    	// בדיקה האם הקובץ נמצא לפני שמנסים לטעון
    	if (location == null) {
    	    System.err.println("ERROR: Could not find FXML at: " + fxmlPath);
    	    // ניסיון גיבוי - אולי בלי הסלאש הראשון?
    	    location = getClass().getResource("/clientGui/logInServer.fxml");
    	}

    	if (location == null) {
    	    throw new IllegalStateException("FATAL: FXML file not found! Check project structure.");
    	}
    	
    	try {
    	    // 1. הטעינה שעשית (מביאה את ה-Root)
    	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/logInServer.fxml"));
    	    Parent root = loader.load();

    	    // 2. יצירת ה-Scene (הדף) שמכיל את ה-Root
    	    Scene scene = new Scene(root);

    	    // 3. הגדרת החלון (Stage) והצגתו
    	    // הערה: וודא שיש לך גישה ל-primaryStage.
    	    // אם אתה בתוך ה-start(), השתמש במשתנה שקיבלת:
    	    primaryStage.setScene(scene);
    	    primaryStage.setTitle("BISTRO System"); // כותרת לחלון
    	    primaryStage.show(); // <--- הפקודה הכי חשובה! בלעדיה לא רואים כלום

    	} catch (Exception e) {
    	    e.printStackTrace(); // חובה כדי לראות אם יש שגיאות נסתרות
    	}
    	
        
   
    }

    public static void main(String[] args) {
        launch(args);
    }
}
