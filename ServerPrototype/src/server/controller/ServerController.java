package server.controller;

import DAO.OrderDAO;
import DBConnection.DBConnection;
import Entities.Request;
import Entities.ResourceType;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.gui.ServerViewController;

public class ServerController extends AbstractServer {

    private final DBConnection model;
    private final ServerViewController view;
    private final OrderDAO orderDao;
    private final OrderController orderController;
    private final Router router; 
    public ServerController(int port, DBConnection model, ServerViewController view) {
        super(port);
        this.model = model;
        this.view = view;
        this.orderDao = new OrderDAO(model);
        this.orderController = new OrderController(orderDao);
        this.router = new Router(orderController);
    }

    /* ===== callbacks של OCSF ===== */

    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip   = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();
        view.addClient(ip, host);
        view.log("Client connected: " + ip + " (" + host + ")");
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        view.removeClient(ip);
        view.log("Client disconnected: " + ip);
    }

    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        view.log("Client exception from " + client + ": " + exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    protected void listeningException(Throwable exception) {
        view.log("Listening exception: " + exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    protected void serverClosed() {
        view.log("Server closed.");
    }

    /* ===== קבלת הודעות מהלקוח ===== */

    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        try {
            // support old "quit" string if you still need it
            if (msg instanceof String && "quit".equals(msg)) {
                clientDisconnected(client);
                sendToAllClients("Disconnecting the client from the server.");
                return;
            }

            if (!(msg instanceof Request)) {
                System.err.println("Unsupported message type: " +
                                   (msg != null ? msg.getClass() : "null"));
                return;
            }

            Request request = (Request) msg;
            router.route(request, client);   // <-- central routing here

        } catch (Exception e) {
            e.printStackTrace();
            view.log("Error while handling message: " + e.getMessage());
        }
    }
}
