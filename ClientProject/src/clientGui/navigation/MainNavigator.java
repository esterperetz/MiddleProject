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

			if (controller instanceof BaseController) {
				BaseController base = (BaseController) controller;

				base.setClientUi(c);

				
				base.setMainNavigator(this);
			}
			Stage stage;
			System.out.println(this.clientUi);
			if (event != null) {
				javafx.scene.Node source = (javafx.scene.Node) event.getSource();
				stage = (Stage) source.getScene().getWindow();
			} else {
				stage = this.mainStage;
			}

			this.mainStage = stage;
			
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();

			return controller; 

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error loading screen: " + fxmlPath);
			return null;
		}
	}

	
	// ask liel about this function(ido)
	public static void showAlert(String header_text, String context_text, Alert.AlertType type) {

		Alarm.showAlert(header_text, context_text, type);
	}
}
