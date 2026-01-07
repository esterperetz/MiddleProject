package server.controller;


import java.io.IOException;
import java.sql.SQLException;

import DAO.CustomerDAO;
import DAO.EmployeeDAO;
import entities.*;
import ocsf.server.ConnectionToClient;

public class EmployeeController {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    /** Processes manager authentication requests. */
    public void handle(Request req, ConnectionToClient client) throws IOException {
        try {
            switch (req.getAction()) {
                case LOGIN:
                    processLogin(req, client);
                    break;
                case REGISTER_EMPLOYEE:
                	processRegister(req,client);  
                	break;
                case REGISTER_SUBSCRIBER:
                	processRegisterSubscriber(req,client);
                	break;
                case UPDATE:
                	processupdateEmploye(req,client);
                	break; //added break here.
                default:
                    client.sendToClient(new Response(req.getResource(), req.getAction(), 
                        Response.ResponseStatus.ERROR, "Unsupported Action", null));
                    break;
            }
        } catch (SQLException e) {
            client.sendToClient(new Response(req.getResource(), req.getAction(), 
                Response.ResponseStatus.DATABASE_ERROR, "DB Error: " + e.getMessage(), null));
        }
    }

    private void processRegisterSubscriber(Request req, ConnectionToClient client) {
    	int code;
		boolean isUnique = false;
		Customer customer = (Customer) req.getPayload();
		
		Customer existing = customerDAO.getCustomerByEmail(customer.getEmail());
		if (customer.getType() == CustomerType.SUBSCRIBER && existing != null) {
			try {
				client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
						Response.ResponseStatus.ERROR, "Error: Email already exists.", null));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		// לולאה שרצה עד שנמצא מספר פנוי
		do {
			// הגרלת מספר (למשל מספר בן 5 ספרות: 10000 עד 99999)
			code = 10000 + (int) (Math.random() * 90000);
			// בדיקה מול ה-DB אם המספר הזה כבר קיים
			if ( customerDAO.getCustomerBySubscriberCode(code) == null) {
					isUnique = true;
				}
		} while (!isUnique);
				customer.setSubscriberCode(code);
				boolean success = employeeDAO.createSubscriber(customer);
				if(success) {
					try {
						EmailService.sendEmailToSubscriber(customer);
						System.out.println(EmailService.getContent());
						client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
							Response.ResponseStatus.SUCCESS, "Created Subscriber with id: " + customer.getCustomerId(), customer));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				else {
					try {
						client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_SUBSCRIBER,
								Response.ResponseStatus.ERROR, "Error: Couldnt create subscriber.", null));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
		}
	}

	private void processLogin(Request req, ConnectionToClient client) throws SQLException, IOException {
        Employee credentials = (Employee) req.getPayload();
       try { Employee authorized = employeeDAO.login(credentials.getUserName(), credentials.getPassword());
        
        
        if (authorized != null) {
            client.sendToClient(new Response(ResourceType.EMPLOYEE, ActionType.LOGIN, 
                Response.ResponseStatus.SUCCESS, "Manager Team Auth Successful", authorized));
        } else {
            client.sendToClient(new Response(ResourceType.EMPLOYEE, ActionType.LOGIN, 
                Response.ResponseStatus.UNAUTHORIZED, "Invalid creds or already logged in", null));
        }
       }catch(Exception e) {
    	   e.printStackTrace();
       }
    }
    
    private void processRegister(Request req, ConnectionToClient client) throws SQLException, IOException {
        Employee credentials = (Employee) req.getPayload();
        
        Employee existing = employeeDAO.checkIfUsernameIsAlreadyTaken(credentials.getUserName());
		if (existing != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.REGISTER_EMPLOYEE,
					Response.ResponseStatus.ERROR, "Error: Username already taken.", null));
			return;
		}
		
       try { boolean success = employeeDAO.createEmployee(credentials);
        
        
        if (success) {
        	EmailService.sendEmail(credentials.getEmail(),credentials);
            client.sendToClient(new Response(ResourceType.EMPLOYEE, ActionType.REGISTER_EMPLOYEE, 
                Response.ResponseStatus.SUCCESS, "Manager Team Auth Successful, email details: "+ EmailService.getContent(), success));
        } else {
            client.sendToClient(new Response(ResourceType.EMPLOYEE, ActionType.REGISTER_EMPLOYEE, 
                Response.ResponseStatus.ERROR, "Error: Failed to create employee in DB", null));
        }
       }catch(Exception e) {
    	   e.printStackTrace();
       }
    }
    
	private void processupdateEmploye(Request req, ConnectionToClient client) throws IOException, SQLException {
		Employee subToUpdate = (Employee) req.getPayload();
		boolean success = employeeDAO.updateEmployeeDetails(subToUpdate);

		if (success) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Success: Employee updated.", null));
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: Failed to update subsEmployeecriber.", null));
		}
	}

}