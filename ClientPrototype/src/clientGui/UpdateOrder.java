package clientGui;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import Entities.Order;
import clientLogic.OrderLogic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateOrder implements Initializable {

    @FXML private TextField txtId;
    @FXML private TextField txtName; // Guests
    @FXML private TextField txtName1; // Date

    private Order currentOrder;
    private OrderLogic orderLogic;
    private ClientUi clientUi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // אתחול רגיל
    }

    // פונקציה לקבלת המידע מהחלון הקודם
    public void initData(Order o, ClientUi clientUi) {
        this.currentOrder = o;
        this.clientUi = clientUi;
        this.orderLogic = new OrderLogic(clientUi);

        // הצגת הנתונים בשדות
        txtId.setText(String.valueOf(o.getOrder_number()));
        txtId.setEditable(false); // בדרך כלל לא משנים ID
        txtName.setText(String.valueOf(o.getNumber_of_guests()));
        txtName1.setText(o.getOrder_date().toString()); // או פורמט תאריך מתאים
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        try {
            int guests = Integer.parseInt(txtName.getText().trim());
            // המרת תאריך - לצורך הדוגמה נשתמש בתאריך הנוכחי או נפרסר (תלוי בפורמט)
            // Date date = new SimpleDateFormat("yyyy-MM-dd").parse(txtName1.getText());
            Date date = new Date(); // זמני

            // עדכון האובייקט
            currentOrder.setNumber_of_guests(guests);
            currentOrder.setOrder_date(date);
            
            // שליחת בקשת עדכון לשרת
            orderLogic.updateOrder(currentOrder);

            // חזרה למסך הראשי
            goBackToMain(event);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Input Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void goBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/orderUi.fxml"));
            Parent root = loader.load();
            
            // אנחנו צריכים לוודא שהקונטרולר הראשי מקבל את ה-ClientUi הקיים
            // אבל מכיוון ש-OrderUi_controller יוצר חדש ב-initialize, זה עלול ליצור כפילות.
            // לפתרון מושלם: צריך להעביר את ה-ClientUi גם לקונטרולר הראשי בחזרה, 
            // או להשתמש ב-Singleton ל-ClientUi.
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}