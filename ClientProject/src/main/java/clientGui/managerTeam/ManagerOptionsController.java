package clientGui.managerTeam;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.OrderUi_controller;
import clientGui.reservation.ReservationController;
import clientGui.reservation.WaitingListController;
import clientGui.user.RegisterSubscriberController;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import client.MessageListener;

/**
 * Controller for the Manager's Dashboard (Options Screen).
 * <p>
 * This class handles: 1. Navigation to various manager sub-screens (Waiting
 * List, Orders, Registration). 2. Configuration of Standard Opening Hours. 3.
 * Management of Special Dates/Holidays (view, add, remove). 4. Permission-based
 * visibility of sensitive buttons (e.g., Reports).
 */
public class ManagerOptionsController extends MainNavigator implements Initializable, MessageListener<Object> {

	// --- Internal Fields ---
	/** Flag to store the user's permission level. */
	private boolean isManager;

	/** Data model for the special dates list view. */
	private ObservableList<String> specialDatesModel;

	// --- FXML UI Components ---

	/** Button to view reports - hidden for non-managers. */
	@FXML
	private Button btnViewReports;

	// --- Standard Opening Hours UI ---

	/** Input field for the standard opening time (e.g., "08:00"). */
	@FXML
	private TextField txtOpenTime;

	/** Input field for the standard closing time (e.g., "23:00"). */
	@FXML
	private TextField txtCloseTime;

	// --- Special Dates / Holidays UI ---

	/** Date picker for selecting a specific holiday or special event date. */
	@FXML
	private DatePicker dpSpecialDate;

	/** Input field for opening time on the special date. */
	@FXML
	private TextField txtSpecialOpen;

	/** Input field for closing time on the special date. */
	@FXML
	private TextField txtSpecialClose;

	/** List view to display the added special dates/hours. */
	@FXML
	private ListView<String> listSpecialDates;

	/** Label to display success or error messages to the user. */
	@FXML
	private Label lblHoursStatus;
	
	//add button to register a new Employee by Manager.

