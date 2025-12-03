package clientGui;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;


import Entities.Order;
import clientLogic.OrderLogic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DateStringConverter;

public class UpdateOrder implements Initializable {
	
	private Order o;
	@FXML
	private Label Order_ID;
	@FXML
	private Label Number_Of_Guests;
	@FXML
	private Label Order_Date;
	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;
	@FXML
	private TextField txtName1;

	@FXML
	private Button btnclose = null;

	private ObservableList<Order> orderData = FXCollections.observableArrayList();

	@FXML
	private Button btnSave;
	@FXML
	private Button btnUpdate;
	
	private OrderLogic ol;
	
	private OrderUi_controller OrderController;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		   Platform.runLater(() -> {
		        // שליפת Stage של החלון הנוכחי
		        Stage stage = (Stage) txtId.getScene().getWindow();

		        stage.setOnCloseRequest(event -> {
		            try {
		                System.out.println("Returning to OrdersUI...");

		                Parent root = FXMLLoader.load(getClass().getResource("/clientGui/orderUi.fxml"));
		                Stage main = new Stage();
		                main.setScene(new Scene(root));
		                main.show();

		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        });
		    });
	}
	
	public UpdateOrder() {
		OrderController = new OrderUi_controller();
		ol = new OrderLogic(new ClientUi());
		
	}


	public void loadStudent(Order o1) {
		this.o = o1;


	}

	@FXML
	private void onUpdate(ActionEvent event)  {
		try {
			String OrderNum = txtId.getText().trim();
			String Number_Of_Guests = txtName.getText().trim();
			String OrderDate = txtName1.getText();
			System.out.println("heree");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(OrderDate);

			if (OrderNum.isEmpty()) {
				OrderController.showAlert("Input Error", "Please Enter Only Numbers", Alert.AlertType.ERROR);
			}
			else {

				o = new Order(Integer.parseInt(OrderNum), date, Integer.parseInt(Number_Of_Guests), 0, 0, date);
				ol.updateOrder(o);
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/orderUi.fxml"));
				Parent root = loader.load();
				Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				stage.setScene(new Scene(root));
				stage.show();
			}
			
			 
			
			
		}catch(Exception e) {
			if(e instanceof ParseException) {
				OrderController.showAlert("Input Error", "Please Enter valid date.", Alert.AlertType.ERROR);
			}
			e.printStackTrace();
		}
	}
	


	
	
}
