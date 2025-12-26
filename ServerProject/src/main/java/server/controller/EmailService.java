package server.controller;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import entities.Order;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class EmailService{
	private static String plainTextBody;
	private static String apiKey;
	private static Email from;
	private static Request request;
	private static SendGrid sg;
	
	private static void setService() {
		// משיכת המפתח מהגדרות הווינדוס
        apiKey = System.getenv("SENDGRID_API_KEY"); //need to set it up in ur system*ASK LIEL*
   
        from = new Email("systembistro@gmail.com");  //Gmail password : Bistro123456
        sg = new SendGrid(apiKey);
        request = new Request();
	}
	
	public static void sendConfirmation(String customerEmail, Order order) {
        
        String subject = "Confirmation booking in Bistro";
        Email to = new Email(customerEmail);
    
        // תוכן המייל
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        plainTextBody = String.format(
            "Hello %s,\n\n" +
            "Your reservation at BISTRO has been successfully confirmed!\n\n" +
            "--- Reservation Details ---\n" +
            "Confirmation Code: %d\n" +
            "Date: %s\n" +
            "Arrival Time: %s\n" +
            "Number of Guests: %d\n" +
            "Table Number: %s\n\n" +
            "Phone Number: %s\n\n" +
            "If you wish to cancel or modify your reservation, please contact us!\n\n" +
            "We look forward to seeing you!",
            order.getClientName() ,
            order.getConfirmationCode(),
            order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "To be assigned",
            order.getArrivalTime() != null ? timeFormat.format(order.getArrivalTime()) : "To be assigned",
            order.getNumberOfGuests(),
            (order.getTableNumber() != null ? order.getTableNumber() : "To be assigned"),
            order.getClientPhone()
        );
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
	
	public static void sendCancelation(String customerEmail , Order order){
		
        String subject = "Cancelation booking in Bistro";
        Email to = new Email(customerEmail);
    
        // תוכן המייל
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    

        plainTextBody = String.format(
            "Hello %s,\n\n" +
            "Your reservation at BISTRO has been cancelled :( \n\n" +
            "--- Reservation Details ---\n" +
            "Date: %s\n" +
            "Number of Guests: %d\n" +
            "Phone Number: %s\n\n" +
            "If you wish to modify your reservation, please contact us!\n\n" +
            "We look forward to seeing you!",
            order.getClientName() ,
            order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "To be assigned",
            order.getNumberOfGuests(),
            order.getClientPhone()
        );

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
	public static String getContent() {
		return plainTextBody;
	}
}
