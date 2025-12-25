package clientGui.reservation;
import clientGui.navigation.MainNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class OrderUi extends Application{
	  /**
	 *Starts the JavaFX window and loads the first screen (login screen).
     *
     * @param primaryStage The main window of the application.
     * @throws Exception If the FXML file cannot be loaded.
	 */
	@Override
	    public void start(Stage primaryStage) throws Exception {
	
		  Parent root = FXMLLoader.load(getClass().getResource("/clientGui/logInServer.fxml"));

		  	
	        Scene scene = new Scene(root);
	        primaryStage.setTitle("Orders Management");
	        primaryStage.setScene(scene);
	        primaryStage.show();	
	        }

	    public static void main(String[] args) {
	        launch(args);
	    }
}

