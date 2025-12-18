package clientGui.reservation;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import Entities.Alarm;
import Entities.Order;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
 * * Controller for the "Update Order" window. Shows the selected order data and
 * sends the updated order to the server.
 */
public class UpdateOrder extends MainNavigator implements Initializable {

	private Order o;
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtName1;

	private OrderUi_controller mainController; // Field to hold the main controller reference
	private OrderLogic ol;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Initialization is done in initData
	}

	/**
	 * Initializes data, OrderLogic, and the main controller reference.
	 * 
	 * @param order          The Order object to be updated.
	 * @param orderLogic     The logic object for server communication.
	 * @param mainController The reference to the main UI controller for data
	 *                       refresh.
	 */
	public void initData(Order order, OrderLogic orderLogic, OrderUi_controller mainController) { // FIXED SIGNATURE
		this.o = order;
		this.ol = orderLogic;
		this.mainController = mainController; // Store main controller reference

		txtId.setText(String.valueOf(o.getOrder_number()));
		txtName.setText(String.valueOf(o.getNumber_of_guests()));
		txtId.setEditable(false);
		if (o.getOrder_date() != null) {
			txtName1.setText(dateFormat.format(o.getOrder_date()));
		}
	}

	public void loadStudent(Order o1) {
		this.o = o1;
	}

	@FXML
	private void onUpdate(ActionEvent event) {
		try { // 1. פתיחת TRY
			String OrderNum = txtId.getText().trim();
			String Number_Of_Guests = txtName.getText().trim();
			String OrderDate = txtName1.getText();

			Date date = dateFormat.parse(OrderDate);

			if (OrderNum.isEmpty()) {
				String header = "Input Error";
				String context = "Please Enter Order ID (This field is now locked).";
				Alarm.showAlert(header, context, Alert.AlertType.ERROR);
			} else { // פתיחת ELSE
				// יצירת האובייקט
				Order updatedOrder = new Order(
						Integer.parseInt(OrderNum),
						date,
						Integer.parseInt(Number_Of_Guests),
						o.getConfirmation_code(),
						o.getSubscriber_id(),
						o.getDate_of_placing_order(),
						o.getIdentification_details(),
						o.getFull_name(),
						o.getTotal_price(),
						o.getStatus());

				if (ol != null) {
					ol.updateOrder(updatedOrder);
					OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, this.clientUi);
					
					if (controller != null) {
						controller.initData();
					} else {
						System.err.println("Error: Could not load OrderUi_controllerr.");
					}

					if (mainController != null) {
						mainController.refreshTableData();
					}
					
					// אופציונלי: סגירת החלון
					//((Node) event.getSource()).getScene().getWindow().hide(); 
				} // סגירת IF (ol != null)
			} // סגירת ELSE
			
		} catch (NumberFormatException | ParseException e) { // <--- כאן היה חסר סוגר סוגר של ה-TRY לפני ה-CATCH
			String header = "Format Error";
			String context = "Please verify that Order ID and Guests are valid numbers, and Date is 'yyyy-MM-dd'.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			e.printStackTrace();
		} catch (Exception e) {
			String header = "Error";
			String context = "An unexpected error occurred during update.";
			Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
			e.printStackTrace();
		}
	} 
		
	/*
    /**
     * @param title
    * @param content
    * @param type
    * Shows a simple alert dialog with a title, content and type.
     
    public void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
	*/
}