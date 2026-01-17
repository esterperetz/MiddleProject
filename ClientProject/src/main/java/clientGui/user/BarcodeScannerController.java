package clientGui.user;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.UserLogic;
import entities.Customer;
import entities.CustomerType;
import entities.Response;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Controller for the Barcode Scanner simulation screen.
 * Simulates scanning a QR/Barcode to verify user presence/identity.
 */
public class BarcodeScannerController extends MainNavigator implements MessageListener<Object> {

    @FXML
    private ImageView cameraView;
    @FXML
    private TextField resultField;
    @FXML
    private Button scanBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Label statusLabel;

    private static BarcodeScannerController instance;
    private UserLogic userLogic;
    private CustomerType customerType;
    private Customer customer;
    private int subCode;

    /**
     * Initializes the controller with necessary data.
     * 
     * @param clientUi The client UI instance
     * @param type     The customer type
     * @param subcode  The subscriber code
     * @param cust     The customer object
     */
    public void initData(ClientUi clientUi, CustomerType type, int subcode, Customer cust) {
        this.clientUi = clientUi;
        this.customerType = type;
        this.customer = cust;
        instance = this; // Save current instance
        this.subCode = subcode;
        resultField.setText("");
        if (statusLabel != null)
            statusLabel.setText("Ready to scan");
    }

    @FXML
    void initialize() {
        userLogic = new UserLogic(clientUi);
        scanBtn.setOnAction(e -> startSimulationScan());
        cancelBtn.setOnAction(this::goBack);
    }

    /**
     * Simulates the scanning process with a delay.
     */
    private void startSimulationScan() {
        resultField.setText("Scanning...");
        resultField.setStyle("-fx-text-fill: black;");
        scanBtn.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        pause.setOnFinished(event -> {
            String scannedCode = "50023";

            resultField.setText(scannedCode);

            sendQrCheckToServer(scannedCode);
        });

        pause.play();
    }

    /**
     * Sends the scanned code to the server for verification.
     * 
     * @param code The scanned code
     */
    private void sendQrCheckToServer(String code) {
        if (statusLabel != null)
            statusLabel.setText("Verifying with server...");

        userLogic.CheckQRcode(code);
    }

    @Override
    public void onMessageReceive(Object msg) {
        if (!(msg instanceof Response))
            return;
        Response res = (Response) msg;

        // Ensure updates run on the GUI thread
        Platform.runLater(() -> {
            try {
                switch (res.getResource()) {
                    case CUSTOMER:
                        handleUserResponse(res);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles responses related to user operations.
     */
    private void handleUserResponse(Response res) {

        switch (res.getAction()) {

            case CHECK_QR_CODE:

                // Check if the scanner controller is currently open
                if (BarcodeScannerController.instance != null) {
                    boolean isSuccess;
                    String message;

                    if (res.getData() != null) {
                        isSuccess = true;
                        message = "Welcome!";
                        // Optional: Save the logged-in user
                        // UserLogic.setCurrentCustomer((Customer) res.getData());
                    } else {
                        isSuccess = false;
                        message = "Subscriber Code Not Found";
                    }

                }
                break;

            default:
                break;
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        super.loadScreen("user/SubscriberLogin", event, clientUi);
    }
}