	/**
	 * Called to initialize the controller after the FXML file has been loaded. Sets
	 * up permissions, loads initial data, and initializes the lists.
	 *
	 * @param location  The location used to resolve relative paths for the root
	 *                  object.
	 * @param resources The resources used to localize the root object.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		specialDatesModel = FXCollections.observableArrayList();
		listSpecialDates.setItems(specialDatesModel);

		// הסתרת כפתורים כברירת מחדל
		//btnViewReports.setVisible(false);
		//btnViewReports.setManaged(false);
		/*
		 * // --- 1. Permission Check --- // TODO: Replace with real logic (e.g.,
		 * ClientUI.currentUser.getRole().equals("MANAGER")) isManager = true;
		 * 
		 * if (isManager) { // Reveal the Reports button if the user has the correct
		 * permissions btnViewReports.setVisible(true); btnViewReports.setManaged(true);
		 * }
		 * 
		 * // --- 2. Load Standard Hours --- // Pre-fill the text fields with the
		 * current hours from the server loadStandardHours();
		 * 
		 * // --- 3. Initialize Special Dates List --- // Create the observable list and
		 * bind it to the ListView specialDatesModel =
		 * FXCollections.observableArrayList();
		 * listSpecialDates.setItems(specialDatesModel);
		 * 
		 * // Load Mock Data (Simulating existing data from server) // Example: On
		 * Christmas, the restaurant opens late.
		 * specialDatesModel.add("25/12/2025: 18:00 - 02:00 (Holiday)");
		 */
	}

	/**
	 * Initializes the controller with the connection to the server.
	 * 
	 * @param clientUi The connection instance.
	 */
	public void initData(ClientUi clientUi,boolean isManager) {
		this.clientUi = clientUi;

		// 3. הגדרת הרשאות (כרגע hardcoded, בהמשך תביא מהמשתמש המחובר)
		//isManager = true; // בהמשך זה יגיע מ-User
		if(isManager)
		{
			this.isManager=true;
			btnViewReports.setVisible(true);
			btnViewReports.setManaged(true);
		}
		else {
			this.isManager=false;
			btnViewReports.setVisible(false);
			btnViewReports.setManaged(false);
		}
//		// הצגת הכפתור אם צריך
//		//if (this.isManager) {
//			//btnViewReports.setVisible(true);
//			//btnViewReports.setManaged(true);
//		} else {
//			btnViewReports.setVisible(false);
//			btnViewReports.setManaged(false);
//		}
		if (this.clientUi == null) {
			System.err.println("Error: ClientUi is null in ManagerOptionsController!");
			return;
		}
//		this.clientUi.addListener(this);

		// 4. עכשיו בטוח לקרוא לשרת כי clientUi קיים
		loadStandardHours();

		// כאן גם תוסיף בהמשך: loadSpecialDates();
		// הערה: את בדיקת isManager השארנו כרגע ב-initialize כמו שביקשת
	}
	
	@FXML
	public void signUpRepresentative(ActionEvent event) {
		System.out.println("Navigating to Sign Up screen...");
		// טעינת מסך ההרשמה הקיים בפרויקט
		super.loadScreen("user/RegisterSubscriber", event, clientUi);
	

	}

	/**
	 * Loads the current standard opening hours from the server/database. Currently
	 * uses mock data.
	 */
	private void loadStandardHours() {
		// TODO: Replace with real server call, e.g., String[] hours =
		// ClientUI.chat.getStandardHours();

		// Safety check to ensure FXML injection worked
		if (clientUi != null) {
			if (txtOpenTime != null && txtCloseTime != null) {
				txtOpenTime.setText("08:00");
				txtCloseTime.setText("23:00");
			}
		}
	}

	/**
	 * Action handler to save the standard weekly opening hours. Triggered by the
	 * "Save" button in the standard hours section.
	 *
	 * @param event The ActionEvent triggered by the button click.
	 */
	@FXML
	void saveStandardHoursBtn(ActionEvent event) {
		String openTime = txtOpenTime.getText();
		String closeTime = txtCloseTime.getText();

		// Basic Validation: Ensure fields are not empty
		if (openTime.isEmpty() || closeTime.isEmpty()) {
			setStatus("Missing standard hours!", true);
			return;
		}

		// TODO: Send update to server
		// Example: ClientUI.chat.updateRestaurantHours(openTime, closeTime);

		System.out.println("Standard Hours updated -> Open: " + openTime + ", Close: " + closeTime);
		setStatus("Standard hours saved successfully!", false);
	}

	/**
	 * Adds a new special date/holiday to the list. Triggered by the "+" button.
	 *
	 * @param event The ActionEvent triggered by the button click.
	 */
	@FXML
	void addSpecialDateBtn(ActionEvent event) {
		LocalDate date = dpSpecialDate.getValue();
		String start = txtSpecialOpen.getText();
		String end = txtSpecialClose.getText();

		// Validation: Ensure all fields (Date, Start Time, End Time) are filled
		if (date == null || start.isEmpty() || end.isEmpty()) {
			setStatus("Date and times are required for special entry", true);
			return;
		}

		// Format the date for display (e.g., "dd/MM/yyyy")
		String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		String listEntry = String.format("%s: %s - %s", dateStr, start, end);

		// Add to the local list model (updates UI automatically)
		specialDatesModel.add(listEntry);

		// TODO: Send the new special date to the server
		// ClientUI.chat.addSpecialDate(date, start, end);

		// Clear input fields for the next entry
		dpSpecialDate.setValue(null);
		txtSpecialOpen.clear();
		txtSpecialClose.clear();

		setStatus("Special date added!", false);
	}

	/**
	 * Removes the selected special date from the list. Triggered by the "Remove
	 * Selected" button.
	 *
	 * @param event The ActionEvent triggered by the button click.
	 */
	@FXML
	void removeSpecialDateBtn(ActionEvent event) {
		// Get the item currently selected by the user
		String selectedItem = listSpecialDates.getSelectionModel().getSelectedItem();

		if (selectedItem != null) {
			// Remove from the model
			specialDatesModel.remove(selectedItem);

			// TODO: Notify server to delete this entry
			// ClientUI.chat.removeSpecialDate(selectedItem);

			setStatus("Entry removed", false);
		} else {
			setStatus("Select an item to remove", true);
		}
	}

	/**
	 * Helper method to display status messages to the user. Changes the text color
	 * based on whether it is an error or success.
	 *
	 * @param msg     The message to display.
	 * @param isError True if it is an error (Red), False if success (Green).
	 */
	private void setStatus(String msg, boolean isError) {
		lblHoursStatus.setText(msg);
		// Set color: Red for error, Green for success
		lblHoursStatus.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
	}

	// --- Navigation Methods ---

	/**
	 * Navigates to the Waiting List management screen.
	 */
	@FXML
	void goToWaitingListBtn(ActionEvent event) {
		
			WaitingListController waiting_list=super.loadScreen("reservation/WaitingList", event,clientUi);
			waiting_list.initData(this.clientUi, this.isManager);
	}

	/**
	 * Navigates to the Order Details / Table management screen.
	 */
	@FXML
	void goToOrderDetailsBtn(ActionEvent event) {
		// Passing nulls as this is a general view, not specific to one order yet
		// BaseController controller =
		// MainNavigator.loadScreen("reservation/ReservationScreen", clientUi);
		// ((ReservationController) controller).setData(true, null, null, null);
		// clientGui.reservation.OrderUi_controller controller =
		// MainNavigator.loadScreen("reservation/orderUi", clientUi);
//		clientUi.removeListener(this);
		OrderUi_controller controller = super.loadScreen("reservation/orderUi", event,clientUi);
		if (controller != null) {
			//////////////////////////////////////////////////////////////check if button disapear
			//controller.initData(clientUi, clientUi.getIp());
			controller.initData(this.isManager);
			
		} else {
			System.err.println("Failed to load OrderUi. Check FXML path name.");
		}
	}

	/**
	 * Navigates to the Subscriber Registration screen.
	 */
	@FXML
	void goToRegisterSubscriberBtn(ActionEvent event) {
		RegisterSubscriberController r=super.loadScreen("user/RegisterSubscriber", event,clientUi);
		if(r!=null)
		{
			r.initData(this.clientUi,this.isManager);
		}
		else {
			System.out.println("Error: the object RegisterSubscriberController is null");
		}
		
	}

	/**
	 * Navigates to the Reports Dashboard. Only accessible if the user has Manager
	 * permissions.
	 */
	@FXML
	void goToReportsBtn(ActionEvent event) {
		// MainNavigator.loadScene("manager/ReportsScreen");
		System.out.println("Navigate to Reports Screen...");
		ReportsController r=super.loadScreen("managerTeam/ReportsScreen", event,clientUi);
		if(r!=null)
		{
			r.initData(this.clientUi,this.isManager);
		}
		else {
			System.out.println("Error: ReportsController is null!!");
		}

	}

	/**
	 * Logs out or returns to the previous menu.
	 */
	@FXML
	void goBackBtn(ActionEvent event) {
		// Logic for signing out or returning to the main selection screen
		System.out.println("Going back / Signing out...");
		super.loadScreen("navigation/SelectionScreen", event,clientUi);

	}

	@Override
	public void onMessageReceive(Object msg) {
		System.out.println("Manager Controller received: " + msg.toString());
	}

	
}