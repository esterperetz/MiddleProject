package server.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import server.controller.ServerController;

/**
 * Controller class for the Server GUI view.
 * Handles the display of connected clients and server logs.
 */
public class ServerViewController {

    @FXML
    private TableView<ClientRow> tblClients;

    @FXML
    private TableColumn<ClientRow, String> colIp;

    @FXML
    private TableColumn<ClientRow, String> colHost;

    @FXML
    private TextArea txtLog;

    // Data model for the table
    private ObservableList<ClientRow> clients = FXCollections.observableArrayList();
    
    // Reference to the main logic controller
    private ServerController sc;

    /**
     * Initializes the controller class.
     * Sets up the table columns and the window close event listener.
     */
    @FXML
    private void initialize() {
        // Initialize table columns mapping
        colIp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIp()));
        colHost.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHost()));
        
        // Bind the list to the table
        tblClients.setItems(clients);

        // Handle window closing event on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) tblClients.getScene().getWindow();

                stage.setOnCloseRequest(event -> {
                    System.out.println("User has closed the window (X button).");
                    if (sc != null) {
                        sc.serverClosed();
                    }
                    System.exit(0);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Adds a new client to the connected clients table.
     * This method is thread-safe and updates the UI on the JavaFX Application Thread.
     *
     * @param ip   The IP address of the client.
     * @param host The host name of the client.
     */
    public void addClient(String ip, String host) {
        try {
            Platform.runLater(() -> clients.add(new ClientRow(ip, host)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a client from the table based on their IP address.
     * Removes the last occurrence found in the list.
     * This method is thread-safe.
     *
     * @param ip The IP address of the client to remove.
     */
    public void removeClient(String ip) {
        Platform.runLater(() -> {
            try {
                // Iterate backwards to safely remove from list while iterating
                for (int i = clients.size() - 1; i >= 0; i--) {
                    if (clients.get(i).getIp().equals(ip)) {
                        clients.remove(i);
                        break; // Stop after removing the first match found from the end
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Appends a message to the server log text area.
     * This method is thread-safe.
     *
     * @param msg The message to log.
     */
    public void log(String msg) {
        Platform.runLater(() -> {
            try {
                txtLog.appendText(msg + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sets the main server controller instance.
     * Used for callbacks when the server window is closed.
     *
     * @param sc The ServerController instance.
     */
    public void setServerController(ServerController sc) {
        this.sc = sc;
    }

    /**
     * Inner class representing a row in the clients table.
     * Holds the data for a single connected client.
     */
    public static class ClientRow {
        private String ip;
        private String host;

        public ClientRow(String ip, String host) {
            this.ip = ip;
            this.host = host;
        }

        public String getIp() {
            return ip;
        }

        public String getHost() {
            return host;
        }
    }
}