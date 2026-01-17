package server.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Server Application.
 * This class extends the JavaFX Application class and is responsible for
 * launching the initial login/configuration window.
 */
public class ServerApp extends Application {

    /**
     * Starts the JavaFX application.
     * Delegates the creation of the primary stage to the ServerLoginController.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     * @throws Exception if an error occurs during the loading of the login screen.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Loads and displays the server login/configuration screen
        ServerLoginController loginController = new ServerLoginController();
        loginController.start(primaryStage);
    }

    /**
     * The main method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be launched
     * through deployment artifacts, e.g., in IDEs with limited FX support.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}