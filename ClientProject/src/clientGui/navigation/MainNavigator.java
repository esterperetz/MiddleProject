package clientGui.navigation;

import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.reservation.ReservationController;
import Entities.Alarm;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class MainNavigator implements BaseController {

	private Stage mainStage;
	protected ClientUi clientUi;// importent!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	// שמירת ה-Stage הראשי בפעם הראשונה שהאפליקציה עולה
	// public MainNavigator(Stage stage) {
	// mainStage = stage;

	// }
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

	/**
	 * פונקציה גנרית לטעינת כל מסך
	 * 
	 * @param fxmlPath הנתיב לקובץ ה-FXML (למשל: "reservation/OrderUi")
	 * @param clientUi החיבור לשרת
	 * @return המחלקה של הקונטרולר (כדי שנוכל להשתמש בה אם צריך)
	 */
	public <T> T loadScreen(String fxmlPath, javafx.event.ActionEvent event,ClientUi c) {
		try {
			FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlPath + ".fxml"));
			Parent root = loader.load();

			// קבלת הקונטרולר (אנחנו לא יודעים איזה סוג הוא, וזה בסדר)
			T controller = loader.getController();

			// הקסם: אם הקונטרולר הזה יודע לקבל ClientUi, ניתן לו אותו!
			if (controller instanceof BaseController) {
				BaseController base = (BaseController) controller;

				// 1. הקונטרולר מקבל את ה-ClientUi
				base.setClientUi(c);

				// 2. הקונטרולר מקבל את ה-Navigator הזה עצמו!
				// כך הוא יוכל לקרוא ל-navigator.loadScreen(...) בעתיד
				base.setMainNavigator(this);
			}
			Stage stage;
			System.out.println(this.clientUi);
			if (event != null) {
				// אופציה 1: חילוץ החלון מתוך הכפתור שנלחץ
				javafx.scene.Node source = (javafx.scene.Node) event.getSource();
				stage = (Stage) source.getScene().getWindow();
			} else {
				// אופציה 2: גיבוי למקרה שלא העברנו event (למשל בהתחלה)
				stage = this.mainStage;
			}

			// עדכון ה-mainStage שלנו שיהיה מסונכרן
			this.mainStage = stage;
			
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();

			return controller; // מחזירים את הקונטרולר למקרה שצריך עוד אתחולים

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error loading screen: " + fxmlPath);
			return null;
		}
	}

	/**
	 * פונקציה לטעינת מסך חדש
	 * 
	 * @param fxmlFileName שם קובץ ה-FXML (ללא סיומת .fxml)
	 */
	//public void loadScene(String fxmlFileName) {
		//try {
			//loadScreen(fxmlFileName, clientUi.getInstance(),);
		//} catch (Exception e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
	//}

//    
//    public static void loadReservationScreen(boolean isSubscriber, String phone, String email, String name) {
//        try {
//            // 1. טעינת ה-Loader
//            FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/reservation/ReservationScreen.fxml"));
//            Parent root = loader.load();
//
//            // 2. קבלת הקונטרולר מתוך ה-Loader
//            ReservationController controller = loader.getController();
//            
//            // 3. הזרקת הנתונים לקונטרולר
//            controller.initData(isSubscriber, phone, email, name);
//
//            // 4. הצגת המסך
//            Scene scene = new Scene(root);
//            mainStage.setScene(scene);
//            mainStage.show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    public static void loadOrderTableScreen(boolean isSubscriber, String phone, String email, String name)
//    {
//    	try {
//	        FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/reservation/orderUi.fxml"));
//	        Parent root = loader.load();
//
//	        clientGui.reservation.OrderUi_controller controller = loader.getController();
//
//	         
//	        controller.initData(ClientUi.getInstance(), ClientUi.getInstance().getIp());  
//
//	        
//	        Scene scene = new Scene(root); 
//	        //Stage stage = (Stage) usernameField.getScene().getWindow();
//	        mainStage.setScene(scene);
//	        mainStage.show();
//
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        System.out.println("Error loading OrderUi: " + e.getMessage());
//	    }
//    }
//    
	// ask liel about this function(ido)
	public static void showAlert(String header_text, String context_text, Alert.AlertType type) {

		Alarm.showAlert(header_text, context_text, type);
	}
}
