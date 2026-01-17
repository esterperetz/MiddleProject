package clientGui.reservation;

import entities.Customer;
import entities.CustomerType;
import entities.Order;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.navigation.MainNavigator;

/**
 * Controller for the Bill Screen.
 * Simulates and displays a bill for the customer's order, including item
 * details and price calculations.
 */
public class BillController extends MainNavigator implements Initializable, MessageListener<Object> {

    @FXML
    private ListView<String> itemsList;
    @FXML
    private Label lblOriginalPrice, lblDiscountAmount, lblFinalPrice;
    @FXML
    private HBox discountBox;
    @FXML
    private Button btnCancel;
    private Double totalPrice;

    // List of possible food items for simulation
    private final String[] menuItems = {
            "Margherita Pizza", "Pasta Carbonara", "Caesar Salad",
            "Beef Burger", "Coca Cola", "Red Wine", "Chocolate Souffle", "Fries"
    };
    private final double[] prices = { 55.0, 62.0, 45.0, 68.0, 12.0, 35.0, 38.0, 25.0 };
    private Order order;
    private Integer subId;
    private int tableId;
    private CustomerType customerType;
    private ActionEvent currentEvent;
    private Customer customer;

    /**
     * Initializes the controller.
     * Sets up a close request handler to disconnect the client when the window is
     * closed.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
                Stage stage = (Stage) btnCancel.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    clientUi.disconnectClient();

                });
            }
        });
    }

    /**
     * Initializes the controller with order and customer data.
     * 
     * @param order        The order to generate the bill for
     * @param subscriberId The subscriber's ID (if applicable)
     * @param customerType The type of customer (Subscriber/Regular)
     * @param tableId      The table number
     * @param customer     The customer object
     */
    public void initData(Order order, Integer subscriberId, CustomerType customerType, int tableId, Customer customer) {
        this.order = order;
        this.subId = subscriberId;
        this.tableId = tableId;
        this.customerType = customerType;
        this.customer = customer;

        generateBill(order, customerType == CustomerType.SUBSCRIBER);
    }

    /**
     * Generates a simulated bill based on the order ID.
     * 
     * @param orderId      The order details
     * @param isSubscriber Whether the customer is a subscriber (eligible for
     *                     discount)
     */
    private void generateBill(Order orderId, boolean isSubscriber) {
        // Use hash of Order ID as seed to ensure consistency for the same order
        long seed = String.valueOf(orderId.getOrderNumber()).hashCode();
        Random random = new Random(seed);

        ObservableList<String> itemsDisplay = FXCollections.observableArrayList();
        double subtotal = 0;

        // Randomly select between 3 to 6 items
        int numberOfItems = random.nextInt(4) + 3;

        for (int i = 0; i < numberOfItems; i++) {
            int itemIndex = random.nextInt(menuItems.length);
            String itemName = menuItems[itemIndex];
            double price = prices[itemIndex];

            itemsDisplay.add(String.format("%-20s %10.2f $", itemName, price));
            subtotal += price;
        }

        itemsList.setItems(itemsDisplay);
        displayPrices(subtotal, isSubscriber);
    }

    /**
     * Calculates and displays the final price, applying discounts if applicable.
     * 
     * @param subtotal     The total price before discount
     * @param isSubscriber Whether the customer is a subscriber
     */
    private void displayPrices(double subtotal, boolean isSubscriber) {
        lblOriginalPrice.setText(String.format("%.2f $", subtotal));

        if (isSubscriber) {
            double discount = subtotal * 0.10; // 10% discount for subscribers
            double finalPrice = subtotal - discount;
            totalPrice = finalPrice;

            discountBox.setVisible(true);
            discountBox.setManaged(true);
            lblDiscountAmount.setText(String.format("-%.2f $", discount));
            lblFinalPrice.setText(String.format("%.2f $", finalPrice));
        } else {
            totalPrice = subtotal;
            discountBox.setVisible(false);
            discountBox.setManaged(false);
            lblFinalPrice.setText(String.format("%.2f $", subtotal));
        }
    }

    /**
     * Handles the "Pay" button action.
     * Saves the total price to the order and navigates to the Payment screen.
     * 
     * @param event The action event
     */
    @FXML
    void payAndReleaseTable(ActionEvent event) {
        this.currentEvent = event;
        System.out.println("Processing bill details for order: " + order.getOrderNumber());
        PaymentController control = super.loadScreen("reservation/Payment", event, clientUi);

        order.setTotalPrice(totalPrice);
        if (control != null)
            control.initData(order, totalPrice, subId, customerType, tableId, customer);
        else
            System.err.println("Error: PaymentController could not be loaded. Check if the FXML path is correct.");

    }

    @Override
    public void onMessageReceive(Object msg) {
        // TODO Auto-generated method stub

    }

    /**
     * Navigates back to the Checkout screen.
     * 
     * @param event The action event
     */
    @FXML
    void goBack(ActionEvent event) {
        // Update current event before transition
        this.currentEvent = event;

        CheckOutController control = super.loadScreen("reservation/CheckOutScreen", event, clientUi);
        control.initData(subId, customerType, tableId, customer);
    }

}