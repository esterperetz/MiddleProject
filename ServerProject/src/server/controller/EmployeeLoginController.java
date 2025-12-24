package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import DAO.EmployeeDAO;
import entities.*;
import ocsf.server.ConnectionToClient;

public class EmployeeLoginController {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    /** Processes manager authentication requests. */
    public void handle(Request req, ConnectionToClient client) throws IOException {
        try {
            switch (req.getAction()) {
                case LOGIN:
                    processLogin(req, client);
                    break;
                case LOGOUT:
//                    processLogout(req, client);
                    break;
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

//    private void processLogout(Request req, ConnectionToClient client) throws SQLException, IOException {
//        if (req.getId() != null) {
//            employeeDAO.updateLoginStatus(req.getId(), false);
//            client.sendToClient(new Response(ResourceType.EMPLOYEE, ActionType.LOGOUT, 
//                Response.ResponseStatus.SUCCESS, "Staff Logged Out", null));
//        }
//    }
}