package clientGui.navigation;

import clientGui.BaseController;
import clientGui.ClientUi;
import client.MessageListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Base class for all controllers that handle screen navigation.
 * Manages loading FXML screens, injecting dependencies (ClientUi), and handling
 * stage transitions.
 */
public class MainNavigator implements BaseController {

	private Stage mainStage;
	protected ClientUi clientUi;

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
	 * Loads a new screen (FXML) into the current stage or a new one.
	 * Initializes the controller with the ClientUi instance and sets up listeners.
	 * 
	 * @param <T>      The type of the controller
	 * @param fxmlPath The relative path to the FXML file (without .fxml extension)
	 * @param event    The event that triggered this navigation (used to find the
	 *                 current stage)
	 * @param c        The ClientUi instance
	 * @return The controller of the loaded screen
	 */
	@SuppressWarnings("unchecked")
	public <T> T loadScreen(String fxmlPath, javafx.event.ActionEvent event, ClientUi c) {
		try {

			FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlPath + ".fxml"));
			Parent root = loader.load();

			// Get and setup the controller
			T controller = loader.getController();
			System.out.println(controller.getClass().toString());
			if (controller instanceof MainNavigator) {
				BaseController base = (BaseController) controller;

				base.setClientUi(c);
				base.setMainNavigator(this);
			}

			// Handle MessageListener registration
			if (controller instanceof MessageListener) {
				c.removeAllListeners();
				c.addListener((MessageListener<Object>) controller);
				System.out.println("DEBUG: Cleared old listeners and added new listener: "
						+ controller.getClass().getSimpleName());
			}

			System.out.println(this.clientUi);

			// Determine target stage
			Stage stage = (event != null && event.getSource() instanceof javafx.scene.Node)
					? (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()
					: this.mainStage;

			if (stage == null) {
				System.err.println("Error: Stage is null. Cannot load screen.");
				return null;
			}

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

	/**
	 * Opens a new popup window with the specified FXML.
	 * 
	 * @param <T>      The type of the controller
	 * @param fxmlPath The relative path to the FXML file
	 * @param title    The title of the popup window
	 * @param c        The ClientUi instance
	 * @return The controller of the loaded popup
	 */
	@SuppressWarnings("unchecked")
	public <T> T openPopup(String fxmlPath, String title, ClientUi c) {
		try {

			FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlPath + ".fxml"));
			Parent root = loader.load();

			T controller = loader.getController();

			if (controller instanceof MainNavigator) {
				BaseController base = (BaseController) controller;
				base.setClientUi(c);
				base.setMainNavigator(this);
			}

			if (controller instanceof MessageListener) {
				c.addListener((MessageListener<Object>) controller);
				System.out.println("DEBUG: Added Popup listener: " + controller.getClass().getSimpleName());
			}

			// Create a new Stage for the popup
			Stage popupStage = new Stage();
			popupStage.setTitle(title);
			popupStage.setScene(new Scene(root));

			// Clean up listeners when popup closes
			popupStage.setOnHidden(e -> {
				if (controller instanceof MessageListener) {
					c.removeListener((MessageListener<Object>) controller);
					System.out.println("Popup closed and listener removed");
				}
			});

			popupStage.show();

			return controller;

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error loading popup: " + fxmlPath);
			return null;
		}
	}
}
