package server.gui;
import javafx.application.Application; // עבור extends Application
import javafx.stage.Stage;             // עבור Stage primaryStage
import javafx.scene.Parent;
import javafx.scene.Scene;             // עבור Scene scene
import javafx.fxml.FXMLLoader;         // עבור FXMLLoader loader


// ייבוא המחלקות שנוצרו בפרויקט שלך:
import DBConnection.DBConnection;          // עבור ServerModel
import server.controller.ServerController;  // עבור ServerController
// עבור DBConnectionFactory/DBConnection
// נניח ש-DBConnectionFactory היא מחלקה באחת החבילות שראינו קודם:
public class ServerApp extends Application {
	 /**
     * The start() method is called automatically when the program begins.
     * Its job is to:
     * 1. Load the login screen (FXML file)
     * 2. Create a window for it
     * 3. Display the window to the user
     * This is the first function that runs in a JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
    	// שינוי הנתיב: שימוש ב-ClassLoader כדי לחפש את הקובץ מה-Root
    	/*
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("connections_to_server.fxml"));    	
        Scene scene = new Scene(loader.load());
        ServerViewController viewController = loader.getController();
        //DBConnection model = new DBConnection("root","159357","mid_project_prototype");
        DBConnection model = new DBConnection("root","1234","prototype");
        ServerController server = new ServerController(5555, model, viewController);
        server.listen();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.show();

        viewController.log("Server listening on port 5555!");
        */
    	//ServerLoginController loginController = new ServerLoginController();
        //loginController.start(primaryStage);
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
