package server.controller;


import java.io.IOException;
import java.sql.SQLException;
import DAO.EmployeeDAO;
import entities.*;
import ocsf.server.ConnectionToClient;

public class EmployeeController {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

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