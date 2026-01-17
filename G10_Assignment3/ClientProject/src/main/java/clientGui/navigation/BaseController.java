package clientGui.navigation;

import clientGui.ClientUi;

/**
 * Interface for all controllers that need access to the main client UI and
 * navigator.
 * Ensures consistent handling of client connections and screen navigation
 * logic.
 */
public interface BaseController {

	/**
	 * Sets the ClientUi instance for communication with the server.
	 * 
	 * @param clientUi The ClientUi instance
	 */
	void setClientUi(ClientUi clientUi);

	/**
	 * Sets the MainNavigator instance for screen navigation.
	 * 
	 * @param navigator The MainNavigator instance
	 */
	void setMainNavigator(MainNavigator navigator);
}
