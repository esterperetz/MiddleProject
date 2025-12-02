package server.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;

public class ServerViewController {

    @FXML
    private TableView<ClientRow> tblClients;

    @FXML
    private TableColumn<ClientRow, String> colIp;

    @FXML
    private TableColumn<ClientRow, String> colHost;

    @FXML
    private TextArea txtLog;

    private ObservableList<ClientRow> clients = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colIp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIp()));
        colHost.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHost()));
        tblClients.setItems(clients);
        
     // יש להמתין מעט כדי שה-Stage ייווצר על ידי JavaFX
	    Platform.runLater(() -> {
	        // 1. קבלת Stage (החלון הראשי)
	        Stage stage = (Stage) tblClients.getScene().getWindow();
	        
	        // 2. הוספת מאזין לאירוע סגירת חלון (ה-X)
	        stage.setOnCloseRequest(event -> {
	            // זהו קוד שרץ כאשר המשתמש לוחץ על 'X'
	            
	            System.out.println("User has been closed the window (X button).");
	            
//	            // מנע את הסגירה המיידית של JavaFX
//	            event.consume(); 
	            System.exit(0);
	        	
	        });
	    });
    }

    public void addClient(String ip, String host) {
        Platform.runLater(() -> clients.add(new ClientRow(ip, host)));
    }

    public void removeClient(String ip) {
        Platform.runLater(() -> clients.removeIf(c -> c.getIp().equals(ip)));
    }

    public void log(String msg) {
        Platform.runLater(() -> txtLog.appendText(msg + "\n"));
    }

    public static class ClientRow {
        private String ip;
        private String host;
        public ClientRow(String ip, String host) { this.ip = ip; this.host = host; }
        public String getIp() { return ip; }
        public String getHost() { return host; }
    }
    
    
}
