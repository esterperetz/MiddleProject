package server.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import entities.Order;
import entities.WaitingList; // <--- Import חדש
import entities.CustomerType;

public class MonthlyReportService {

    // עדכנו את החתימה לקבל גם את רשימת ההמתנה
    public File generateHtmlReport(int month, int year, List<Order> orders, List<WaitingList> waitingList) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // חישוב נתונים כלליים להזמנות
        double totalRevenue = 0;
        int subscribers = 0;
        int lateOrders = 0;
        
        for (Order o : orders) {
            if (o.getOrderStatus() != Order.OrderStatus.CANCELLED) {
                totalRevenue += o.getTotalPrice();
                if (o.getCustomer() != null && o.getCustomer().getType() == CustomerType.SUBSCRIBER) {
                    subscribers++;
                }
                if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                    long diff = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                    if (diff > 15 * 60 * 1000) lateOrders++;
                }
            }
        }

        // חישוב נתונים לרשימת המתנה
        int totalWaiting = (waitingList != null) ? waitingList.size() : 0;

        // === התחלת ה-HTML ===
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<title>Monthly Report</title>");
        
        // === CSS ===
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', sans-serif; background-color: #f4f7f6; color: #333; padding: 20px; }");
        sb.append(".container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }");
        sb.append("h1 { text-align: center; color: #2c3e50; }");
        sb.append(".subtitle { text-align: center; color: #7f8c8d; margin-bottom: 40px; }");
        
        // Cards
        sb.append(".cards { display: flex; justify-content: space-between; margin-bottom: 30px; gap: 10px; }");
        sb.append(".card { flex: 1; background: #ecf0f1; padding: 20px; border-radius: 8px; text-align: center; }");
        sb.append(".card h3 { margin: 0 0 10px 0; font-size: 14px; color: #7f8c8d; }");
        sb.append(".card .value { font-size: 24px; font-weight: bold; color: #2c3e50; }");
        sb.append(".card.green { border-top: 4px solid #2ecc71; }");
        sb.append(".card.blue { border-top: 4px solid #3498db; }");
        sb.append(".card.red { border-top: 4px solid #e74c3c; }");
        sb.append(".card.orange { border-top: 4px solid #e67e22; }"); // צבע חדש לרשימת המתנה
        
        // Tables
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; margin-bottom: 40px; }");
        sb.append("th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }");
        sb.append("th { background-color: #34495e; color: white; }");
        
        // Bar chart styling (Simple CSS bars)
        sb.append(".bar-container { background-color: #ecf0f1; width: 100%; border-radius: 4px; height: 20px; }");
        sb.append(".bar { height: 100%; border-radius: 4px; text-align: center; color: white; font-size: 12px; line-height: 20px; }");
        sb.append(".bar-sub { background-color: #3498db; }");
        sb.append(".bar-reg { background-color: #95a5a6; }");
        
        sb.append("</style>");
        sb.append("</head><body>");

        // === גוף הדוח ===
        sb.append("<div class='container'>");
        sb.append("<h1>Monthly Operations Report</h1>");
        sb.append("<p class='subtitle'>Period: ").append(month).append("/").append(year).append("</p>");

        // כרטיסיות סיכום (הוספנו את הכתום)
        sb.append("<div class='cards'>");
        sb.append("<div class='card green'><h3>Total Revenue</h3><div class='value'>₪").append(String.format("%.2f", totalRevenue)).append("</div></div>");
        sb.append("<div class='card blue'><h3>Total Orders</h3><div class='value'>").append(orders.size()).append("</div></div>");
        sb.append("<div class='card orange'><h3>Waiting List Entries</h3><div class='value'>").append(totalWaiting).append("</div></div>");
        sb.append("<div class='card red'><h3>Late Arrivals</h3><div class='value'>").append(lateOrders).append("</div></div>");
        sb.append("</div>");

        // --- חלק 1: הזמנות ---
        sb.append("<h2>1. Order Performance</h2>");
        sb.append("<table><thead><tr><th>ID</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th></tr></thead><tbody>");
        for (Order o : orders) {
            String name = (o.getCustomer() != null) ? o.getCustomer().getName() : "Guest";
            sb.append("<tr><td>").append(o.getOrderNumber()).append("</td>")
              .append("<td>").append(name).append("</td>")
              .append("<td>").append(fmt.format(o.getOrderDate())).append("</td>")
              .append("<td>₪").append(o.getTotalPrice()).append("</td>")
              .append("<td>").append(o.getOrderStatus()).append("</td></tr>");
        }
        sb.append("</tbody></table>");

        // --- חלק 2: רשימות המתנה (החדש!) ---
        sb.append("<h2>2. Waiting List Analysis</h2>");
        if (waitingList != null && !waitingList.isEmpty()) {
            sb.append("<table><thead><tr><th>Time Entered</th><th>Customer</th><th>Guests</th><th>Group Size Visual</th></tr></thead><tbody>");
            for (WaitingList w : waitingList) {
                String wName = (w.getCustomer() != null) ? w.getCustomer().getName() : "Anonymous";
                
                // חישוב אורך הבר לפי כמות האורחים (סתם ויזואלי)
                int width = Math.min(w.getNumberOfGuests() * 10, 100); 
                
                sb.append("<tr>");
                sb.append("<td>").append(fmt.format(w.getEnterTime())).append("</td>");
                sb.append("<td>").append(wName).append("</td>");
                sb.append("<td>").append(w.getNumberOfGuests()).append("</td>");
                sb.append("<td><div class='bar-container'><div class='bar bar-sub' style='width:").append(width).append("%;'>").append(w.getNumberOfGuests()).append("</div></div></td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        } else {
            sb.append("<p>No waiting list entries recorded for this month.</p>");
        }

        sb.append("</div></body></html>");

        // === שמירה לקובץ (אותו קוד כמו מקודם) ===
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