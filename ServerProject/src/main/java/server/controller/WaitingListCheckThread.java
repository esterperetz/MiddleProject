package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import DAO.WaitingListDAO;
import DAO.TableDAO;
import DAO.OrderDAO;
import entities.WaitingList;
import java.util.Collections;
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
				// בדיקה כל דקה (60000 מילישניות)
				Thread.sleep(60000);
				processWaitingList();
			} catch (InterruptedException e) {
				running = false;
			}
		}
	}

//    private void processWaitingList() {
//        try {
//            List<WaitingList> waitingEntries = waitingListDao.getAllWaitingList();
//            if (waitingEntries.isEmpty()) return;
//
//            Date now = new Date();
//            
//            // מפה למעקב אחרי כמה אנשים קידמנו בסבב הנוכחי
//            // Key: כמות האורחים, Value: כמה הזמנות כאלו קידמנו הרגע
//            Map<Integer, Integer> promotedInThisLoop = new HashMap<>();
//
//            for (WaitingList entry : waitingEntries) {
//                int guests = entry.getNumberOfGuests();
//                
//                // שימוש בלוגיקה החכמה (המפל) שבודקת חפיפות בין שולחנות קטנים לגדולים
//                if (isSpaceAvailable(now, guests, promotedInThisLoop)) {
//                    
//                    // יש מקום! נקדם להזמנה
//                    boolean success = promoteEntry(entry);
//                    
//                    if (success) {
//                        // עדכון המונה כדי שהבא בתור לא יתפוס את אותו מקום וירטואלי
//                        promotedInThisLoop.put(guests, promotedInThisLoop.getOrDefault(guests, 0) + 1);
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("WaitingList Thread Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

	private void processWaitingList() {
		try {
			List<WaitingList> entries = waitingListDao.getAllWaitingList();

			// רשימה לשמירת האנשים שקידמנו *בתוך הלולאה הזו*
			// זה משמש כ-Cache זמני עד שהלולאה מסתיימת והנתונים נשמרים באמת ב-DB
			List<WaitingList> promotedThisCycle = new ArrayList<>();
			//Date promotedDate=null;
			for (WaitingList entry : entries) {
				int guests = entry.getNumberOfGuests();

				// 1. קביעת התאריך המבוקש להזמנה הנוכחית
				Date requestedDate = entry.getReservationDate();
				if (requestedDate == null) {
					requestedDate = new Date(); // עכשיו
				}

				// 2. כמה שולחנות מתאימים יש בכלל במסעדה?
				// int totalSuitableTables = tableDao.countSuitableTables(guests);
				int totalSuitableTables = tableDao.countTotalPhysicalTables(guests);
				// 3. כמה הזמנות מתנגשות יש *במסד הנתונים*?
				int dbConflicts = orderDao.countConflictingOrders(requestedDate, guests);
				// 4. --- התוספת החדשה: חישוב התנגשויות פנימיות ---
				// נבדוק כמה אנשים קידמנו *הרגע* (ב-promotedThisCycle) שמתנגשים עם הבקשה הזו
				int localConflicts = 0;
				
				for (WaitingList promoted : promotedThisCycle) {
					// נשתמש באותו היגיון כמו ב-SQL:
					// אם ההזמנה שקידמנו גדולה/שווה להזמנה הנוכחית + הזמנים חופפים -> זה תופס מקום

					Date promotedDate = promoted.getReservationDate();
					if (promotedDate == null)
						promotedDate = new Date();

					if (promoted.getNumberOfGuests() >= guests && isOverlapping(promotedDate, requestedDate)) {
						localConflicts++;
					}
				}
				System.out.println("Date: " + requestedDate + " totalTables : " + totalSuitableTables + " dbConflicts: "
						+ dbConflicts + " localConflicts: " + localConflicts);
				// 5. חישוב סופי: סך הכל - (תפוסים ב-DB + תפוסים וירטואלית בלולאה)
				if ((totalSuitableTables > (dbConflicts + localConflicts))) {
					List<Integer> table_list= tableDao.getAllTableCapacities2();
					List<Integer> orders = orderDao.getActiveOrderSizes2(requestedDate);
					System.out.println("table list: "+table_list.toString());
					System.out.println("order list: "+orders.toString());
					System.out.println(""+isValidRemoveWaitingList(table_list,orders));
					if (isValidRemoveWaitingList(table_list,orders)) {
						boolean success = promoteEntry(entry);
						//
						// table:[4,2] -> isOccpuid=0
						// [4,3]
						//
						//
						if (success) {
							System.out.println("Promoted waiting list entry " + entry.getWaitingId());

							// חשוב מאוד: מוסיפים את הממתין לרשימה הפנימית
							// כדי שהאיטרציה הבאה בלולאה "תדע" שהמקום הזה נתפס
							promotedThisCycle.add(entry);
						}
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("WaitingList Thread Error: " + e.getMessage());
		}
	}

	// --- מתודת עזר לבדיקת חפיפת זמנים (אותו היגיון כמו ה-SQL) ---
	private boolean isOverlapping(Date date1, Date date2) {
		long time1 = date1.getTime();
		long time2 = date2.getTime();
		long twoHours = 2 * 60 * 60 * 1000; // שעתיים במילי-שניות

		// טווח 1: מתחיל ב-time1, נגמר ב-time1 + שעתיים
		// טווח 2: מתחיל ב-time2, נגמר ב-time2 + שעתיים

		// חפיפה מתקיימת אם: (Start1 < End2) וגם (End1 > Start2)
		return (time1 < time2 + twoHours) && (time1 + twoHours > time2);
	}

	private boolean isValidRemoveWaitingList(List<Integer> table_list, List<Integer> orders) {
		if (table_list.size() < orders.size()) {
			System.out.println("The size of lists are different!!");
			return false;
		}
		//table :[2,4]
		//[3,4]
		table_list.sort(Comparator.reverseOrder());
		orders.sort(Comparator.reverseOrder());
		for (int i = table_list.size()-1; i >= 0; i--) {
			if (!(table_list.get(i) >= orders.get(i))) {
				return false;
			}
		}
		return true;
	}

//	/**
//	 * פונקציה חכמה לבדיקת זמינות (Waterfall Logic) לוקחת בחשבון גם את ה-DB וגם את
//	 * מה שקידמנו הרגע בתוך הלולאה
//	 */
//	private boolean isSpaceAvailable(Date date, int guests, Map<Integer, Integer> promotedInThisLoop)
//			throws SQLException {
//		// שולף את הגדלים (למשל: 2, 4). חייב להיות ממוין עולה!
//		List<Integer> allSizes = tableDao.getAllTableCapacities();
//		if (allSizes == null || allSizes.isEmpty())
//			return false;
//
//		// מתחילים מ-0 כדי לתפוס את השולחן הכי קטן
//		int prevSize = 0;
//
//		for (int currentTableSize : allSizes) {
//			// החישוב הקריטי:
//			// עבור שולחן בגודל 2 -> הסף הוא 1 (כל מי שגדול מ-0).
//			// עבור שולחן בגודל 4 -> הסף הוא 3 (כל מי שגדול מ-2).
//			int threshold = prevSize + 1;
//
//			// 1. היצע: כמה שולחנות יש בגודל הזה ומעלה?
//			// (למשל עבור גודל 4 -> יש 1. עבור גודל 2 -> יש 2).
//			int supply = tableDao.countSuitableTables(currentTableSize);
//
//			// 2. ביקוש: כמה הזמנות ב-DB יש להן 'threshold' אורחים ומעלה?
//			// כאן התיקון הגדול: הזמנה של 3 אנשים (שהיא >= 3) תיספר כאן!
//			int activeDemand = orderDao.countActiveOrdersInTimeRange(date, threshold);
//
//			// 3. כמה הזמנות קידמנו הרגע בלולאה הנוכחית?
//			int promotedDemand = countRelevantPromotions(promotedInThisLoop, threshold);
//
//			// 4. הבקשה הנוכחית שלי (מה-WaitingList) - האם אני צריך שולחן בגודל הזה?
//			// אם אני 4 אורחים, והסף הוא 3 -> כן, אני תורם לביקוש.
//			int myDemand = (guests >= threshold) ? 1 : 0;
//
//			int totalDemand = activeDemand + promotedDemand + myDemand;
//
//			// אם הביקוש עולה על ההיצע ברמה הזו - אין מקום!
//			if (supply < totalDemand) {
//				return false;
//			}
//
//			// שומרים את הגודל הנוכחי כדי לחשב את הסף לשולחן הבא
//			prevSize = currentTableSize;
//		}
//
//		return true;
//	}

	/**
	 * פונקציית עזר: סופרת כמה הזמנות קידמנו בסבב הזה שמתחרות על שולחן בגודל minSize
	 */
	private int countRelevantPromotions(Map<Integer, Integer> promotedMap, int minGuests) {
		int count = 0;
		for (Map.Entry<Integer, Integer> entry : promotedMap.entrySet()) {
			// בודקים אם כמות האורחים בהזמנה שקודמה גדולה או שווה לסף
			if (entry.getKey() >= minGuests) {
				count += entry.getValue();
			}
		}
		return count;
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