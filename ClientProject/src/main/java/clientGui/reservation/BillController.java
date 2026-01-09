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

public class BillController extends MainNavigator implements  Initializable , MessageListener<Object>{

    @FXML private ListView<String> itemsList;
    @FXML private Label lblOriginalPrice, lblDiscountAmount, lblFinalPrice;
    @FXML private HBox discountBox;
    @FXML private Button btnCancel;
    	  private Double totalPrice;

    // רשימת מאכלים אפשריים לסימולציה
    private final String[] menuItems = {
        "Margherita Pizza", "Pasta Carbonara", "Caesar Salad", 
        "Beef Burger", "Coca Cola", "Red Wine", "Chocolate Souffle", "Fries"
    };
    private final double[] prices = {55.0, 62.0, 45.0, 68.0, 12.0, 35.0, 38.0, 25.0};
	private Order order;
	private Integer subId;
	private int tableId;
	private CustomerType customerType;
	private ActionEvent currentEvent;
	private Customer customer;

    
    
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
    public void initData(Order order, Integer subscriberId, CustomerType customerType, int tableId,Customer customer) {
    	this.order = order;
    	this.subId = subscriberId;
    	this.tableId = tableId;
    	this.customerType = customerType;
    	this.customer = customer;
    	
        generateBill(order, customerType == CustomerType.SUBSCRIBER);
    }

    private void generateBill(Order orderId, boolean isSubscriber) {
        // שימוש ב-Hash של ה-ID כ-Seed מבטיח שאותו ID ייתן תמיד אותה רשימה
        long seed = String.valueOf(orderId.getOrderNumber()).hashCode();
        Random random = new Random(seed);

        ObservableList<String> itemsDisplay = FXCollections.observableArrayList();
        double subtotal = 0;

        // נגריל בין 3 ל-6 פריטים
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

    private void displayPrices(double subtotal, boolean isSubscriber) {
        lblOriginalPrice.setText(String.format("%.2f $", subtotal));

        if (isSubscriber) {
            double discount = subtotal * 0.10; // 10% הנחה למנוי
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

    @FXML
    void payAndReleaseTable(ActionEvent event) {
       this.currentEvent = event;
       System.out.println("Processing bill details for order: " + order.getOrderNumber());
       PaymentController control = super.loadScreen("reservation/Payment", event, clientUi);
     
       order.setTotalPrice(totalPrice);
       if (control!= null)
    	   control.initData(order,totalPrice,subId ,customerType , tableId,customer);
       else
    	   System.err.println("Error: PaymentController could not be loaded. Check if the FXML path is correct.");
     
    }

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	void goBack(ActionEvent event) {
	    // עדכון ה-event הנוכחי לפני המעבר
	    this.currentEvent = event;

	   CheckOutController control = super.loadScreen("reservation/CheckOutScreen", event, clientUi);
	   control.initData(subId, customerType, tableId,customer);
	}

	
}