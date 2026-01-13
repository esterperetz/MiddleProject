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

    private void processWaitingList() {
        try {
            List<WaitingList> waitingEntries = waitingListDao.getAllWaitingList();
            if (waitingEntries.isEmpty()) return;

            Date now = new Date();
            
            // מפה למעקב אחרי כמה אנשים קידמנו בסבב הנוכחי
            // Key: כמות האורחים, Value: כמה הזמנות כאלו קידמנו הרגע
            Map<Integer, Integer> promotedInThisLoop = new HashMap<>();

            for (WaitingList entry : waitingEntries) {
                int guests = entry.getNumberOfGuests();
                
                // שימוש בלוגיקה החכמה (המפל) שבודקת חפיפות בין שולחנות קטנים לגדולים
                if (isSpaceAvailable(now, guests, promotedInThisLoop)) {
                    
                    // יש מקום! נקדם להזמנה
                    boolean success = promoteEntry(entry);
                    
                    if (success) {
                        // עדכון המונה כדי שהבא בתור לא יתפוס את אותו מקום וירטואלי
                        promotedInThisLoop.put(guests, promotedInThisLoop.getOrDefault(guests, 0) + 1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("WaitingList Thread Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * פונקציה חכמה לבדיקת זמינות (Waterfall Logic)
     * לוקחת בחשבון גם את ה-DB וגם את מה שקידמנו הרגע בתוך הלולאה
     */
    private boolean isSpaceAvailable(Date date, int guests, Map<Integer, Integer> promotedInThisLoop) throws SQLException {
        // שולף את הגדלים (למשל: 2, 4). חייב להיות ממוין עולה!
        List<Integer> allSizes = tableDao.getAllTableCapacities(); 
        if (allSizes == null || allSizes.isEmpty()) return false;

        // מתחילים מ-0 כדי לתפוס את השולחן הכי קטן
        int prevSize = 0;

        for (int currentTableSize : allSizes) {
            // החישוב הקריטי: 
            // עבור שולחן בגודל 2 -> הסף הוא 1 (כל מי שגדול מ-0).
            // עבור שולחן בגודל 4 -> הסף הוא 3 (כל מי שגדול מ-2).
            int threshold = prevSize + 1;

            // 1. היצע: כמה שולחנות יש בגודל הזה ומעלה?
            // (למשל עבור גודל 4 -> יש 1. עבור גודל 2 -> יש 2).
            int supply = tableDao.countSuitableTables(currentTableSize);

            // 2. ביקוש: כמה הזמנות ב-DB יש להן 'threshold' אורחים ומעלה?
            // כאן התיקון הגדול: הזמנה של 3 אנשים (שהיא >= 3) תיספר כאן!
            int activeDemand = orderDao.countActiveOrdersInTimeRange(date, threshold);

            // 3. כמה הזמנות קידמנו הרגע בלולאה הנוכחית?
            int promotedDemand = countRelevantPromotions(promotedInThisLoop, threshold);

            // 4. הבקשה הנוכחית שלי (מה-WaitingList) - האם אני צריך שולחן בגודל הזה?
            // אם אני 4 אורחים, והסף הוא 3 -> כן, אני תורם לביקוש.
            int myDemand = (guests >= threshold) ? 1 : 0;

            int totalDemand = activeDemand + promotedDemand + myDemand;

            // אם הביקוש עולה על ההיצע ברמה הזו - אין מקום!
            if (supply < totalDemand) {
                return false;
            }

            // שומרים את הגודל הנוכחי כדי לחשב את הסף לשולחן הבא
            prevSize = currentTableSize;
        }

        return true;
    }

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