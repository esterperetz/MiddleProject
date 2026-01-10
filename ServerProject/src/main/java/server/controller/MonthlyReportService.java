package server.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import entities.Order;
import entities.WaitingList;
import entities.CustomerType;

public class MonthlyReportService {

    public File generateHtmlReport(int month, int year, List<Order> orders, List<WaitingList> waitingList) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // --- משתנים לחישוב סטטיסטיקות ---
        double totalRevenue = 0;
        int subscribers = 0;
        int regulars = 0;
        int lateOrders = 0;
        
        // מערך המונה כניסות לפי שעות (0 עד 23) לגרף העמודות
        int[] hourlyActivity = new int[24];
        Arrays.fill(hourlyActivity, 0);

        Calendar cal = Calendar.getInstance();

        // --- לולאת חישוב נתונים ---
        for (Order o : orders) {
            if (o.getOrderStatus() != Order.OrderStatus.CANCELLED) {
                totalRevenue += o.getTotalPrice();
                
                // ספירת סוגי לקוחות (לגרף עוגה)
                if (o.getCustomer() != null && o.getCustomer().getType() == CustomerType.SUBSCRIBER) {
                    subscribers++;
                } else {
                    regulars++;
                }

                // בדיקת איחור ואיסוף שעות
                if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                    long diff = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                    if (diff > 15 * 60 * 1000) lateOrders++; // איחור מעל 15 דקות
                    
                    // חישוב שעת הגעה (לגרף עמודות)
                    cal.setTime(o.getArrivalTime());
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    if (hour >= 0 && hour < 24) {
                        hourlyActivity[hour]++;
                    }
                }
            }
        }

        int totalWaiting = (waitingList != null) ? waitingList.size() : 0;

        // === התחלת ה-HTML ===
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<title>Monthly Report</title>");
        
        // --- הוספת ספריית Chart.js (חובה לגרפים) ---
        sb.append("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");

        // === CSS ===
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', sans-serif; background-color: #f4f7f6; color: #333; padding: 20px; }");
        sb.append(".container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }");
        sb.append("h1 { text-align: center; color: #2c3e50; }");
        sb.append(".subtitle { text-align: center; color: #7f8c8d; margin-bottom: 40px; }");
        
        // עיצוב כרטיסיות (Cards)
        sb.append(".cards { display: flex; justify-content: space-between; margin-bottom: 30px; gap: 10px; }");
        sb.append(".card { flex: 1; background: #ecf0f1; padding: 20px; border-radius: 8px; text-align: center; }");
        sb.append(".card .value { font-size: 24px; font-weight: bold; color: #2c3e50; }");
        sb.append(".card.green { border-top: 4px solid #2ecc71; }");
        sb.append(".card.blue { border-top: 4px solid #3498db; }");
        sb.append(".card.orange { border-top: 4px solid #e67e22; }");
        sb.append(".card.red { border-top: 4px solid #e74c3c; }");
        
        // --- עיצוב אזור הגרפים ---
        sb.append(".charts-section { display: flex; justify-content: space-between; gap: 20px; margin-bottom: 40px; }");
        sb.append(".chart-container { width: 48%; background: #fff; border: 1px solid #eee; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }");

        // עיצוב טבלאות
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; margin-bottom: 40px; }");
        sb.append("th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }");
        sb.append("th { background-color: #34495e; color: white; }");
        sb.append(".late { color: #e74c3c; font-weight: bold; }"); // עיצוב לאיחורים
        
        sb.append(".bar-container { background-color: #ecf0f1; width: 100%; border-radius: 4px; height: 20px; }");
        sb.append(".bar-sub { background-color: #3498db; height: 100%; border-radius: 4px; text-align: center; color: white; font-size: 12px; }");
        
        sb.append("</style>");
        sb.append("</head><body>");

        // === גוף הדוח ===
        sb.append("<div class='container'>");
        sb.append("<h1>Monthly Operations Report</h1>");
        sb.append("<p class='subtitle'>Period: ").append(month).append("/").append(year).append("</p>");

        // 1. כרטיסיות סיכום
        sb.append("<div class='cards'>");
        sb.append("<div class='card green'><h3>Total Revenue</h3><div class='value'>₪").append(String.format("%.2f", totalRevenue)).append("</div></div>");
        sb.append("<div class='card blue'><h3>Total Orders</h3><div class='value'>").append(orders.size()).append("</div></div>");
        sb.append("<div class='card orange'><h3>Waiting List</h3><div class='value'>").append(totalWaiting).append("</div></div>");
        sb.append("<div class='card red'><h3>Late Arrivals</h3><div class='value'>").append(lateOrders).append("</div></div>");
        sb.append("</div>");

        // --- 2. אזור הגרפים ---
        sb.append("<h2>Visual Analysis</h2>");
        sb.append("<div class='charts-section'>");
        sb.append("<div class='chart-container'><canvas id='pieChart'></canvas></div>");
        sb.append("<div class='chart-container'><canvas id='barChart'></canvas></div>");
        sb.append("</div>"); 

        // --- 3. טבלאות הנתונים ---
        
        // === שינוי כאן: טבלת הזמנות מפורטת עם זמנים ועיכובים ===
        sb.append("<h2>1. Order Performance (Arrivals & Delays)</h2>");
        sb.append("<table><thead><tr><th>ID</th><th>Customer</th><th>Ordered For</th><th>Actual Arrival</th><th>Delay</th><th>Total</th><th>Status</th></tr></thead><tbody>");
        
        for (Order o : orders) {
            String name = (o.getCustomer() != null) ? o.getCustomer().getName() : "Guest";
            
            String orderedFor = fmt.format(o.getOrderDate());
            String arrivalTime = (o.getArrivalTime() != null) ? fmt.format(o.getArrivalTime()) : "-";
            
            // חישוב עיכוב להצגה בטבלה
            String delayStr = "0 min";
            String rowClass = "";
            
            if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                long diffMillis = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                long diffMinutes = diffMillis / (60 * 1000);
                
                if (diffMinutes > 0) {
                    delayStr = "+" + diffMinutes + " min";
                    if (diffMinutes > 15) rowClass = "class='late'"; // צביעה באדום אם איחור גדול
                } else if (diffMinutes < 0) {
                    delayStr = diffMinutes + " min (Early)";
                }
            }

            sb.append("<tr>");
            sb.append("<td>").append(o.getOrderNumber()).append("</td>");
            sb.append("<td>").append(name).append("</td>");
            sb.append("<td>").append(orderedFor).append("</td>");
            sb.append("<td>").append(arrivalTime).append("</td>");
            sb.append("<td ").append(rowClass).append(">").append(delayStr).append("</td>"); // עמודת העיכוב
            sb.append("<td>₪").append(o.getTotalPrice()).append("</td>");
            sb.append("<td>").append(o.getOrderStatus()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");

        // טבלת רשימת המתנה (נשאר ללא שינוי)
        sb.append("<h2>2. Waiting List Log</h2>");
        if (waitingList != null && !waitingList.isEmpty()) {
            sb.append("<table><thead><tr><th>Entered At</th><th>Customer</th><th>Guests</th><th>Group Size</th></tr></thead><tbody>");
            for (WaitingList w : waitingList) {
                String wName = (w.getCustomer() != null) ? w.getCustomer().getName() : "Anonymous";
                int width = Math.min(w.getNumberOfGuests() * 10, 100); 
                
                sb.append("<tr>");
                sb.append("<td>").append(fmt.format(w.getEnterTime())).append("</td>");
                sb.append("<td>").append(wName).append("</td>");
                sb.append("<td>").append(w.getNumberOfGuests()).append("</td>");
                sb.append("<td><div class='bar-container'><div class='bar-sub' style='width:").append(width).append("%;'>").append(w.getNumberOfGuests()).append("</div></div></td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        } else {
            sb.append("<p>No waiting list entries recorded.</p>");
        }

        sb.append("</div>"); // סגירת container

        // === סקריפט JS ליצירת הגרפים ===
        sb.append("<script>");
        
        // יצירת גרף עוגה (לקוחות)
        sb.append("const ctxPie = document.getElementById('pieChart').getContext('2d');");
        sb.append("new Chart(ctxPie, { type: 'doughnut', data: {");
        sb.append("  labels: ['Subscribers', 'Regular Customers'],");
        sb.append("  datasets: [{ data: [").append(subscribers).append(", ").append(regulars).append("],");
        sb.append("    backgroundColor: ['#2ecc71', '#95a5a6'] }]");
        sb.append("}, options: { plugins: { title: { display: true, text: 'Customer Segmentation' } } } });");

        // יצירת גרף עמודות (שעות עומס)
        sb.append("const ctxBar = document.getElementById('barChart').getContext('2d');");
        sb.append("new Chart(ctxBar, { type: 'bar', data: {");
        sb.append("  labels: [");
        for(int i=0; i<24; i++) sb.append("'").append(String.format("%02d:00", i)).append("',");
        sb.append("],");
        sb.append("  datasets: [{ label: 'Arrivals Count', data: [");
        for(int i=0; i<24; i++) sb.append(hourlyActivity[i]).append(",");
        sb.append("], backgroundColor: '#3498db', borderRadius: 4 }]");
        sb.append("}, options: { scales: { y: { beginAtZero: true } }, plugins: { title: { display: true, text: 'Busy Hours (Arrivals)' } } } });");
        
        sb.append("</script>");

        sb.append("</body></html>");

        // === שמירה לקובץ ===
        String fileName = "Report_" + year + "_" + String.format("%02d", month) + ".html";
        File file = new File("server_files/reports/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes("UTF-8"));
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}