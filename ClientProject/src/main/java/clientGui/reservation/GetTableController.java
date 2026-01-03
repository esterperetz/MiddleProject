package clientGui.reservation;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import entities.CustomerType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GetTableController extends MainNavigator implements MessageListener<Object> {
    @FXML
    private TextField txtOrderId;

    @FXML
    private Label lblResult;
    private Integer subscriberId;

    private CustomerType isSubscriber;

    public void initData(ClientUi clientUi, CustomerType isSubscriberStatus, Integer subId) {
        this.clientUi = clientUi;
        this.isSubscriber = isSubscriberStatus;
        this.subscriberId = subId;
        System.out.println("Loaded options for subscriber: " + subId);
    }

    /**
     * Triggered when the "Check Table" button is clicked.
     */
    @FXML
    void checkTableAvailability(ActionEvent event) {
        String orderId = txtOrderId.getText();

        // 1. Input Validation
        // add if the code incorrect
        if (orderId == null || orderId.trim().isEmpty()) {
            lblResult.setText("Please enter a valid Order ID.");
            lblResult.setStyle("-fx-text-fill: #ff6b6b;"); // Red color for error
            return;
        }

        // 2. Server Simulation (Replace this with real server call later)
        // Example: int tableNumber = ClientUI.chat.getTableRequest(orderId);
        // int tableNumber = mockServerCheck(orderId);

        // 3. Process Result
        // if (tableNumber != -1) {
        // Success: Table is free
        // lblResult.setText("Table is ready! Table Number: " + tableNumber);
        // lblResult.setStyle("-fx-text-fill: #51cf66; -fx-font-size: 16px;
        // -fx-font-weight: bold;"); // Green color
        // } else {
        // Failure: Order not found or table occupied
        // lblResult.setText("Order not found or table is not ready yet.");
        // lblResult.setStyle("-fx-text-fill: #ff6b6b;"); // Red color
        // }

    }

    @FXML
    void openLostCodePopup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/reservation/ForgetCode.fxml"));
            Parent root = loader.load();

            // Get the controller and pass clientUi
            ForgetCodeController controller = loader.getController();
            controller.setClientUi(this.clientUi);

            Stage popupStage = new Stage();

            // הגדרה קריטית: זה מה שהופך את החלון ל-Modal (חוסם את החלון הראשי)
            popupStage.initModality(Modality.APPLICATION_MODAL);

            // קישור לחלון האב (כדי שהפופ-אפ יפתח במרכז יחסי אליו אם נרצה)
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            popupStage.setTitle("Recover Reservation Code");
            popupStage.setScene(new Scene(root));

            // אופציונלי: ביטול אפשרות הגדלת החלון כדי שישמור על עיצוב נקי
            popupStage.setResizable(false);

            // פקודה שמחכה עד שהחלון ייסגר לפני שחוזרים לקוד
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Triggered when the "Back" button is clicked.
     * Navigates back to the main reservation menu.
     */
    @FXML
    void goBack(ActionEvent event) {
        // MainNavigator.loadScene("user/SubscriberOption");
        SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, isSubscriber, subscriberId);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
    }

    @Override
    public void onMessageReceive(Object msg) {
        // TODO Auto-generated method stub

    }

    /**
     * A temporary mock function to simulate server response.
     * 
     * @param id The Order ID
     * @return Table number if found/ready, or -1 if not found.
     * 
     *         private int mockServerCheck(String id) {
     *         // Simulation logic
     *         if (id.equals("100")) return 5;
     *         if (id.equals("123")) return 10;
     *         return -1; // Not found
     *         }
     */
}