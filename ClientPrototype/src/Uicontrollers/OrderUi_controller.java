package Uicontrollers;


import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import Entities.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DateStringConverter;
 

public class OrderUi_controller implements Initializable {

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
		DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));
		itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));

		// הדגמה
//		orderData.add(new Order(1111, new Date(), 5, 1234, 1, new Date()));
		orderTable.setItems(orderData);

		setupEditableColumns();
	}

	private void setupEditableColumns() {
		// עריכת תאריך
//        DateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
//        DateColumn.setOnEditCommit(event -> {
//            event.getRowValue().setDate_of_placing_order(event.getNewValue());
//        });
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

	// ❤️ הכפתור שהוספת לעדכון
	@FXML
	private void handleUpdateOrder() {
		int rowIndex = orderTable.getSelectionModel().getSelectedIndex();
		if (rowIndex < 0) {
			showAlert("No Selection", "Please select an order to update.", Alert.AlertType.WARNING);
			return;
		}

		// מתחיל עריכה על תא מספר האורחים בשורה המסומנת
		orderTable.edit(rowIndex, itemColumn);
			}

	private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
