package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import DAO.WaitingListDAO;
import DAO.TableDAO;
import DAO.OrderDAO;
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
	        // 1. שליפת כל הממתינים לפי סדר הכניסה (FIFO)
	        List<WaitingList> entries = waitingListDao.getAllWaitingList();

	        // משתנה עזר למעקב אחרי כמה שולחנות "תפסנו" בריצה הנוכחית כדי לא לעשות Double Booking
	        // (הערה: זהו פתרון פשוט, במערכת מורכבת עדיף לנהל הקצאת שולחנות ספציפיים)
	        int promotedThisCycle = 0;

	        for (WaitingList entry : entries) {
	            int guests = entry.getNumberOfGuests();

	            // 2. ספירת סך השולחנות במסעדה שמתאימים לכמות האורחים (>= guests)
	            int totalSuitableTables = tableDao.countSuitableTables(guests);

	            // 3. ספירת שולחנות תפוסים כרגע שיכולים להכיל כמות אורחים כזו
	            // (חשוב: ה-DAO צריך לספור שולחנות תפוסים שגודלם >= guests)
	            int occupiedTables = orderDao.countCurrentlySeatedOrders(guests);

	            // 4. הגנה על הזמנות עתידיות בטווח של השעתיים הקרובות
	            Date now = new Date();
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(now);
	            cal.add(Calendar.HOUR_OF_DAY, 2); // טווח ביטחון של שעתיים
	            Date twoHoursFromNow = cal.getTime();

	            int upcomingReservations = orderDao.countApprovedOrdersInRange(now, twoHoursFromNow, guests);

	            // 5. חישוב זמינות בזמן אמת
	            // הנוסחה: סך הכל מתאימים - (תפוסים כרגע + מוזמנים בקרוב + אלו שקידמנו הרגע בלולאה זו)
	            int realAvailability = totalSuitableTables - occupiedTables - upcomingReservations - promotedThisCycle;

	            if (realAvailability > 0) {
	                // נמצא פער בטוח - נקדם את הלקוח
	                boolean success = promoteEntry(entry);
	                
	                if (success) {
	                    // אם הצלחנו לקדם, נסמן שנתפס שולחן אחד באופן וירטואלי בריצה זו
	                    promotedThisCycle++;
	                    
	                   
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



