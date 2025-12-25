package clientGui.navigation;

import clientGui.ClientUi;
import javafx.event.ActionEvent;

public class MainNavigatorSubscriber extends MainNavigator {
	private Integer subscriber_id;
	
	
	public <T> T loadScreenWithSubscriber(String fxmlPath, ActionEvent event, ClientUi c, Integer subscriber_id) {
		this.setSubscriber_id(subscriber_id);
		return super.loadScreen(fxmlPath, event, c);
	}


	public Integer getSubscriber_id() {
		return subscriber_id;
	}


	public void setSubscriber_id(Integer subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

}
