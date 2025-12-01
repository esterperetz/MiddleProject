package clientGui;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
	private Label lblName;
	@FXML
	private Label lblSurname;
	@FXML
	private Label lblFaculty;
	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;
	@FXML
	private TextField txtSurname;

	@FXML
	private Button btnclose = null;

	@FXML
	private ComboBox<String> cmbFaculty;

	@FXML
	private Button btnSave;
	@FXML
	private Button btnUpdate = null;

	ObservableList<String> list;

	public void loadStudent(Order o1) {
		this.o = o1;

//		this.cmbFaculty.setValue(o.);
		this.txtId.setText("sdfsdf");
		this.txtName.setText("sdfsdf");
		this.txtSurname.setText("dfgdfg");
	}

	@FXML
	private void onUpdate(ActionEvent event)  {
		try {
			System.out.println("heree");
			
			
		}catch(Exception e) {
			System.out.println("ddddd");
		}
	}
	
//
//	    @FXML
//	    private void onSave(ActionEvent event) {
//	    	  
//	    }

	// creating list of Faculties
//	private void setFacultyComboBox() {
//		ArrayList<String> al = new ArrayList<String>();
//		al.add("ME");
//		al.add("IE");
//		al.add("SE");
//
//		list = FXCollections.observableArrayList(al);
//		cmbFaculty.setItems(list);
//	}

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
//		setFacultyComboBox();
	}
}
