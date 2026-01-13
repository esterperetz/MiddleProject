package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import DAO.WaitingListDAO;
import DAO.TableDAO;
import DAO.OrderDAO;
import entities.Order;
import entities.Order.OrderStatus;
import entities.WaitingList;

/**
 * Background thread monitoring the waiting list for safe seating gaps. 
 * This Thread only promotes from waiting list, but doesn't assign itself to a table. This happens in Identify At Terminal!!!
 */
public class WaitingListCheckThread extends Thread {
	private final WaitingListDAO waitingListDao = new WaitingListDAO();
	private final WaitingListController waitingListController = new WaitingListController();
	private final TableDAO tableDao = new TableDAO();
	private final OrderDAO orderDao = new OrderDAO();
	private boolean running = true;

	@Override
	public void run() {
		while (running) {
			try {
				// Interval check every minute 
				Thread.sleep(60000);
				processWaitingList();
			} catch (InterruptedException e) {
				running = false;
			}
		}
	}

	private void processWaitingList() {
	    try {
	        List<WaitingList> entries = waitingListDao.getAllWaitingList();
	        
	   
	        int promotedCount = 0; 

	        for (WaitingList entry : entries) {
	            int guests = entry.getNumberOfGuests();

	            // 1. חילוץ התאריך המבוקש
	            Date desiredTime = new Date(); // ברירת מחדל: עכשיו
	            
	            // ניסיון למצוא הזמנה מקושרת (לוגיקה רגישה, עדיף לשמור order_id ב-waiting_list)
	            if (entry.getCustomer() != null && entry.getCustomer().getCustomerId() != null) {
	                List<Order> orderList = orderDao.getOrdersByCustomerId(entry.getCustomer().getCustomerId());
	                for(Order o : orderList) {
	                    // הנחה: הזמנה שמחכה לאישור היא בסטטוס PENDING או NULL (תלוי במימוש שלך)
	                    // והיא הקרובה ביותר בזמן
	                     if(o.getOrderStatus() == null) {
	                        desiredTime = o.getOrderDate();
	                        break; // לוקחים את הראשונה שמוצאים
	                    }
	                }
	            }

	            // חישוב טווח זמנים
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(desiredTime);
	            cal.add(Calendar.HOUR_OF_DAY, 2); 
	            Date estimatedEndTime = cal.getTime();

	            // 2. בדיקת זמינות נכונה
	            int totalSuitableTables = tableDao.countSuitableTables(guests);
	            
	            // המשתנה הזה סופר גם אנשים שיושבים (אם הזמן הוא עכשיו) וגם הזמנות עתידיות (אם הזמן הוא אח"כ)
	            // הוא עושה את זה באמצעות בדיקת חפיפת זמנים ב-SQL
	            int conflictingOrders = orderDao.countConflictingOrders(desiredTime, estimatedEndTime, guests);

	            // 3. חישוב סופי
	            int realAvailability = totalSuitableTables - conflictingOrders - promotedCount;

	            if (realAvailability > 0) {
	                boolean success = promoteEntry(entry);
	                if (success) {
	                    promotedCount++;
	                    // הערה: זה עדיין מוריד מהמלאי הכללי. במערכת מושלמת היינו עוקבים לפי סוג שולחן.
	                }
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("WaitingList Thread Error: " + e.getMessage());
	    }
	}

	private boolean promoteEntry(WaitingList entry) {
		try {
			// Notify client and convert entry to APPROVED order 
			boolean success = waitingListController.handlePromoteToOrder(entry.getWaitingId(), null);
			if (success) {
				System.out.println("Waiting List: Entry for " + entry.getCustomerId() + " promoted.");
				return success;
			}
		} catch (Exception e) {
			System.err.println("Promotion failed: " + e.getMessage());
		}
		return false;
	}

	public void stopThread() {
		this.running = false;
		this.interrupt();
	}
}



