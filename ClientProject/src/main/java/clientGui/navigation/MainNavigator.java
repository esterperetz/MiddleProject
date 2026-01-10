package clientGui.navigation;

import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.reservation.ReservationController;
import entities.Alarm;
import entities.Response.ResponseStatus;
import client.MessageListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class MainNavigator implements BaseController {
	private MessageListener currentListener;
	private Stage mainStage;
	protected ClientUi clientUi;// importent!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	
	@Override
	public void setMainNavigator(MainNavigator navigator) {

	}

	@Override
	public void setClientUi(ClientUi clientUi) {
		this.clientUi = clientUi;
	}

	public Stage getStage() {
		return mainStage;
	}

	public void setStage(Stage s) {
		mainStage = s;
	}

	public ClientUi getClientUi() {
		return this.clientUi;
	}

	
	public <T> T loadScreen(String fxmlPath, javafx.event.ActionEvent event,ClientUi c) {
		try {
			

			FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlPath + ".fxml"));
			Parent root = loader.load();
			
			//add init  data here

			T controller = loader.getController();
			System.out.println(controller.getClass().toString());
			if (controller instanceof MainNavigator) {
				BaseController base = (BaseController) controller;
				
				base.setClientUi(c);
				base.setMainNavigator(this);
			}
			
			
			if (controller instanceof MessageListener) {
	            c.removeAllListeners(); // ניקוי טוטאלי של רשימת המאזינים
	            c.addListener((MessageListener<Object>) controller);
	            System.out.println("DEBUG: Cleared old listeners and added new listener: " + controller.getClass().getSimpleName());
	        }


			System.out.println(this.clientUi);
			
			// 1. ניסיון לחלץ את ה-Stage מה-event, אם לא הצלחנו - ניקח את ה-mainStage הקיים
			Stage stage = (event != null && event.getSource() instanceof javafx.scene.Node) 
			                ? (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow() 
			                : this.mainStage;

			// 2. בדיקת הגנה אחרונה
			if (stage == null) {
			    System.err.println("Error: Stage is null. Cannot load screen.");
			    return null;
			}

			// 3. עדכון ה-mainStage והצגת הסצנה
			this.mainStage = stage;
			stage.setScene(new Scene(root));
			stage.show();
		

			return controller; 

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error loading screen: " + fxmlPath);
			return null;
		}
		
	}
	
	public <T> T openPopup(String fxmlPath, String title, ClientUi c) {
	    try {
	        // 1. טעינת ה-FXML (הנתיב מותאם למבנה שבתמונה)
	        FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlPath + ".fxml"));
	        Parent root = loader.load();

	        T controller = loader.getController();

	        // 2. אתחול התלויות (כמו ב-loadScreen)
	        if (controller instanceof MainNavigator) {
	            BaseController base = (BaseController) controller;
	            base.setClientUi(c);
	            base.setMainNavigator(this); 
	        }

	        // 3. טיפול במאזינים - קריטי לפופאפ!
	        // אנחנו לא עושים removeAllListeners כי אנחנו רוצים שהחלון הראשי ימשיך לעבוד
	        if (controller instanceof MessageListener) {
	            c.addListener((MessageListener<Object>) controller);
	            System.out.println("DEBUG: Added Popup listener: " + controller.getClass().getSimpleName());
	        }

	        // 4. יצירת Stage חדש ונפרד
	        Stage popupStage = new Stage();
	        popupStage.setTitle(title);
	        popupStage.setScene(new Scene(root));

	        // 5. ניקוי המאזין כשהחלון נסגר (כדי שלא נשלח הודעות לחלון סגור)
	        popupStage.setOnHidden(e -> {
	            if (controller instanceof MessageListener) {
	                // הערה: וודא שיש לך מתודה removeListener ב-ClientUi שמקבלת מאזין ספציפי
	                // c.removeListener((MessageListener<Object>) controller); 
	                // אם אין לך, אתה יכול להשאיר את זה, או להוסיף פונקציה כזו.
	                System.out.println("Popup closed");
	            }
	        });

	        popupStage.show(); // מציג את החלון במקביל

	        return controller;

	    } catch (IOException e) {
	        e.printStackTrace();
	        System.err.println("Error loading popup: " + fxmlPath);
	        return null;
	    }
	}
}
