package clientGui.user;


import java.util.Date;
import java.util.List;

import Entities.Order;
import Entities.Order.OrderStatus;
import Entities.Response;
import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class SubscriberHistoryController extends SubscriberOptionController implements  MessageListener<Object>{

    @FXML private TableView<OrderHistoryItem> ordersTable;
    @FXML private TableColumn<OrderHistoryItem, Integer> colOrderId;
    @FXML private TableColumn<OrderHistoryItem, String> colDate;
    @FXML private TableColumn<OrderHistoryItem, String> colTime;
    @FXML private TableColumn<OrderHistoryItem, String> colTotal;
    @FXML private TableColumn<OrderHistoryItem, String> colStatus;
    	  private OrderLogic orderLogic;
    	  private List<Order> orderList;
    	 

    @FXML
    public void initialize() {
        // חיבור העמודות למשתנים במחלקה
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderLogic = new OrderLogic(clientUi);
        // טעינת נתונים לדוגמה (בפועל זה יגיע מהשרת)
        loadDataFromDB();
    }
 

    private void loadDataFromDB() {
    	if (subscriberId != 0)
    		orderLogic.getOrdersBySubscriberId(subscriberId);
    
    }

    @FXML
    void goBackBtn(ActionEvent event) {
//        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    	super.loadScreen("user/SubscriberOption",event,clientUi);
    	
        
    }

    // --- מחלקה פנימית לייצוג שורה בטבלה ---
    public static class OrderHistoryItem {
        private int orderId;
        private Date date;
        private Date time;
        private double total;
        private OrderStatus  status;

        public OrderHistoryItem(int orderId, Date date, Date time, double total, OrderStatus orderStatus) {
            this.orderId = orderId;
            this.date = date;
            this.time = time;
            this.total = total;
            this.status = orderStatus;
        }

        // Getters are mandatory for PropertyValueFactory
        public int getOrderId() { return orderId; }
        public Date getDate() { return date; }
        public Date getTime() { return time; }
        public double getTotal() { return total; }
        public OrderStatus getStatus() { return status; }
    }

	

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		if (msg instanceof Response) {
			Response response = (Response)msg;
			boolean status = ((Response) msg).getStatus().equals("SUCCESS");
			if(status) {
				List<Order> orderList = (List<Order>) ((Response) msg).getData();
				Platform.runLater(() -> {
	                ObservableList<OrderHistoryItem> tableData = FXCollections.observableArrayList();
	                
	                for (Order o : orderList) {
	                    tableData.add(new OrderHistoryItem(
	                        o.getOrder_number(), 
	                        o.getDate_of_placing_order(), 
	                        o.getArrivalTime(), 
	                        o.getTotal_price(), 
	                        o.getOrder_status()
	                    ));
	                }
	                ordersTable.setItems(tableData);
	                ordersTable.refresh(); // ליתר ביטחון
	            });
			}
			else {
				System.out.println("There are no available orders by this ID");
			}
		
		}
	}
}