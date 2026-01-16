package server.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import entities.Customer;
import entities.Employee;
import entities.Order;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class EmailService {
    private static String plainTextBody;
    private static String apiKey;
    private static Email from;
    private static Request request;
    private static SendGrid sg;

    private static void setService() {
    	
        apiKey = System.getenv("SENDGRID_API_KEY"); 

        from = new Email("systembistro@gmail.com"); // Gmail password : Bistro123456
        sg = new SendGrid(apiKey);
        request = new Request();
    }

    public static void sendConfirmation(Customer customer, Order order) {

        String subject = "Confirmation booking in Bistro";
        Email to = new Email(customer.getEmail());

        // פורמטים לעיצוב התאריך והשעה
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        // יצירת מחרוזת המשלבת תאריך ושעה מתוך orderDate
        String formattedOrderDate = "To be assigned";
        if (order.getOrderDate() != null) {
            // כאן אנחנו שואבים גם את התאריך וגם את השעה מאותו אובייקט Date
            formattedOrderDate = dateFormat.format(order.getOrderDate()) + " at " + timeFormat.format(order.getOrderDate());
        }

        plainTextBody = String.format(
                "Hello %s,\n\n" +
                        "Your reservation at BISTRO has been successfully confirmed!\n\n" +
                        "--- Reservation Details ---\n" +
                        "Confirmation Code: %d\n" +
                        "Scheduled Date & Time: %s\n" + // שינוי הכותרת לתיאור מדויק יותר
                        "Arrival Status: %s\n" +
                        "Number of Guests: %d\n" +
                        "Table Number: %s\n\n" +
                        "Phone Number: %s\n\n" +
                        "If you wish to cancel or modify your reservation, please contact us!\n\n" +
                        "We look forward to seeing you!\n" +
                        "Farewell, Bistro Team.",
                customer.getName(),
                order.getConfirmationCode(),
                formattedOrderDate, // המשתנה החדש שכולל תאריך + שעה
                order.getArrivalTime() != null ? timeFormat.format(order.getArrivalTime()) : "Not arrived yet",
                order.getNumberOfGuests(),
                (order.getTableNumber() != null ? order.getTableNumber() : "To be assigned"),
                customer.getPhoneNumber());

        Content content = new Content("text/plain", plainTextBody);
        setService();
        Mail mail = new Mail(from, subject, to, content);

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Your mail has been sent successfully!");
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }
    }

    public static void sendCancelation(Customer customer, Order order) {

        String subject = "Cancelation booking in Bistro";
        Email to = new Email(customer.getEmail());

        // תוכן המייל
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        String formattedOrderDate = "To be assigned";
        if (order.getOrderDate() != null) {
            // כאן אנחנו שואבים גם את התאריך וגם את השעה מאותו אובייקט Date
            formattedOrderDate = dateFormat.format(order.getOrderDate()) + " at " + timeFormat.format(order.getOrderDate());
        }

        plainTextBody = String.format(
                "Hello %s,\n\n" +
                        "Your reservation at BISTRO has been cancelled :( \n\n" +
                        "--- Reservation Details ---\n" +
                        "Scheduled Date & Time: %s\n" +
                        "Number of Guests: %d\n" +
                        "Phone Number: %s\n\n" +
                        "If you wish to modify your reservation, please contact us!\n\n" +
                        "We look forward to seeing you!\n" +
                        "Farewell, Bistro Team.",
                customer.getName(),
                order.getOrderDate() != null ? formattedOrderDate : "To be assigned",
                order.getNumberOfGuests(),
                customer.getPhoneNumber());

        Content content = new Content("text/plain", plainTextBody);
        setService();
        Mail mail = new Mail(from, subject, to, content);

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Your mail has been sent succsesfully!");
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }
    }

    public static void sendEmail(String customerEmail, Employee employee) {

        String subject = "account creation in Bistro System";
        Email to = new Email(customerEmail);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        plainTextBody = String.format(
                "Hello %s %s,\n\n" +
                        "Your account at BISTRO has been created succsefully! :) \n\n" +
                        "--- Account Details ---\n" +
                        "User Name: %s\n" +
                        "Temporary Password: %s\n" +
                        "Phone Number: %s\n\n" +
                        "If you wish to modify your temporary password, please login-in to Bistro system!\n\n",
                employee.getRole().toString().toLowerCase(),
                employee.getUserName(),
                employee.getUserName(),
                employee.getPassword(),
                employee.getPhoneNumber());

        Content content = new Content("text/plain", plainTextBody);
        setService();
        Mail mail = new Mail(from, subject, to, content);

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Your mail has been sent succsesfully!");
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }

    }
    
    public static void sendEmailToSubscriber(Customer customer) {

        String subject = "Subscriber creation in Bistro System";
        Email to = new Email(customer.getEmail());

        String qrCodeBase64 = generateQRCodeBase64(String.valueOf(customer.getSubscriberCode()));

        String htmlBody = String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                    "<h2>Hello %s,</h2>" +
                    "<p>Your subscription at <b>BISTRO</b> has been created successfully! :)</p>" +
                    "<hr>" +
                    "<h3>--- Subscriber Details ---</h3>" +
                    "<p><b>Subscriber Code:</b> %s</p>" +
                    "<p><b>Phone Number:</b> %s</p>" +
                    "<br>" +
                    "<div style='text-align: center; border: 1px solid #ddd; padding: 10px; display: inline-block;'>" +
                        "<p style='margin: 0 0 10px 0;'>Scan your Code:</p>" +
                        "<img src=\"cid:qr-image\" alt=\"QR Code\" width=\"150\" height=\"150\" />" +
                    "</div>" +
                    "<br><br>" +
                    "<p>If you wish to ask any question, please do not hesitate!</p>" +
                    "<p>Farewell, <br>Bistro Team.</p>" +
                "</div>",
                customer.getName(),
                customer.getSubscriberCode(),
                customer.getPhoneNumber()
        );
        
        plainTextBody = htmlBody;
        Content content = new Content("text/html", htmlBody);
        
        setService();

        Mail mail = new Mail(from, subject, to, content);

        if (qrCodeBase64 != null) {
            Attachments attachments = new Attachments();
            attachments.setContent(qrCodeBase64);
            attachments.setType("image/png");
            attachments.setFilename("qrcode.png");
            attachments.setDisposition("inline");  
            attachments.setContentId("qr-image");   
            mail.addAttachments(attachments);
        }

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Your mail has been sent successfully!");
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }
    }

   
    private static String generateQRCodeBase64(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            System.err.println("Could not generate QR code: " + e.getMessage());
            return null;
        }
    }


    public static String getContent() {
        return plainTextBody;
    }

    public static void sendReminder(Order order) {
        if (order.getCustomer().getEmail() == null || order.getCustomer().getEmail().isEmpty()) {
            System.err.println("Cannot send reminder: Email is missing for order " + order.getOrderNumber());
            return;
        }

        String subject = "Reminder: Your reservation at BISTRO is in 2 hours";
        Email to = new Email(order.getCustomer().getEmail());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        plainTextBody = String.format(
                "Hello %s,\n\n" +
                        "This is a friendly reminder that your reservation at BISTRO is coming up in approximately 2 hours.\n\n"
                        +
                        "--- Reservation Details ---\n" +
                        "Time: %s on %s\n" +
                        "Number of Guests: %d\n" +
                        "Table Number: %s\n\n" +
                        "We look forward to seeing you soon!\n\n" +
                        "If you need to make changes, please contact us.\n" +
                        "Farewell, Bistro Team.",
                (order.getCustomer().getName() != null ? order.getCustomer().getName() : "Guest"),
                (order.getArrivalTime() != null ? timeFormat.format(order.getArrivalTime()) : "Scheduled time"),
                (order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "Today"),
                order.getNumberOfGuests(),
                (order.getTableNumber() != null ? order.getTableNumber() : "TBD"));

        Content content = new Content("text/plain", plainTextBody);
        setService();
        Mail mail = new Mail(from, subject, to, content);

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Reminder email sent successfully to " + order.getCustomer().getEmail());
            } else {
                System.out.println("Error in Sending Reminder: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication (Reminder): " + ex.getMessage());
        }
    }
}
