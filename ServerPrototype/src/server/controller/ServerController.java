package server.controller;

import DBConnection.DBConnection;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.gui.ServerViewController;

public class ServerController extends AbstractServer {

    private DBConnection model;
    private ServerViewController view;

    public ServerController(int port, DBConnection model, ServerViewController view) {
        super(port);
        this.model = model;
        this.view = view;
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
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
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    }


}
