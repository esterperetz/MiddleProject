package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import DAO.WaitingListDAO;
import DAO.TableDAO;
import DAO.OrderDAO;
import entities.WaitingList;
import java.util.Comparator;

public class WaitingListCheckThread extends Thread {
	private final WaitingListDAO waitingListDao = new WaitingListDAO();
	private final WaitingListController waitingListController = new WaitingListController();
	private final TableDAO tableDao = new TableDAO();
	private final OrderDAO orderDao = new OrderDAO();

	private boolean running = true;

	@Override
	public void run() {
		System.out.println("WaitingList Thread Started...");
		while (running) {
			try {
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

			
			List<WaitingList> promotedThisCycle = new ArrayList<>();
			for (WaitingList entry : entries) {
				int guests = entry.getNumberOfGuests();

				Date requestedDate = entry.getReservationDate();
				if (requestedDate == null) {
					requestedDate = new Date();
				}

				int totalSuitableTables = tableDao.countTotalPhysicalTables(guests);
				int dbConflicts = orderDao.countConflictingOrders(requestedDate, guests);
				int localConflicts = 0;
				
				for (WaitingList promoted : promotedThisCycle) {

					Date promotedDate = promoted.getReservationDate();
					if (promotedDate == null)
						promotedDate = new Date();

					if (promoted.getNumberOfGuests() >= guests && isOverlapping(promotedDate, requestedDate)) {
						localConflicts++;
					}
				}
				System.out.println("Date: " + requestedDate + " totalTables : " + totalSuitableTables + " dbConflicts: "
						+ dbConflicts + " localConflicts: " + localConflicts);
				
				if ((totalSuitableTables > (dbConflicts + localConflicts))) {
					List<Integer> table_list= tableDao.getAllTableCapacities2();
					List<Integer> orders = orderDao.getActiveOrderSizes2(requestedDate);
				
				
					if (isValidRemoveWaitingList(table_list,orders) && entry.getInWaitingList() == 1) {
						boolean success = promoteEntry(entry);
					
						if (success) {
							System.out.println("Promoted waiting list entry " + entry.getWaitingId());

							promotedThisCycle.add(entry);
						}
					}else {
						System.out.println("The waiting list is Empty.");
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("WaitingList Thread Error: " + e.getMessage());
		}
	}

	private boolean isOverlapping(Date date1, Date date2) {
		long time1 = date1.getTime();
		long time2 = date2.getTime();
		long twoHours = 2 * 60 * 60 * 1000; 

		
		return (time1 < time2 + twoHours) && (time1 + twoHours > time2);
	}

	private boolean isValidRemoveWaitingList(List<Integer> table_list, List<Integer> orders) {
		if (table_list.size() < orders.size()) {
			System.out.println("The size of lists are different!!");
			return false;
		}
		
		
		table_list.sort(Comparator.reverseOrder());//2
		orders.sort(Comparator.reverseOrder());//2
		System.out.println("table list: "+table_list.toString());
		System.out.println("order list: "+orders.toString());
		int size = 0;

		for (int i = 0; i < orders.size(); i++) {
			if (!(table_list.get(size) >= orders.get(i))) {
				return false;
			}
			size++;//2
			if(size >= table_list.size()) {
				break;
			}
		}
		return true;
	}

	private boolean promoteEntry(WaitingList entry) {
		try {
			System.out.println("Attempting to promote waiting list entry: " + entry.getWaitingId());
			boolean success = waitingListController.handlePromoteToOrder(entry.getWaitingId(), null);
			if (success) {
				System.out.println("Success! Entry " + entry.getWaitingId() + " promoted to Order.");
				return true;
			}
		} catch (Exception e) {
			System.err.println("Promotion failed for ID " + entry.getWaitingId() + ": " + e.getMessage());
		}
		return false;
	}

	public void stopThread() {
		this.running = false;
		this.interrupt();
	}
}