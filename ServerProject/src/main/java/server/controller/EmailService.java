package server.controller;


import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import entities.Customer;
import entities.Employee;
import entities.Order;
import java.io.IOException;
import java.text.SimpleDateFormat;


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
    	
    	if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            System.err.println("Error: Cannot send email. Customer email is missing.");
            return; 
        }
        String subject = "Confirmation booking in Bistro";
        Email to = new Email(customer.getEmail());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        String formattedOrderDate = "To be assigned";
        if (order.getOrderDate() != null) {
            formattedOrderDate = dateFormat.format(order.getOrderDate()) + " at "
                    + timeFormat.format(order.getOrderDate());
        }

        plainTextBody = String.format(
                "Hello %s,\n\n" +
                        "Your reservation at BISTRO has been successfully confirmed!\n\n" +
                        "--- Reservation Details ---\n" +
                        "Confirmation Code: %d\n" +
                        "Scheduled Date & Time: %s\n" +
                        "Arrival Status: %s\n" +
                        "Number of Guests: %d\n" +
                        "Table Number: %s\n\n" +
                        "Phone Number: %s\n\n" +
                        "If you wish to cancel or modify your reservation, please contact us!\n\n" +
                        "We look forward to seeing you!\n" +
                        "Farewell, Bistro Team.",
                customer.getName(),
                order.getConfirmationCode(),
                formattedOrderDate,
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
                System.out.println("Your mail has been sent succsesfully! to "+ customer.getEmail());
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }
    }

    public static void sendCancelation(Customer customer, Order order) {
    	
    	if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            System.err.println("Error: Cannot send email. Customer email is missing.");
            return; 
        }
        String subject = "Cancelation booking in Bistro";
        Email to = new Email(customer.getEmail());
        
        // תוכן המייל
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        String formattedOrderDate = "To be assigned";
        if (order.getOrderDate() != null) {
            formattedOrderDate = dateFormat.format(order.getOrderDate()) + " at "
                    + timeFormat.format(order.getOrderDate());
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
                System.out.println("Your mail has been sent succsesfully! to "+ customer.getEmail());
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }
    }

    public static void sendEmail(String employeeEmail, Employee employee) {
    	
    	if (employee == null || employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            System.err.println("Error: Cannot send email. Customer email is missing.");
            return; 
        }
        String subject = "account creation in Bistro System";
        Email to = new Email(employeeEmail);

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
                System.out.println("Your mail has been sent succsesfully! to "+ employeeEmail);
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
        }

    }

    public static void sendEmailToSubscriber(Customer customer) {
    	
    	if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            System.err.println("Error: Cannot send email. Customer email is missing.");
            return; 
        }
        String subject = "Subscriber creation in Bistro System";
        Email to = new Email(customer.getEmail());

        String qrCodeBase64 = QRcodeScanner.generateQRCodeBase64(String.valueOf(customer.getSubscriberCode()));

        String htmlBody = String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                        "<h2>Hello %s,</h2>" +
                        "<p>Your subscription at <b>BISTRO</b> has been created successfully! :)</p>" +
                        "<hr>" +
                        "<h3>--- Subscriber Details ---</h3>" +
                        "<p><b>Subscriber Code:</b> %s</p>" +
                        "<p><b>Phone Number:</b> %s</p>" +
                        "<br>" +
                        "<div style='text-align: center; border: 1px solid #ddd; padding: 10px; display: inline-block;'>"
                        +
                        "<p style='margin: 0 0 10px 0;'>Scan your Code:</p>" +
                        "<img src=\"cid:qr-image\" alt=\"QR Code\" width=\"150\" height=\"150\" />" +
                        "</div>" +
                        "<br><br>" +
                        "<p>If you wish to ask any question, please do not hesitate!</p>" +
                        "<p>Farewell, <br>Bistro Team.</p>" +
                        "</div>",
                customer.getName(),
                customer.getSubscriberCode(),
                customer.getPhoneNumber());

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
                System.out.println("Your mail has been sent succsesfully! to "+ customer.getEmail());
            } else {
                System.out.println("Error in Sending: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication: " + ex.getMessage());
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

    public static void sendReceipt(Customer fullCustomer, Order order) {
        String subject = "Receipt for your visit at BISTRO";
        Email to = new Email(fullCustomer.getEmail());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        plainTextBody = String.format(
                "Hello %s,\n\n" + "Thank you for dining with us at BISTRO!\n\n" + "--- Receipt Details ---\n"
                        + "Order Number: %d\n" + "Date: %s\n" + "Total Price: $%.2f$\n\n"
                        + "We hope to see you again soon!\n" + "Bistro Team.",
                fullCustomer.getName(), order.getOrderNumber(),
                order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "N/A", order.getTotalPrice());

        Content content = new Content("text/plain", plainTextBody);
        setService();
        Mail mail = new Mail(from, subject, to, content);

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Receipt email sent successfully to " + fullCustomer.getEmail());
            } else {
                System.out.println("Error in Sending Receipt: " + response.getBody());
            }
        } catch (IOException ex) {
            System.err.println("Error in communication (Receipt): " + ex.getMessage());
        }
    }
}
