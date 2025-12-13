package server.gui;

import javafx.application.Application; 
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene; 
import javafx.fxml.FXMLLoader; 
import DBConnection.DBConnection; 
import server.controller.ServerController; 

public class ServerApp extends Application {
	 
    //Starts the JavaFX application by displaying the server login window.
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