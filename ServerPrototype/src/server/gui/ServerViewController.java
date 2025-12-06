package server.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import server.controller.ServerController;  

public class ServerViewController {

    @FXML private TableView<ClientRow> tblClients;
    @FXML private TableColumn<ClientRow, String> colIp;
    @FXML private TableColumn<ClientRow, String> colHost;
    @FXML private TextArea txtLog;

    private ObservableList<ClientRow> clients = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colIp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIp()));
        colHost.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHost()));
        tblClients.setItems(clients);
        
        Platform.runLater(() -> {
            Stage stage = (Stage) tblClients.getScene().getWindow();
            
            stage.setOnCloseRequest(event -> {
                System.out.println("User has been closed the window (X button).");

                System.exit(0); 
            });
        });
    }

    /**
     * @param ip
     * @param host
     * add row to the table
     */
    public void addClient(String ip, String host) {
        Platform.runLater(() -> clients.add(new ClientRow(ip, host)));
    }

    /**
     * @param ip
     * remove last row(the last client was connected) from the table by IP
     */
    public void removeClient(String ip) {
    	Platform.runLater(() -> {
            for (int i = clients.size() - 1; i >= 0; i--) {
                if (clients.get(i).getIp().equals(ip)) {
                    clients.remove(i); // מוחק רק את ההתאמה הראשונה מהסוף
                    break;             // יוצא מהלולאה – לא מוחק אחרים עם אותו IP
                }
            }
        });
    }

    /**
     * @param msg
     * add log message for each change 
     */
    public void log(String msg) {
        Platform.runLater(() -> txtLog.appendText(msg + "\n"));
    }

    
    /**
     * client row, hold the ip and host for a client
     */
    public static class ClientRow {
        private String ip;
        private String host;
        public ClientRow(String ip, String host){
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
