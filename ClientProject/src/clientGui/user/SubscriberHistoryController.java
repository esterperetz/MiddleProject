package clientGui.user;


import clientGui.navigation.MainNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class SubscriberHistoryController {

    @FXML private TableView<OrderHistoryItem> ordersTable;
    @FXML private TableColumn<OrderHistoryItem, Integer> colOrderId;
    @FXML private TableColumn<OrderHistoryItem, String> colDate;
    @FXML private TableColumn<OrderHistoryItem, String> colTime;
    @FXML private TableColumn<OrderHistoryItem, String> colTotal;
    @FXML private TableColumn<OrderHistoryItem, String> colStatus;

    @FXML
    public void initialize() {
        // חיבור העמודות למשתנים במחלקה
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // טעינת נתונים לדוגמה (בפועל זה יגיע מהשרת)
        loadMockData();
    }

    private void loadMockData() {
        ObservableList<OrderHistoryItem> list = FXCollections.observableArrayList(
            new OrderHistoryItem(1024, "12/12/2025", "19:30", "65.00 ₪", "Completed"),
            new OrderHistoryItem(1011, "05/12/2025", "13:15","45.00 ₪", "Completed"),
            new OrderHistoryItem(998, "20/11/2025", "20:00", "180.00 ₪", "Completed")
        );
        ordersTable.setItems(list);
    }

    @FXML
    void goBackBtn(ActionEvent event) {
//        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    	MainNavigator.loadScene("user/SubscriberOption");
    	
        
    }

    // --- מחלקה פנימית לייצוג שורה בטבלה ---
    public static class OrderHistoryItem {
        private int orderId;
        private String date;
        private String time;
        private String total;
        private String status;

        public OrderHistoryItem(int orderId, String date, String time, String total, String status) {
            this.orderId = orderId;
            this.date = date;
            this.time = time;
            this.total = total;
            this.status = status;
        }

        // Getters are mandatory for PropertyValueFactory
        public int getOrderId() { return orderId; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getTotal() { return total; }
        public String getStatus() { return status; }
    }
}