package clientGui.navigation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for the Client UI.
 * Handles loading custom fonts, setting up the primary stage, and launching the
 * initial login screen.
 */
public class NavigatorUi extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Load Luxury Fonts
		try {
			// Using getResourceAsStream for safety. Only if files exist.
			javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/clientGui/fonts/PlayfairDisplay-Bold.ttf"),
					36);
			javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/clientGui/fonts/Montserrat-Regular.ttf"),
					16);
			javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/clientGui/fonts/Montserrat-Bold.ttf"), 16);
		} catch (Exception e) {
			System.err.println("Warning: Luxury fonts not found in /clientGui/fonts/. Using system defaults.");
		}

		String fxmlPath = "/clientGui/logInServer.fxml";
		java.net.URL location = getClass().getResource(fxmlPath);

		// Check if the FXML file exists before attempting to load
		if (location == null) {
			System.err.println("ERROR: Could not find FXML at: " + fxmlPath);
			// Backup attempt
			location = getClass().getResource("/clientGui/logInServer.fxml");
		}

		if (location == null) {
			throw new IllegalStateException("FATAL: FXML file not found! Check project structure.");
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/logInServer.fxml"));
			Parent root = loader.load();

			Scene scene = new Scene(root);

			// Set up the primary stage
			primaryStage.setScene(scene);
			primaryStage.setTitle("BISTRO System");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace(); // Keep stack trace for debugging
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
