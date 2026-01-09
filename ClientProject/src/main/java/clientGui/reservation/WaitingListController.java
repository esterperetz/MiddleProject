package clientGui.reservation;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // Import added
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientLogic.WaitingListLogic;
import entities.ActionType;
import entities.Alarm;
import entities.Customer;
import entities.Employee;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.WaitingList;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId; // Import added
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import client.MessageListener;

public class WaitingListController extends MainNavigator implements Initializable, MessageListener<Object> {

	@FXML
	private DatePicker filterDate;

	private Employee.Role isManager;
	private Employee emp;

	@FXML
	private TableView<WaitingList> waitingListTable;

	@FXML
	private TableColumn<WaitingList, Integer> colWaitingId;
	@FXML
	private TableColumn<WaitingList, Integer> colCustomerId;
	@FXML
	private TableColumn<WaitingList, Integer> colGuests;
	@FXML
	private TableColumn<WaitingList, Date> colEnterTime;
	@FXML
	private TableColumn<WaitingList, Integer> colConfirmationCode;

	@FXML
	private TableColumn<WaitingList, String> colCustomerName;
	@FXML
	private TableColumn<WaitingList, String> colCustomerPhone;
	@FXML
	private TableColumn<WaitingList, String> colCustomerEmail;

	// Data lists
	private ObservableList<WaitingList> waitingListData = FXCollections.observableArrayList();
	private FilteredList<WaitingList> filteredData; // Added FilteredList

