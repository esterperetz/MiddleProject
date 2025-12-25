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
			// 1. Fetch entries in FIFO order 
			List<WaitingList> entries = waitingListDao.getAllWaitingList();

			for (WaitingList entry : entries) {
				int guests = entry.getNumberOfGuests();

				// 2. Total suitable tables in restaurant 
				int totalSuitableTables = tableDao.countSuitableTables(guests);

				// 3. Tables currently occupied by seated diners 
				int occupiedTables = orderDao.countCurrentlySeatedOrders(guests);

				// 4. Protect upcoming reservations within the next 2 hours
				Date now = new Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(now);
				cal.add(Calendar.HOUR_OF_DAY, 2);
				Date twoHoursFromNow = cal.getTime();

				int upcomingReservations = orderDao.countApprovedOrdersInRange(now, twoHoursFromNow, guests);

				// 5. Calculate if a seat is available without bumping a future reservation
				int realAvailability = totalSuitableTables - occupiedTables - upcomingReservations;

				if (realAvailability > 0) {
					// Safe gap found; promote entry
					promoteEntry(entry);

					// Break to prevent double-booking within the same cycle
					break;
				}
			}
		} catch (SQLException e) {
			System.err.println("WaitingList Thread Error: " + e.getMessage());
		}
	}

	private void promoteEntry(WaitingList entry) {
		try {
			// Notify client and convert entry to APPROVED order 
			boolean success = waitingListController.handlePromoteToOrder(entry.getWaitingId(), null);
			if (success) {
				System.out.println("Waiting List: Entry for " + entry.getFullName() + " promoted.");
			}
		} catch (Exception e) {
			System.err.println("Promotion failed: " + e.getMessage());
		}
	}

	public void stopThread() {
		this.running = false;
		this.interrupt();
	}
}



