package entities;

import javafx.scene.control.Alert;

public class Alarm {
	public static void showAlertWithException(String header_text,String context_text,Alert.AlertType type,Exception e)
	{
		Alert alert = new Alert(type);
        alert.setHeaderText(header_text);
        alert.setContentText(context_text + e.getMessage());
        alert.showAndWait();
	}
	public static void showAlert(String header_text,String context_text,Alert.AlertType type)
	{
		Alert alert = new Alert(type);
        alert.setHeaderText(header_text);
        alert.setContentText(context_text);
        alert.showAndWait();
	}

}
