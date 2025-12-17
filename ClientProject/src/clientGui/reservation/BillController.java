package clientGui.reservation;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;

public class BillController extends MainNavigator implements  MessageListener<Object> {

    @FXML private ListView<String> itemsList;
    @FXML private Label lblOriginalPrice;
    @FXML private HBox discountBox; // הקופסה שמחזיקה את שורת ההנחה
    @FXML private Label lblDiscountAmount;
    @FXML private Label lblFinalPrice;

    private int tableId;
    
    /**
     * פונקציה זו נקראת ע"י המסך הקודם כדי לטעון את הנתונים
     * @param items - רשימת הפריטים
     * @param originalTotal - המחיר המקורי לפני הנחה
     * @param isSubscriber - האם הלקוח הוא מנוי
     * @param tableId - מספר השולחן (כדי לשחרר אותו בסוף)
     */
    public void initData(ObservableList<String> items, double originalTotal, boolean isSubscriber, int tableId) {
        this.tableId = tableId;
        itemsList.setItems(items);
        
        lblOriginalPrice.setText(String.format("%.2f $", originalTotal));

        if (isSubscriber) {
            // חישוב הנחה
            double discount = originalTotal * 0.10;
            double finalPrice = originalTotal - discount;

            // הצגת שורת ההנחה
            discountBox.setVisible(true);
            discountBox.setManaged(true); // תופס מקום במסך
            
            lblDiscountAmount.setText(String.format("-%.2f $", discount));
            lblFinalPrice.setText(String.format("%.2f $", finalPrice));
        } else {
            // הסתרת שורת ההנחה ללקוח רגיל
            discountBox.setVisible(false);
            discountBox.setManaged(false); // לא תופס מקום, השורות נצמדות
            
            lblFinalPrice.setText(String.format("%.2f $", originalTotal));
        }
    }

    @FXML
    void payAndReleaseTable(ActionEvent event) {
        
		super.loadScreen("reservation/Payment" , event,clientUi);
        
    }

	

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}
}