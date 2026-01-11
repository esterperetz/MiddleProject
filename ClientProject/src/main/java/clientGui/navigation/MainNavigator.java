package clientGui.navigation;

import clientGui.BaseController;
import clientGui.ClientUi;
import client.MessageListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

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

	
	@SuppressWarnings("unchecked")
	public <T> T loadScreen(String fxmlPath, javafx.event.ActionEvent event,ClientUi c) {
		try {
			
			FXMLLoader loader = new FXMLLoader(MainNavigator.class.getResource("/clientGui/" + fxmlPath + ".fxml"));
			Parent root = loader.load();
			
			//add init  data here

			T controller = loader.getController();
			System.out.println(controller.getClass().toString());
			if (controller instanceof MainNavigator) {
				BaseController base = (BaseController) controller;
				
				base.setClientUi(c);
				base.setMainNavigator(this);
			}
			
			
			if (controller instanceof MessageListener) {
	            c.removeAllListeners(); 
	            c.addListener((MessageListener<Object>) controller);
	            System.out.println("DEBUG: Cleared old listeners and added new listener: " + controller.getClass().getSimpleName());
	        }


			System.out.println(this.clientUi);
			
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

	        // 4. יצירת Stage חדש ונפרד
	        Stage popupStage = new Stage();
	        popupStage.setTitle(title);
	        popupStage.setScene(new Scene(root));

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