	private WaitingListLogic waitingListLogic;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		colWaitingId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getWaitingId()));

		colCustomerId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCustomerId()));

		colGuests.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getNumberOfGuests()));

		colEnterTime.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEnterTime()));

		colConfirmationCode.setCellValueFactory(
				cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getConfirmationCode()));

		colCustomerName
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

		colCustomerPhone.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getPhoneNumber()));

		colCustomerEmail.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getEmail()));

		// 1. Wrap the ObservableList in a FilteredList (initially display all data)
		filteredData = new FilteredList<>(waitingListData, p -> true);

		// 2. Set the SortedList/FilteredList to the TableView
		waitingListTable.setItems(filteredData);

		// 3. Add Listener to the DatePicker
		if (filterDate != null) {
			filterDate.valueProperty().addListener((observable, oldValue, selectedDate) -> {
				filteredData.setPredicate(item -> {
					// If no date is selected, display all
					if (selectedDate == null) {
						return true;
					}

					// If the item has no date, hide it
					if (item.getEnterTime() == null) {
						return false;
					}

					// Convert java.util.Date / SQL Timestamp to LocalDate
					LocalDate itemDate;
					if (item.getEnterTime() instanceof java.sql.Date) {
						itemDate = ((java.sql.Date) item.getEnterTime()).toLocalDate();
					} else {
						itemDate = item.getEnterTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					}

					LocalDate today = LocalDate.now();

					// Logic copied from OrderUi:
					// Show if date is AFTER (or equal) the selected date
					boolean isAfterSelection = !itemDate.isBefore(selectedDate);
					// Show if date is BEFORE (or equal) today
					boolean isBeforeToday = !itemDate.isAfter(today);

					return isAfterSelection && isBeforeToday;
				});
			});
		}
	}

	public void initData(Employee emp, ClientUi clientUi, Employee.Role isManager) {
		this.emp = emp;
		this.clientUi = clientUi;
		this.isManager = isManager;
		this.waitingListLogic = new WaitingListLogic(clientUi);
		waitingListLogic.getAllWaitingListCustomer();
	}

	public void onMessageReceive(Object msg) {
	    Platform.runLater(() -> {
	        if (msg instanceof Response) {
	            Response res = (Response) msg;

	            if (res.getResource() == ResourceType.WAITING_LIST) {
	                
	                switch (res.getAction()) {
	                    case GET_ALL: 
	                    case GET_ALL_LIST:
	                        handleGetAllList(res.getData());
	                        break;

	                    case PROMOTE_TO_ORDER:
	                    case EXIT_WAITING_LIST:
	                        handleUpdateResponse(res);
	                        break;

	                    default:
	                        break;
	                }
	            }
	        }
	    });
	}

	
	@SuppressWarnings("unchecked")
	private void handleGetAllList(Object data) {
	    if (data instanceof List) {
	        List<?> list = (List<?>) data;
	        System.out.println("DEBUG: Rows received: " + list.size());

	        waitingListData.clear();

	        if (list.isEmpty()) {
	            waitingListTable.refresh();
	            return;
	        }

	        for (Object obj : list) {
	            if (obj instanceof Map) {
	                try {
	                    WaitingList item = parseWaitingListRow((Map<String, Object>) obj);
	                    if (item != null) {
	                        waitingListData.add(item);
	                    }
	                } catch (Exception e) {
	                    System.err.println("Error parsing row in WaitingList: " + e.getMessage());
	                    e.printStackTrace();
	                }
	            }
	        }
	        waitingListTable.refresh();
	    }
	}

	private WaitingList parseWaitingListRow(Map<String, Object> row) {
	    WaitingList item = new WaitingList();

	    if (row.get("waiting_id") != null)
	        item.setWaitingId(((Number) row.get("waiting_id")).intValue());

	    if (row.get("customer_id") != null)
	        item.setCustomerId(((Number) row.get("customer_id")).intValue());

	    if (row.get("number_of_guests") != null)
	        item.setNumberOfGuests(((Number) row.get("number_of_guests")).intValue());

	    if (row.get("confirmation_code") != null)
	        item.setConfirmationCode(((Number) row.get("confirmation_code")).intValue());

	    // --- 2. טיפול בתאריך (enter_time) ---
	    if (row.get("enter_time") != null) {
	        Object timeObj = row.get("enter_time");
	        if (timeObj instanceof java.sql.Timestamp) {
	            item.setEnterTime(new java.util.Date(((java.sql.Timestamp) timeObj).getTime()));
	        } else if (timeObj instanceof java.util.Date) {
	            item.setEnterTime((java.util.Date) timeObj);
	        }
	    }

	    Customer customer = new Customer();

	    if (row.get("customer_id") != null)
	        customer.setCustomerId(((Number) row.get("customer_id")).intValue());

	    if (row.get("customer_name") != null)
	        customer.setName((String) row.get("customer_name"));

	    if (row.get("email") != null)
	        customer.setEmail((String) row.get("email"));

	    if (row.get("phone_number") != null)
	        customer.setPhoneNumber((String) row.get("phone_number"));

	    Object subCode = row.get("subscriber_code");
	    if (subCode != null) {
	        customer.setSubscriberCode(((Number) subCode).intValue());
	    } else {
	        customer.setSubscriberCode(0);
	    }

	    item.setCustomer(customer);
	    
	    return item;
	}

	
	private void handleUpdateResponse(Response res) {
	    if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
	        waitingListLogic.getAllWaitingListCustomer(); 
	        
	        if (res.getMessage_from_server() != null) {
	            Alarm.showAlert("Success", res.getMessage_from_server(),Alert.AlertType.CONFIRMATION);
	        }
	    } else {
	    	Alarm.showAlert("Error", res.getMessage_from_server(),Alert.AlertType.ERROR);
	    }
	}

	// Removed the manual handleDateSelect logic since we added a Listener in
	// initialize
	@FXML
	void handleDateSelect(ActionEvent event) {
		// Keeping this empty or removing it is fine,
		// as the work is now done by the listener in initialize()
	}

	@FXML
	void handleClearFilter(ActionEvent event) {
		// Setting this to null triggers the listener in initialize, which resets the
		// filter
		filterDate.setValue(null);
	}

	@FXML
	void handleAssignTable(ActionEvent event) {
		WaitingList selected = waitingListTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			Alarm.showAlert("Selection Error", "Please select a customer to assign.",Alert.AlertType.ERROR);
			return;
		}
		Request req = new Request(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER, selected.getWaitingId(),
				null);
		clientUi.sendRequest(req);
	}

	@FXML
	void handleRemoveEntry(ActionEvent event) {
		WaitingList selected = waitingListTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			Alarm.showAlert("Selection Error", "Please select a customer to remove.",Alert.AlertType.ERROR);
			return;
		}
		Request req = new Request(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST, selected.getWaitingId(),
				null);
		clientUi.sendRequest(req);
	}

	@FXML
	void handleBackBtn(ActionEvent event) {
		ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
		if (controller != null) {
			controller.initData(emp, clientUi, this.isManager);
		} else {
			System.err.println("Error: Could not load ManagerOptionsController.");
		}
	}


}