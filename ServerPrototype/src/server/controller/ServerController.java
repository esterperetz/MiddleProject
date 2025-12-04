package server.controller;

import DBConnection.DBConnection; // ייבוא לגישה ל-DBConnection
import Entities.Request;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.gui.ServerViewController;

public class ServerController extends AbstractServer {

    private final ServerViewController view;
    private final Router router;

    public ServerController(int port, ServerViewController view) {
        super(port);
        this.view = view;
        this.router = new Router();
    }

    /* ===== OCSF callbacks ===== */

    // ... (clientConnected, clientDisconnected, clientException, listeningException נשארים כפי שהם) ...

    @Override
    protected void serverClosed() {
        // קורא לשיטה החדשה שהוספנו כדי לסגור את החיבור הקבוע
        DBConnection.getInstance().closeConnection();
        view.log("Server closed and single persistent DB connection closed.");
    }

    /* ===== Messages from client ===== */
    
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        try {
            if (msg instanceof String && "quit".equals(msg)) {
                clientDisconnected(client);
                sendToAllClients("Disconnecting the client from the server.");
                return;
            }

            if (!(msg instanceof Request)) {
                System.err.println("Unsupported message type: " + (msg != null ? msg.getClass() : "null"));
                return;
            }

            Request request = (Request) msg;
            router.route(request, client);

        } catch (Exception e) {
            e.printStackTrace();
            view.log("Error while handling message: " + e.getMessage());
        }
    }
}