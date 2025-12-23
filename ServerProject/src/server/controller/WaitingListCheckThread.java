package server.controller;

import java.util.Date;
import java.util.List;
import DAO.WaitingListDAO;
import Entities.WaitingList;
import DAO.TableDAO;
import DAO.OrderDAO;

/**
 * Background thread following the same style as OrderCleanupThread. Monitors
 * the waiting list and performs automatic promotion.
 */
public class WaitingListCheckThread extends Thread {
	private final WaitingListDAO waitingListDao = new WaitingListDAO();
	private final WaitingListController controller = new WaitingListController();
	private final TableDAO tableDao = new TableDAO();
	private final OrderDAO orderDao = new OrderDAO();
	private boolean running = true;

	@Override
	public void run() {
		while (running) {
			try {
				// Sleep for 1 minute
				Thread.sleep(60000);
				checkWaitingList();
			}
			catch (InterruptedException e) {
				running = false;
			}
		}
	}

	private void checkWaitingList() {
	    try {
	        // Fetch current waiting list entries (FIFO) [cite: 59, 66]
	        List<WaitingList> entries = waitingListDao.getAllWaitingList();

	        for (WaitingList entry : entries) {
	            int guests = entry.getNumberOfGuests(); // [cite: 37]
	            
	            // Get tables with no current diners ('SITTING' or 'ARRIVED') [cite: 40, 51]
	            List<Entities.Table> vacantTables = tableDao.getAvailableTablesNow(guests);
	            
	            // Count 'APPROVED' orders starting within 15 minutes 
	            int reservedSoon = orderDao.countActiveOrdersInTimeRange(new Date(), guests);
	            
	            // Check if enough vacant tables exist after saving spots for reservations
	            if (vacantTables.size() > reservedSoon) {
	                // Assign the first available table from the list 
	                Entities.Table tableToAssign = vacantTables.get(0);
	                
	                // Promote entry and assign the specific table number 
	                boolean promoted = controller.promoteToOrder(entry, tableToAssign.getTableNumber());

	                if (promoted) {
	                    System.out.println("Automation: Promoted " + entry.getFullName() + 
	                            " to Table " + tableToAssign.getTableNumber());
	                    
	                    // Break to allow DB update and prevent double-assignment in one cycle
	                    break; 
	                }
	            } else {
	                System.out.println("Automation: " + entry.getFullName() + 
	                        " waiting - tables reserved for upcoming bookings.");
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("WaitingList thread error: " + e.getMessage());
	    }
	}

	public void stopThread() {
		this.running = false;
		this.interrupt();
	}
}