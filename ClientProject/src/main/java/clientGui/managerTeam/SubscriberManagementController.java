package clientGui.managerTeam;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberHistoryController;
import clientLogic.UserLogic;
import entities.ActionType;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SubscriberManagementController extends MainNavigator implements Initializable, MessageListener<Object> {

	@FXML
	private VBox subscribersContainer; // המיכל של הכפתורים

	private UserLogic userLogic;
	private Employee connectedEmployee;
	private Employee.Role role;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void initData(Employee emp, ClientUi clientUi, Employee.Role role) {
		this.clientUi = clientUi;
		this.connectedEmployee = emp;
		this.role = role;
		this.userLogic = new UserLogic(clientUi);

		userLogic.getAllSubscribers();
	}

	@Override
	public void onMessageReceive(Object msg) {
		if (!(msg instanceof Response))
			return;
		Response res = (Response) msg;

		Platform.runLater(() -> {
			if (res.getResource() == entities.ResourceType.CUSTOMER && res.getAction() == ActionType.GET_ALL) {

				if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
					if (res.getData() instanceof List) {
						List<Customer> data = (List<Customer>) res.getData();
						updateSubscriberList(data);
					}
				} else {
					Label errorLbl = new Label("Failed to load: " + res.getMessage_from_server());
					errorLbl.setStyle("-fx-text-fill: red;");
					subscribersContainer.getChildren().add(errorLbl);
				}
			}
		});
	}

	private void updateSubscriberList(List<Customer> subscribers) {
		subscribersContainer.getChildren().clear(); // ניקוי

		if (subscribers.isEmpty()) {
			Label emptyLbl = new Label("No subscribers found.");
			emptyLbl.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
			subscribersContainer.getChildren().add(emptyLbl);
			return;
		}

		for (Customer c : subscribers) {
			Button subBtn = createSubscriberButton(c);
			subscribersContainer.getChildren().add(subBtn);
		}
	}

	private Button createSubscriberButton(Customer c) {
		String btnText = String.format("Name: %-20s | ID: %-10s | Phone: %s", c.getName(), c.getCustomerId(),
				c.getPhoneNumber());

		Button btn = new Button(btnText);
		btn.setPrefWidth(800);
		btn.setPrefHeight(50);

		btn.setStyle("-fx-background-color: #383838; " + "-fx-text-fill: white; " + "-fx-border-color: #D4AF37; "
				+ "-fx-border-radius: 5; " + "-fx-background-radius: 5; " + "-fx-alignment: CENTER_LEFT; "
				+ "-fx-font-size: 16px; " + "-fx-padding: 0 0 0 20;");

		btn.setOnMouseEntered(e -> btn
				.setStyle("-fx-background-color: #555; " + "-fx-text-fill: white; " + "-fx-border-color: #F4C430; "
						+ "-fx-border-radius: 5; " + "-fx-background-radius: 5; " + "-fx-alignment: CENTER_LEFT; "
						+ "-fx-font-size: 16px; " + "-fx-padding: 0 0 0 20; "
						+ "-fx-effect: dropshadow(three-pass-box, rgba(212, 175, 55, 0.4), 10, 0, 0, 0);"));

		btn.setOnMouseExited(e -> btn
				.setStyle("-fx-background-color: #383838; " + "-fx-text-fill: white; " + "-fx-border-color: #D4AF37; "
						+ "-fx-border-radius: 5; " + "-fx-background-radius: 5; " + "-fx-alignment: CENTER_LEFT; "
						+ "-fx-font-size: 16px; " + "-fx-padding: 0 0 0 20;"));

		btn.setOnAction(event -> {
			openSubscriberHistory(c, event);
		});

		return btn;
	}

	private void openSubscriberHistory(Customer selectedCustomer, ActionEvent event) {
		SubscriberHistoryController controller = super.loadScreen("user/SubscriberHistory", event, clientUi);
		if (controller != null) {
			controller.initData(selectedCustomer.getSubscriberCode(), CustomerType.SUBSCRIBER, connectedEmployee,
					selectedCustomer);
		}
	}

	@FXML
	void goBackBtn(ActionEvent event) {
		ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
		if (controller != null) {
			controller.initData(connectedEmployee, clientUi, role);
		}
	}
}