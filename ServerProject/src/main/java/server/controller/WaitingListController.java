package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import DAO.*;
import entities.*;
import entities.Order.OrderStatus;
import ocsf.server.ConnectionToClient;

public class WaitingListController {

	private final WaitingListDAO waitingListDAO = new WaitingListDAO();
	private final OrderDAO orderDAO = new OrderDAO();
	private final CustomerDAO customerDAO = new CustomerDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
		if (req.getResource() != ResourceType.WAITING_LIST) {
			client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR,
					"Incorrect resource type.", null));
			return;
		}

		try {
			switch (req.getAction()) {
				case GET_ALL:
					handleGetAll(req, client);
					break;
				case GET_ALL_LIST:
					handleGetAllListWithCustomer(req, client);
					break;
				case ENTER_WAITING_LIST:
					handleEnterWaitingList(req, client);
					break;

				case EXIT_WAITING_LIST:
					handleExitWaitingList(req, client);
					break;

				case PROMOTE_TO_ORDER:
					handlePromoteToOrder(req.getId(), client);
					break;

				default:
					client.sendToClient(new Response(ResourceType.WAITING_LIST, req.getAction(),
							Response.ResponseStatus.ERROR, "Unknown action", null));
					break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			client.sendToClient(new Response(ResourceType.WAITING_LIST, req.getAction(),
					Response.ResponseStatus.DATABASE_ERROR, e.getMessage(), null));
		}
	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<WaitingList> list = waitingListDAO.getAllWaitingList();
		client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
				null, list));
	}

	private void handleGetAllListWithCustomer(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<Map<String, Object>> list = waitingListDAO.getAllWaitingListWithCustomers();
		client.sendToClient(
				new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL_LIST, Response.ResponseStatus.SUCCESS,
						null, list));
	}

	private void handleEnterWaitingList(Request req, ConnectionToClient client) throws SQLException, IOException {
	    WaitingList item = (WaitingList) req.getPayload();

	    // יצירת קוד אישור
	    int generatedCode = 1000 + (int) (Math.random() * 9000);
	    item.setConfirmationCode(generatedCode);
	    item.setEnterTime(new Date());

	    // --- זיהוי הלקוח (לוגיקה קיימת שלך) ---
	    Customer finalCustomer = null;
	    Integer subCode = item.getCustomer().getSubscriberCode();

	    if (subCode != null && subCode > 0) {
	        finalCustomer = customerDAO.getCustomerBySubscriberCode(subCode);
	        if (finalCustomer == null) {
	            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
	                    Response.ResponseStatus.ERROR, "Invalid Subscriber Code.", null));
	            return;
	        }
	    } else {
	        String email = item.getCustomer().getEmail();
	        finalCustomer = customerDAO.getCustomerByEmail(email);

	        if (finalCustomer == null) {
	            Customer newGuest = item.getCustomer();
	            newGuest.setType(CustomerType.REGULAR);
	            customerDAO.createCustomer(newGuest);
	            finalCustomer = customerDAO.getCustomerByEmail(email);
	        }
	    }

	    if (finalCustomer == null || finalCustomer.getCustomerId() == null) {
	        client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
	                Response.ResponseStatus.DATABASE_ERROR, "Failed to identify customer.", null));
	        return;
	    }

	    // עדכון הפריט עם פרטי הלקוח
	    item.setCustomerId(finalCustomer.getCustomerId());
	    item.setCustomer(finalCustomer);

	    // --- שלב קריטי: יצירת הזמנה "רדומה" (Placeholder) ---
	    // אנו יוצרים הזמנה עם התאריך שהלקוח ביקש (item.getReservationDate())
	    // הסטטוס נשלח כ-NULL (הפרמטר האחרון בבנאי)
	    Order placeholderOrder = new Order(0, item.getReservationDate(), item.getNumberOfGuests(), 
	                                       item.getConfirmationCode(), finalCustomer, null, 
	                                       new Date(), null, null, 0.0, null); // Status = null
	    
	    // שומרים את ההזמנה במסד הנתונים
	    orderDAO.createOrder(placeholderOrder);

	    // --- שמירה לרשימת ההמתנה ---
	    // ההזמנה (id) תקושר בפנים אם עדכנת את DAO כפי שדיברנו, או דרך הקוד אישור
	    // נשתמש ב-generatedCode כדי למנוע כפילות בתוך enterWaitingList אם מימשת את הלוגיקה הקודמת
	    if (waitingListDAO.enterWaitingList(item)) {
	        client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
	                Response.ResponseStatus.SUCCESS, String.valueOf(generatedCode), true));
	        sendListToAllClients();
	    } else {
	        client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
	                Response.ResponseStatus.DATABASE_ERROR, "Failed to add to waiting list.", null));
	    }
	}
	private void handleExitWaitingList(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST,
					Response.ResponseStatus.ERROR, "Missing ID", null));
			return;
		}
		if (waitingListDAO.exitWaitingList(req.getId())) {
//			EmailService.sendConfirmation(null, null);
			client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST,
					Response.ResponseStatus.SUCCESS, "Removed", true));
			sendListToAllClients();
		}
	}
	//there is a problem with waiting list , they move to orders as proved with the thread when not needed (ask liel)
	public boolean handlePromoteToOrder(Integer waitingId, ConnectionToClient client) throws SQLException, IOException {
	    if (waitingId == null) {
	        if (client != null)
	            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER,
	                    Response.ResponseStatus.ERROR, "Missing ID", null));
	        return false;
	    }

	    // 1. שליפת הפריט מרשימת ההמתנה
	    WaitingList entry = waitingListDAO.getByWaitingId(waitingId);
	    if (entry == null) return false;

	    Customer customer = entry.getCustomer();
	    if (customer == null && entry.getCustomerId() != null) {
	        customer = customerDAO.getCustomerByCustomerId(entry.getCustomerId());
	    }

	    if (customer == null) {
	        System.err.println("Failed to promote waiting list entry " + waitingId + ": Customer not found.");
	        return false;
	    }

	    // 2. מציאת ההזמנה ה"רדומה" לפי קוד האישור
	    // (כאן אנחנו משתמשים בפונקציה שעדכנו קודם שמוצאת גם NULL)
	    Order existingOrder = orderDAO.getOrderByConfirmationCode(entry.getConfirmationCode());
	    
	    if (existingOrder == null) {
	        System.err.println("Critical Error: Order not found for code " + entry.getConfirmationCode());
	        return false;
	    }

	    // 3. עדכון הסטטוס ל-APPROVED
	    existingOrder.setCustomer(customer);
	    existingOrder.setOrderStatus(OrderStatus.APPROVED); 
	    // אם התאריך היה יכול להשתנות, זה הזמן לעדכן אותו ב-existingOrder

	    
	    if (orderDAO.updateOrder(existingOrder)) {
	        
	        // מחיקה מרשימת ההמתנה
	        waitingListDAO.exitWaitingList(waitingId);

	        // שליחת עדכונים
	        List<WaitingList> updatedList = waitingListDAO.getAllWaitingList();
	        EmailService.sendConfirmation(existingOrder.getCustomer(), existingOrder);
	        
	        Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
	                Response.ResponseStatus.SUCCESS, null, updatedList));
	        Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
	                Response.ResponseStatus.SUCCESS, null, orderDAO.getAllOrders()));

	        if (client != null) {
	            client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER,
	                    Response.ResponseStatus.SUCCESS, null, true));
	        }
	        return true;
	    }
	    return false;
	}
	private void sendListToAllClients() throws SQLException {
		List<WaitingList> list = waitingListDAO.getAllWaitingList();
		Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
				Response.ResponseStatus.SUCCESS, null, list));
	}
}