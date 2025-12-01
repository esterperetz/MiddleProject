package clientUi;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class OrderUi extends Application{
	  @Override
	    public void start(Stage primaryStage) throws Exception {
		  Parent root = FXMLLoader.load(getClass().getResource("/clientGui/orderUi.fxml"));

		  
	        Scene scene = new Scene(root);
	        primaryStage.setTitle("Orders Management");
	        primaryStage.setScene(scene);
	        primaryStage.show();	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
}

