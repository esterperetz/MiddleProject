package clientGui;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import Entities.Order;
import client.MessageListener;
import clientLogic.OrderLogic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class OrderUi_controller implements Initializable, MessageListener {

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> Order_numberColumn;
    @FXML private TableColumn<Order, Date> DateColumn;
    @FXML private TableColumn<Order, Integer> itemColumn; // Guests

    private ObservableList<Order> orderData = FXCollections.observableArrayList();
    private ClientUi clientUi;
    private OrderLogic orderLogic;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. הגדרת עמודות
        Order_numberColumn.setCellValueFactory(new PropertyValueFactory<>("order_number"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));

        // הגדרת עמודות כניתנות לעריכה (אופציונלי - לפי הקוד המקורי שלך)
        setupEditableColumns();

        // 2. חיבור הנתונים לטבלה
        orderTable.setItems(orderData);

        // 3. יצירת קשר עם השרת
        clientUi = new ClientUi();
        clientUi.addListener(this); // הרשמה לקבלת הודעות
        orderLogic = new OrderLogic(clientUi);

        // 4. שליחת בקשה ראשונית
        System.out.println("Initialization: Requesting all orders...");
        orderLogic.getAllOrders();

        // 5. טיפול בסגירת חלון
        Platform.runLater(() -> {
            Stage stage = (Stage) orderTable.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("Closing client...");
                clientUi.disconnectClient();
                System.exit(0);
            });
        });
    }

    /**
     * זוהי הפונקציה שנקראת כשהשרת מחזיר תשובה.
     * כעת msg הוא אובייקט אמיתי!
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            
            // מקרה 1: השרת החזיר רשימה של הזמנות (תשובה ל-GET_ALL)
            if (msg instanceof List) {
                List<?> list = (List<?>) msg;
                // בדיקה גסה אם הרשימה מכילה Orders או שהיא ריקה
                if (list.isEmpty() || list.get(0) instanceof Order) {
                    List<Order> incomingOrders = (List<Order>) msg;
                    orderData.clear();
                    orderData.addAll(incomingOrders);
                    System.out.println("Updated table with " + incomingOrders.size() + " orders.");
                } else {
                    System.out.println("Received a list, but it's not orders.");
                }
            }
            
            // מקרה 2: השרת החזיר הזמנה בודדת (תשובה ל-GET_BY_ID או CREATE)
            else if (msg instanceof Order) {
                Order o = (Order) msg;
                System.out.println("Received single order: " + o);
                // אפשר להוסיף לטבלה או לעדכן
                // orderData.add(o); 
            }
            
            // מקרה 3: השרת החזיר מחרוזת (הודעת שגיאה או ניתוק)
            else if (msg instanceof String) {
                String text = (String) msg;
                System.out.println("Message from server: " + text);
                if (text.contains("Disconnecting")) {
                    clientUi.disconnectClient();
                }
            }
            
            // מקרה 4: השרת החזיר בוליאני (הצלחה/כישלון של CREATE/UPDATE)
            else if (msg instanceof Boolean) {
                boolean success = (Boolean) msg;
                if (success) {
                    showAlert("Success", "Operation completed successfully!", Alert.AlertType.INFORMATION);
                    // רענון הטבלה אחרי עדכון
                    orderLogic.getAllOrders();
                } else {
                    showAlert("Failure", "Operation failed.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleUpdateOrder(ActionEvent event) {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("No Selection", "Please select an order to update.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // מעבר למסך העדכון
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/updateOrder.fxml"));
            Parent root = loader.load();
            
            // העברת המידע לקונטרולר של מסך העדכון
            UpdateOrder controller = loader.getController();
            controller.initData(selectedOrder, clientUi); // שים לב לפונקציה הזו שאוסיף למטה

            Stage stage = new Stage();
            stage.setTitle("Update Order");
            stage.setScene(new Scene(root));
            stage.show();
            
            // אופציונלי: סגירת החלון הנוכחי
             ((Node) event.getSource()).getScene().getWindow().hide();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteOrder() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            // שליחת בקשת מחיקה לשרת
            // (הערה: השרת צריך לממש את זה, כרגע יש לנו מימוש ל-getAll, getById, create)
            // orderLogic.deleteOrder(selectedOrder.getOrder_number());
            
            // כרגע רק נמחק מהתצוגה
            orderData.remove(selectedOrder);
        } else {
            showAlert("No Selection", "Please select an order to delete.", Alert.AlertType.WARNING);
        }
    }

    private void setupEditableColumns() {
        // הגדרת עריכה בטבלה
        DateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
        itemColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        
        itemColumn.setOnEditCommit(event -> {
            Order o = event.getRowValue();
            o.setNumber_of_guests(event.getNewValue());
            // שליחת עדכון לשרת (אם רוצים עדכון בלייב)
            // orderLogic.updateOrder(o);
        });
    }

    public void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // בוטל ה-displayMessage הישן כי הכל ב-onMessageReceive
}