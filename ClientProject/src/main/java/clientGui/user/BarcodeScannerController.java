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

public class BarcodeScannerController extends MainNavigator implements MessageListener<Object> {

	// ... (כל המשתנים שלך נשארים אותו דבר) ...
    @FXML private ImageView cameraView;
    @FXML private TextField resultField;
    @FXML private Button scanBtn;
    @FXML private Button cancelBtn;
    @FXML private Label statusLabel;
    
    private static BarcodeScannerController instance;
    private UserLogic userLogic;
    private CustomerType customerType;
    private Customer customer;
	private int subCode;

    public void initData(ClientUi clientUi, CustomerType type,int subcode, Customer cust) {
        this.clientUi = clientUi;
        this.customerType = type;
        this.customer = cust;
        instance = this; // שומרים את המופע הנוכחי
        this.subCode = subCode;
        resultField.setText("");
        if(statusLabel != null) statusLabel.setText("Ready to scan");
    }

    @FXML
    void initialize() {
    	userLogic = new UserLogic(clientUi);
        scanBtn.setOnAction(e -> startSimulationScan());
        cancelBtn.setOnAction(this::goBack);
    }

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

    
    private void sendQrCheckToServer(String code) {
        if(statusLabel != null) statusLabel.setText("Verifying with server...");
        
        userLogic.CheckQRcode(code);
    }

    @Override
    public void onMessageReceive(Object msg) {
        if (!(msg instanceof Response))
            return;
        Response res = (Response) msg;

        // ה-runLater מבטיח שכל העדכונים מכאן והלאה ירוצו על ה-Thread של ה-GUI
        Platform.runLater(() -> {
            try { 
                switch (res.getResource()) {
                    case CUSTOMER:
                        handleUserResponse(res);
                        break;
                    // case ORDER: ...
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * פונקציה המטפלת בכל התשובות הקשורות למשתמשים
     */
    private void handleUserResponse(Response res) {
    	
        switch (res.getAction()) { 
            
            case CHECK_QR_CODE: // השם של הפעולה שהגדרת בשרת
                
                // בדיקה אם הקונטרולר של הסורק פתוח כרגע
                if (BarcodeScannerController.instance != null) {
                    boolean isSuccess;
                    String message;

                    // נניח שאם זה הצליח, ה-Data מכיל אובייקט Customer או null אם נכשל
                    // או שיש שדה res.isSuccess()
                    if (res.getData() != null) { // או res.isSuccess()
                        isSuccess = true;
                        message = "Welcome!";
                        // אופציונלי: שמירת המשתמש שהתחבר
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