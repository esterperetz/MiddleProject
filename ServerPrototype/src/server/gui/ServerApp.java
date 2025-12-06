package server.gui;

import javafx.application.Application; 
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene; 
import javafx.fxml.FXMLLoader; 
import DBConnection.DBConnection; 
import server.controller.ServerController; 

public class ServerApp extends Application {
	 /**
     * Starts the JavaFX application by displaying the server login window.
     *
     * This method is called automatically by the JavaFX runtime after
     * the application has been launched using {@link #main(String[])}.
     *
     * @param primaryStage The primary Stage provided by the JavaFX runtime,
     *                     used here as the main window for the login screen.
     *
     * @throws Exception If loading the login UI or starting the controller fails.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
    	//loads login screenן
    	ServerLoginController loginController = new ServerLoginController();
    	loginController.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}