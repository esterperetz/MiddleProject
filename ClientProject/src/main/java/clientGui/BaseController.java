package clientGui;

import clientGui.navigation.MainNavigator;

/**
 * Interface defining the base requirements for a GUI controller in this
 * application.
 * Ensures every controller can reference the main client UI and navigator.
 */
public interface BaseController {
	/**
	 * Sets the ClientUi instance.
	 * 
	 * @param clientUi The main client UI
	 */
	void setClientUi(ClientUi clientUi);

	/**
	 * Sets the MainNavigator instance for screen transitions.
	 * 
	 * @param navigator The navigator instance
	 */
	void setMainNavigator(MainNavigator navigator);
}
