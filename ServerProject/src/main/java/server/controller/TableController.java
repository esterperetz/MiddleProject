package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.CustomerDAO;
import DAO.OrderDAO;
import DAO.TableDAO;
import entities.ActionType;
import entities.Customer;
import entities.Order;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.Table;
import ocsf.server.ConnectionToClient;

/**
 * Controller class responsible for handling Table-related requests.
 * Manages operations such as fetching tables, assigning tables to orders (check-in),
 * and CRUD operations (Create, Update, Delete) for tables.
 */
public class TableController {

    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Main handler method that processes incoming requests for the TABLE resource.
     * Dispatches the request to the specific handler based on the ActionType.
     *
     * @param req    The client's request object.
     * @param client The connection to the client.
     * @throws IOException If an I/O error occurs.
     */
    public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.TABLE) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: Incorrect resource type. Expected TABLE.", null));
            return;
        }

        try {
            switch (req.getAction()) {
                case GET_ALL:
                    handleGetAll(client);
                    break;
                case GET:
                    handleGetTable(req, client);
                    break;
                case CREATE:
                    handleCreate(req, client);
                    break;
                case UPDATE:
                    handleUpdate(req, client);
                    break;
                case DELETE:
                    handleDelete(req, client);
                    break;
                default:
                    client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                            "Error: Unknown action for Table resource.", null));
            }
        } catch (SQLException e) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Database Error: ", e.getMessage()));
        }
    }

    // --- Action Handlers ---

    /**
     * Retrieves all tables from the database and sends them to the client.
     * Also broadcasts the updated list to all connected clients.
     *
     * @param client The client requesting the list.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
    private void handleGetAll(ConnectionToClient client) throws IOException, SQLException {
        List<Table> tables = tableDAO.getAllTables();
        client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
                "get all tables", tables));
        sendTablesToAllClients();
    }

    /**
     * Handles the check-in process for a customer.
     * Verifies the order using confirmation code and subscriber code, finds an available table,
     * and updates the order status to SEATED.
     *
     * @param req    The request containing confirmation code and subscriber ID.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
    private void handleGetTable(Request req, ConnectionToClient client) throws IOException, SQLException {
        int conformationCode = (int) req.getPayload();

        int subscriberCode = (int) req.getId();

        System.out.println("the code is: ------ " + conformationCode);

        Customer customer = new Customer();
        if (subscriberCode != 0) {
            customer = customerDAO.getCustomerBySubscriberCode(subscriberCode);
            if (customer == null) {
                client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
                        "CANOT find coustomerId by subscriber code ", null));
                return;
            }
        }

        Order order = orderDAO.getOrderByConfirmationCodeApproved(conformationCode, customer.getCustomerId());
        if (order == null) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
                    "You arrived too early", null));
            return;
        }

        Integer tableNumber = tableDAO.findAvailableTable(order.getNumberOfGuests());

        if (tableNumber == null) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
                    "NO suitable table is available please wait for notifiaction ", null));
            return;
        }

        boolean updateOrder = orderDAO.updateOrderSeating(order.getOrderNumber(), tableNumber,
                Order.OrderStatus.SEATED);
        boolean update = tableDAO.updateTableStatus(tableNumber, 1);
        if (!update || !updateOrder) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.DATABASE_ERROR,
                    "CANNOT update", null));
            return;
        }
        client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.SUCCESS,
                "get tableNumber", tableNumber));
    }

    /**
     * Creates a new table in the database.
     * Verifies if the table number already exists before adding.
     *
     * @param req    The request containing the new Table object.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
    private void handleCreate(Request req, ConnectionToClient client) throws IOException, SQLException {
        Table newTable = (Table) req.getPayload();
        if (newTable == null) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: Invalid payload. Expected Table object.", null));
            return;
        }

        if (tableDAO.getTable(newTable.getTableNumber()) != null) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: Table number already exists.", null));
            return;
        }

        if (tableDAO.addTable(newTable)) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
                    "Success: Table added.", newTable));
            sendTablesToAllClients();
        } else {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: Failed to add table.", null));
        }
    }

    /**
     * Updates an existing table in the database.
     *
     * @param req    The request containing the updated Table object.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
    private void handleUpdate(Request req, ConnectionToClient client) throws IOException, SQLException {
        Table updateTable = (Table) req.getPayload();
        if (updateTable == null) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.UPDATE, Response.ResponseStatus.ERROR,
                    "Error: Invalid payload. Expected Table object.", null));
            return;
        }

        if (tableDAO.updateTable(updateTable)) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
                    "Success: Table updated.", updateTable));
            sendTablesToAllClients();
        } else {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: Failed to update table.", null));
        }
    }

    /**
     * Deletes a table from the database based on the ID provided in the request.
     *
     * @param req    The request containing the ID of the table to delete.
     * @param client The client connection.
     * @throws IOException  If an I/O error occurs.
     * @throws SQLException If a database error occurs.
     */
    private void handleDelete(Request req, ConnectionToClient client) throws IOException, SQLException {
        if (req.getId() == null) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: ID required for DELETE.", null));
            return;
        }

        if (tableDAO.deleteTable(req.getId())) {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
                    "Success: Table deleted.", req.getId()));
            sendTablesToAllClients();
        } else {
            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
                    "Error: Failed to delete table.", null));
        }
    }

    /**
     * Helper method to broadcast the current list of tables to all connected clients.
     * Used to keep clients in sync after changes.
     *
     * @throws SQLException If a database error occurs.
     * @throws IOException  If an I/O error occurs.
     */
    private void sendTablesToAllClients() throws SQLException, IOException {
        List<Table> tables = tableDAO.getAllTables();
        Request updateMsg = new Request(ResourceType.TABLE, ActionType.GET_ALL, null, tables);
        Router.sendToAllClients(updateMsg);
    }
}