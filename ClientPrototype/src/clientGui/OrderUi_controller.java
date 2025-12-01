package clientGui;



import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;


import Entities.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DateStringConverter;

public class OrderUi_controller  implements Initializable {

	@FXML
	private TableView<Order> orderTable;
	@FXML
	private TableColumn<Order, Integer> Order_numberColumn;
	@FXML
	private TableColumn<Order, Date> DateColumn;
	@FXML
	private TableColumn<Order, Integer> itemColumn;

	private ObservableList<Order> orderData = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Order_numberColumn.setCellValueFactory(new PropertyValueFactory<>("order_number"));
		itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));

		// --- Date column formatting ---
		DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// Display formatted date in the table (dd/MM/yyyy)
//	    DateColumn.setCellFactory(column -> new TableCell<Order, Date>() {
//	        @Override
//	        protected void updateItem(Date item, boolean empty) {
//	            super.updateItem(item, empty);
//            
//	            if (empty || item == null) {
//	                setText(null);
//	            } else {
//	                setText(item.format(formatter)); // <-- format to dd/MM/yyyy
//	            }
//	        }
//	    });

		// Set the data into the table
		orderTable.setItems(orderData);

		setupEditableColumns();
	}

	private void setupEditableColumns() {
		// עריכת תאריך
		DateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
		DateColumn.setOnEditCommit(event -> {
			event.getRowValue().setDate_of_placing_order(event.getNewValue());
		});
//
//        // עריכת Guests
//        itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
//        itemColumn.setOnEditCommit(event -> {
//            event.getRowValue().setNumber_of_guests(event.getNewValue());
//        });
		itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

		itemColumn.setOnEditCommit(event -> {
			Integer newValue = event.getNewValue();
			if (newValue != null) {
				event.getRowValue().setNumber_of_guests(newValue);
			}
		});
	}

	@FXML
	private void handleAddOrder() {
		int newId = orderData.isEmpty() ? 1 : orderData.get(orderData.size() - 1).getOrder_number() + 1;

		Order newOrder = new Order(newId, new Date(), 0, 0, 1, new Date());
		orderData.add(newOrder);

		orderTable.getSelectionModel().select(newOrder);
		orderTable.scrollTo(newOrder);
	}

	@FXML
	private void handleDeleteOrder() {

		Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
		if (selectedOrder != null) {
			orderData.remove(selectedOrder);
		} else {
			showAlert("No Selection", "Please select an order to delete.", Alert.AlertType.WARNING);
		}

	}

	@FXML
	private void handleUpdateOrder(ActionEvent event) throws Exception{

		
		int rowIndex = orderTable.getSelectionModel().getSelectedIndex();
		if (rowIndex < 0) {
			showAlert("No Selection", "Please select an order to update.", Alert.AlertType.WARNING);
			return;
		}
		System.out.println("sdsdfdsf");
		FXMLLoader loader = new FXMLLoader();
		((Node) event.getSource()).getScene().getWindow().hide(); // hiding primary window
		Stage primaryStage = new Stage();
		loader = new FXMLLoader(getClass().getResource("/clientGui/updateOrder.fxml"));
		Pane root = loader.load();
	    Scene scene = new Scene(root);

	    primaryStage.setTitle("Update Order");
	    primaryStage.setScene(scene);
	    primaryStage.show();

		
	}


	private void showAlert(String title, String content, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}

