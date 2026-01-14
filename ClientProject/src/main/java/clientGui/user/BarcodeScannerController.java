package clientGui.user;

import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import entities.Customer;
import entities.CustomerType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class BarcodeScannerController extends MainNavigator {

    @FXML
    private ImageView cameraView;

    @FXML
    private TextField resultField;

    @FXML
    private Button scanBtn;

    @FXML
    private Button cancelBtn;
    
    private CustomerType customerType;
    private int subCode;
    private Customer customer;
    private ActionEvent previousEvent;

    public void initData(ClientUi clientUi, CustomerType type, int code, Customer cust) {
        this.clientUi = clientUi;
        this.customerType = type;
        this.subCode = code;
        this.customer = cust;
        
        resultField.setText("Ready to scan...");
    }

    @FXML
    void initialize() {
        scanBtn.setOnAction(e -> {
            resultField.setText("Simulating scan: 123456");
        });

        cancelBtn.setOnAction(this::goBack);
    }

    @FXML
    void goBack(ActionEvent event) {
        super.loadScreen("user/SubscriberLogin", event, clientUi);
    }
}