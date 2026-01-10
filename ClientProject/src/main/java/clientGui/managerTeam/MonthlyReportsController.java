package clientGui.managerTeam;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import client.ChatClient;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import entities.ActionType;
import entities.Alarm;
import entities.Employee;
import entities.MyFile;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;

public class MonthlyReportsController extends MainNavigator implements Initializable, MessageListener<Object> {

	@FXML
	private ComboBox<String> cmbMonth;

	@FXML
	private ComboBox<Integer> cmbYear;

	@FXML
	private Button btnDownload;

	@FXML
	private Label lblStatus;

	private Employee emp;
	private Employee.Role role;

	// מופע סטטי כדי שנוכל לקרוא לו מ-ChatClient כשהקובץ חוזר
	public static MonthlyReportsController instance;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		instance = this;
		initComboBoxes();
	}

	public void initData(Employee emp, ClientUi clientUi, Employee.Role role) {
		this.clientUi = clientUi;
		this.emp = emp;
		this.role = role;
	}

	/**
	 * ממלא את ה-ComboBox בחודשים ושנים רלוונטיות
	 */
	private void initComboBoxes() {
		// מילוי חודשים 01-12
		for (int i = 1; i <= 12; i++) {
			cmbMonth.getItems().add(String.format("%02d", i));
		}

		// מילוי שנים (למשל מ-2024 עד השנה הנוכחית)
		int currentYear = LocalDate.now().getYear();
		for (int i = 2024; i <= currentYear; i++) {
			cmbYear.getItems().add(i);
		}

		// בחירת ברירת מחדל (חודש שעבר)
		LocalDate prevMonth = LocalDate.now().minusMonths(1);
		cmbMonth.setValue(String.format("%02d", prevMonth.getMonthValue()));
		cmbYear.setValue(prevMonth.getYear());
	}

	@FXML
	void downloadReportBtn(ActionEvent event) {
		String month = cmbMonth.getValue();
		Integer year = cmbYear.getValue();

		if (month == null || year == null) {
			lblStatus.setText("Please select both month and year.");
			lblStatus.setStyle("-fx-text-fill: red;");
			return;
		}

		lblStatus.setText("Downloading report...");
		lblStatus.setStyle("-fx-text-fill: blue;");

		// פורמט המחרוזת שהשרת מצפה לו: "MM/yyyy"
		String datePayload = month + "/" + year;

		// שליחת בקשה לשרת
		Request req = new Request(ResourceType.REPORT_MONTHLY, ActionType.DOWNLOAD_REPORT, null, datePayload);
		this.clientUi.sendRequest(req);
	}

	@Override
	public void onMessageReceive(Object msg) {
		if (msg instanceof Response) {
			Response response = (Response) msg;

			// מוודאים שזו התשובה לבקשה שלנו
			if (response.getAction() == ActionType.DOWNLOAD_REPORT
					&& response.getStatus() == Response.ResponseStatus.SUCCESS) {

				Platform.runLater(() -> {
					try {

						Object payload = response.getData();

						// --- בדיקה 1: האם חזר קובץ? (התרחיש הטוב) ---
						if (payload instanceof MyFile) {
							saveAndOpenFile((MyFile) payload);
							return; // סיימנו
						}

						// --- בדיקה 2: האם חזרה הודעת טקסט? (שגיאה או הודעה מהשרת) ---
						if (payload instanceof String) {
							String message = (String) payload;
							lblStatus.setText(message);

							// צבע אדום אם הסטטוס הוא ERROR, אחרת כחול/ירוק
							if (response.getStatus() == Response.ResponseStatus.ERROR) {
								lblStatus.setStyle("-fx-text-fill: red;");
							} else {
								lblStatus.setStyle("-fx-text-fill: blue;");
							}
							return;
						}

						// --- בדיקה 3: טיפול במקרה של ERROR ללא payload ---
						if (response.getStatus() == Response.ResponseStatus.ERROR) {
							String errorMsg = response.getMessage_from_server(); // במידה וההודעה בשדה message ולא
																					// ב-payload
							lblStatus.setText(errorMsg != null ? errorMsg : "Unknown error occurred");
							lblStatus.setStyle("-fx-text-fill: red;");
						}

					} catch (Exception e) {
						e.printStackTrace();
						lblStatus.setText("Client error: " + e.getMessage());
						lblStatus.setStyle("-fx-text-fill: red;");
					}
				});
			}
		}
	}

	public void receiveReportFile(Object msg) {
		Platform.runLater(() -> {
			if (msg instanceof String) {
				// הודעת שגיאה מהשרת (למשל "Report not found")
				lblStatus.setText((String) msg);
				lblStatus.setStyle("-fx-text-fill: red;");
				return;
			}

			if (msg instanceof MyFile) {
				MyFile myFile = (MyFile) msg;
				saveAndOpenFile(myFile);
			}
		});
	}

	private void saveAndOpenFile(MyFile myFile) {
		try {
			// שמירה בתיקיית ההורדות של המשתמש
			String userHome = System.getProperty("user.home");
			String filePath = userHome + "/Downloads/" + myFile.getFileName();
			File file = new File(filePath);

			// כתיבת הביטים לקובץ
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(myFile.getMybytearray(), 0, myFile.getSize());
			bos.flush();
			bos.close();
			fos.close();

			lblStatus.setText("Report saved to Downloads!");
			lblStatus.setStyle("-fx-text-fill: green;");

			// פתיחה אוטומטית בדפדפן
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(file);
			}

		} catch (IOException e) {
			e.printStackTrace();
			lblStatus.setText("Error saving file: " + e.getMessage());
			lblStatus.setStyle("-fx-text-fill: red;");
		}
	}

	@FXML
	void backBtn(ActionEvent event) {
		// חזרה למסך המנהל
		ManagerOptionsController managerOptions = super.loadScreen("managerTeam/ManagerOptions", event, clientUi);
		if (managerOptions != null) {
			managerOptions.initData(emp, clientUi, role);
		}
	}
}