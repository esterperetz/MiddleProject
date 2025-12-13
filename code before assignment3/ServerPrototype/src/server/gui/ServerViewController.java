package server.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import server.controller.ServerController;

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

			Platform.runLater(() -> {
				try {
				Stage stage = (Stage) tblClients.getScene().getWindow();

				stage.setOnCloseRequest(event -> {
					System.out.println("User has been closed the window (X button).");

					System.exit(0);
				});
				}catch (Exception e) {
					e.printStackTrace();//d
				}
			});
		
	}

	/**
	 * @param ip
	 * @param host add row to the table
	 */
	public void addClient(String ip, String host) {
		try {
			Platform.runLater(() -> clients.add(new ClientRow(ip, host)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param ip remove last row(the last client was connected) from the table by IP
	 */
	public void removeClient(String ip) {

		Platform.runLater(() -> {
			try {
				for (int i = clients.size() - 1; i >= 0; i--) {
					if (clients.get(i).getIp().equals(ip)) {
						clients.remove(i);
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * @param msg add log message for each change
	 */
	public void log(String msg) {

		Platform.runLater(() -> {
			try {
				txtLog.appendText(msg + "\n");
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	/**
	 * client row, hold the ip and host for a client
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
