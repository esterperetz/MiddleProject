package clientGui.user;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.managerTeam.SubscriberManagementController;
import clientGui.navigation.MainNavigator;
import clientLogic.OrderLogic;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import entities.Employee.Role;
import entities.Order;
import entities.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SubscriberHistoryController extends MainNavigator implements MessageListener<Object>, Initializable {

	// we need to bring the join between subscriber and order
	@FXML
	private TableView<OrderHistoryItem> ordersTable;
	@FXML
	private TableColumn<OrderHistoryItem, Integer> colOrderId;
	@FXML
	private TableColumn<OrderHistoryItem, String> colDate;
	@FXML
	private TableColumn<OrderHistoryItem, String> colTime;
	@FXML
	private TableColumn<OrderHistoryItem, String> colTotal;
	@FXML
	private TableColumn<OrderHistoryItem, String> colStatus;
	@FXML
	private DatePicker filterDatePicker;
	private ObservableList<OrderHistoryItem> fullDataList = FXCollections.observableArrayList();
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private OrderLogic orderLogic;
	private int currentSubscriberId;
	private CustomerType isSubscriber;
	private Employee employee;
	private Customer customer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
		colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
		colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
			handleDateFilter(null);
		});
	}

	public void initData(int subscriberId, CustomerType isSubscriber, Employee employee, Customer customer) {
		this.isSubscriber = isSubscriber;
		this.currentSubscriberId = subscriberId;
		this.orderLogic = new OrderLogic(clientUi);
		this.customer = customer;
		this.employee = employee;
		System.out.println("Fetching history for subscriber: " + subscriberId);
		orderLogic.getOrdersBySubscriberId(subscriberId);

	}

	@FXML
	void handleClearFilter(ActionEvent event) {
		filterDatePicker.setValue(null);
		ordersTable.setItems(fullDataList);
	}

	@FXML
	void handleDateFilter(ActionEvent event) {
		LocalDate selectedDate = filterDatePicker.getValue();

		if (selectedDate == null) {
			return;
		}

		// create a list that help us to filter
		ObservableList<OrderHistoryItem> filteredList = FXCollections.observableArrayList();

		for (OrderHistoryItem item : fullDataList) {
			try {
				// convert string
				LocalDate itemDate = LocalDate.parse(item.getDate(), formatter);
				// filter orders
				if (!itemDate.isBefore(selectedDate)) {
					filteredList.add(item);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// update the filter table
		ordersTable.setItems(filteredList);
	}

	@FXML
	void goBackBtn(ActionEvent event) {
		if (employee != null) {
			SubscriberManagementController controller = super.loadScreen("managerTeam/SubscriberManagement", event, clientUi);
			if (controller != null) {
				controller.initData(employee, clientUi, employee.getRole());
			} else
				System.out.println("Error in loading Manager options");
		} else {
			SubscriberOptionController subscriberOptionController = super.loadScreen("user/SubscriberOption", event,
					clientUi);
			if (subscriberOptionController != null)
				subscriberOptionController.initData(clientUi, isSubscriber, currentSubscriberId, customer);
			else
				System.err.println("Error loading subscriber option");
		}
	}

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		try {
			Platform.runLater(() -> {
				if (msg instanceof Response) {
					Response res = (Response) msg;

					if (res.getAction() == ActionType.GET_ALL_BY_SUBSCRIBER_ID) {

						if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
							if (res.getData() instanceof List) {
								List<Order> orders = (List<Order>) res.getData();
								updateTable(orders);
							}
						} else {

							System.err.println("Error fetching history: " + res.getMessage_from_server());
						}
					} else
						System.out.println("Action isnt GET ALL BY SUBSCRIBER ID");
				}
			});
		} catch (Exception e) {
			System.out.println("two ");
		}

	}

	private void updateTable(List<Order> orders) {
		fullDataList.clear();

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		for (Order o : orders) {
			LocalDate localDate = o.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String dateStr = localDate.format(formatter);

			String timeStr = o.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).format(timeFormatter);

			String priceStr = String.format("%.2f ₪", o.getTotalPrice());
			String statusStr = o.getOrderStatus().toString();

			fullDataList.add(new OrderHistoryItem(o.getOrderNumber(), dateStr, timeStr, priceStr, statusStr));
		}
		// ordersTable.setItems(fullDataList);
		if (filterDatePicker.getValue() != null) {
			handleDateFilter(null);
		} else {
			ordersTable.setItems(fullDataList);
		}
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
		public int getOrderId() {
			return orderId;
		}

		public String getDate() {
			return date;
		}

		public String getTime() {
			return time;
		}

		public String getTotal() {
			return total;
		}

		public String getStatus() {
			return status;
		}
	}

}