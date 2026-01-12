package clientGui.managerTeam;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.reservation.OrderUi_controller;
import clientGui.reservation.WaitingListController;
import clientGui.user.RegisterSubscriberController;
import clientLogic.EmployeeLogic;
import entities.Employee;
import entities.OpeningHours;
import entities.Response;
import entities.Table;
import entities.WaitingList;

import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import client.MessageListener;

public class ManagerOptionsController extends MainNavigator implements Initializable, MessageListener<Object> {

    // --- Internal Fields ---\
	private TranslateTransition currentTransition;
    private Employee.Role isManager;
    private boolean isManagerFlag;
    private ObservableList<String> specialDatesModel;
    private Employee emp;
    @FXML private Pane tickerPane;
    @FXML private Label lblTicker;
    @FXML private javafx.scene.text.TextFlow tfTicker;

    // --- FXML UI Components (Left Side - Navigation) ---
    @FXML private Button btnViewReports;
    @FXML private Button btnMonthlyReports;
    @FXML private Label lblDashboardTitle;
    @FXML private Label lblDashboardSubtitle;
    @FXML private Button btnSignUp;
    

    // --- Schedule Management UI (Right Side - UPDATED) ---
    
    @FXML private DatePicker dpManageDate;     
    @FXML private TextField txtManageOpen;      
    @FXML private TextField txtManageClose;     
    @FXML private CheckBox cbIsSpecial;         
    @FXML private ListView<String> listSpecialDates; 
    @FXML private Label lblHoursStatus;     
    @FXML private CheckBox cbIsClosed;
	private EmployeeLogic employeeLogic;
	private String specialDate;
	private String listEntry;
	

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        specialDatesModel = FXCollections.observableArrayList();
        listSpecialDates.setItems(specialDatesModel);
     // יצירת מסכה (Clip) כדי שהטקסט ייראה רק בתוך הגבולות של ה-Pane
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        // קושרים את גודל המסכה לגודל ה-Pane
        clip.widthProperty().bind(tickerPane.widthProperty());
        clip.heightProperty().bind(tickerPane.heightProperty());
        tickerPane.setClip(clip);
      
    }
    
    private void initTicker() {
    	
       employeeLogic.getAllOpeningHours();
    }

    private void startAnimation(double paneWidth) {
        // 1. עצירת אנימציה קודמת
        if (currentTransition != null) {
            currentTransition.stop();
        }

        // 2. איפוס מיקום
        tfTicker.setTranslateX(paneWidth);

        // 3. כפיית חישוב גודל מחדש
        tfTicker.applyCss();
        tfTicker.layout();

        // --- התיקון הגדול ---
        // במקום getWidth() שמושפע מגודל המסך, אנחנו משתמשים ב-prefWidth(-1).
        // הפרמטר -1 אומר: "תחזיר לי את הרוחב האידיאלי שלך ללא הגבלות".
        double contentWidth = tfTicker.prefWidth(-1); 

        // הגנה: אם החישוב נכשל והחזיר 0, ננסה לחשב לפי סכום הילדים (Text nodes)
        if (contentWidth <= 0) {
            contentWidth = tfTicker.getChildren().stream()
                    .mapToDouble(node -> node.getLayoutBounds().getWidth())
                    .sum();
            // הוספת מרווח ביטחון קטן
            contentWidth += 20; 
        }

        // 4. הגדרת האנימציה
        currentTransition = new TranslateTransition();
        currentTransition.setNode(tfTicker);

        // חישוב המרחק הכולל: כניסה מצד ימין -> יציאה מלאה מצד שמאל
        double totalDistance = paneWidth + contentWidth;

        // הגדרת מהירות אחידה (פיקסלים לשנייה)
        // 100 פיקסלים לשנייה זה קצב קריאה נוח
        double speedPixelsPerSecond = 80.0; 
        double durationSeconds = totalDistance / speedPixelsPerSecond;

        currentTransition.setDuration(Duration.seconds(durationSeconds));

        // מאיפה: קצה ימני של המסך
        currentTransition.setFromX(paneWidth);
        
        // לאיפה: שמאלה עד שכל הטקסט נעלם (מינוס הרוחב שלו)
        currentTransition.setToX(-contentWidth);

        currentTransition.setCycleCount(Animation.INDEFINITE);
        currentTransition.setInterpolator(Interpolator.LINEAR);
        currentTransition.play();
    }
    public void initData(Employee emp, ClientUi clientUi, Employee.Role isManager) {
        this.clientUi = clientUi;
        this.emp = emp;
       
        employeeLogic = new EmployeeLogic(this.clientUi);
        if (isManager == Employee.Role.MANAGER) {
            this.isManager = Employee.Role.MANAGER;
            this.isManagerFlag = true;
            btnViewReports.setVisible(true);
            btnViewReports.setManaged(true);
            btnSignUp.setVisible(true);
            btnSignUp.setManaged(true);
            btnMonthlyReports.setVisible(true);
            btnMonthlyReports.setManaged(true);
            lblDashboardTitle.setText("Hello Manager, " + emp.getUserName());
            lblDashboardSubtitle.setText("Manager Dashboard - Full Access");
        } else {
            this.isManagerFlag = false;
            this.isManager = Employee.Role.REPRESENTATIVE;
            btnViewReports.setVisible(false);
            btnViewReports.setManaged(false);
            btnSignUp.setVisible(false);
            btnSignUp.setManaged(false);
            btnMonthlyReports.setVisible(false);
            btnMonthlyReports.setManaged(false);
            lblDashboardTitle.setText("Hello, " + emp.getUserName());
            lblDashboardSubtitle.setText("Employee Dashboard");
        }

        if (this.clientUi == null) {
            System.err.println("Error: ClientUi is null in ManagerOptionsController!");
            return;
        }
        initTicker();
    }

    /**
     * UNIFIED Method: Update hours for a specific date (Standard OR Special).
     */
    @FXML
    void updateScheduleBtn(ActionEvent event) {
        LocalDate date = dpManageDate.getValue();
        String openTimeStr = txtManageOpen.getText();
        String closeTimeStr = txtManageClose.getText();
        boolean isSpecial = cbIsSpecial.isSelected();
        boolean isClosed = cbIsClosed.isSelected();

        if (date == null) {
            setStatus("Please select a date first.", true);
            return;
        }

        if (!isClosed && (openTimeStr == null || openTimeStr.trim().isEmpty() ||
                          closeTimeStr == null || closeTimeStr.trim().isEmpty())) {
            setStatus("Please enter both opening and closing times.", true);
            return;
        }

        try {
            Time sqlOpenTime = null;
            Time sqlCloseTime = null;

            if (!isClosed) {
            	
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
                LocalTime localOpen = LocalTime.parse(openTimeStr, formatter);
                LocalTime localClose = LocalTime.parse(closeTimeStr, formatter);
                sqlOpenTime = Time.valueOf(localOpen);
                sqlCloseTime = Time.valueOf(localClose);
            }

            String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String typeStr = isSpecial ? "(Special Event)" : "(Updated Hours)";
            String timeDisplay = isClosed ? "[CLOSED]" : sqlOpenTime + " - " + sqlCloseTime;
            
            this.listEntry = String.format("%s: %s %s", dateStr, timeDisplay, typeStr);

            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            OpeningHours oh;

            if (!isSpecial) {
            	
                oh = new OpeningHours(sqlDate, null, sqlOpenTime, sqlCloseTime, isClosed);
            } else {
                oh = new OpeningHours(sqlDate, sqlDate, sqlOpenTime, sqlCloseTime, isClosed);
            }

            employeeLogic.createOpeningHours(oh);

        } catch (DateTimeParseException e) {
            e.printStackTrace();
            setStatus("Invalid time format! Use HH:mm (e.g., 08:00 or 8:00)", true);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("An error occurred during update.", true);
        }
    }
        
     

    @FXML
    void removeSpecialDateBtn(ActionEvent event) {
        String selectedItem = listSpecialDates.getSelectionModel().getSelectedItem();
        
        if (selectedItem != null) {
        
        	if(selectedItem.contains("(Special Event)")) {
        		String[] parts = selectedItem.split(":");
                String dateString = parts[0].trim(); 

                // המרה לתאריך SQL
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                java.sql.Date dateToDelete = java.sql.Date.valueOf(localDate);

                this.specialDate = selectedItem;
                employeeLogic.cancelOpeningHours(dateToDelete);
        	 	
        	}
        	else {
        		setStatus("could remove please select special date!", true);
        	}
  
        } else {
            setStatus("Select an item to remove", true);
        }
    }

    private void setStatus(String msg, boolean isError) {
        lblHoursStatus.setText(msg);
        lblHoursStatus.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #2ecc71;");
    }

    // --- Navigation Methods (No Changes) ---

    @FXML
    void goToWaitingListBtn(ActionEvent event) {
        WaitingListController waiting_list = super.loadScreen("reservation/WaitingList", event, clientUi);
        if (waiting_list != null) {
            waiting_list.initData(emp, this.clientUi, this.isManager);
        } else {
            System.err.println("Failed to load WaitingList.");
        }
    }

    @FXML
    void goToMonthlyReportsBtn(ActionEvent event) {
         MonthlyReportsController m = super.loadScreen("managerTeam/MonthlyReports", event, clientUi);
         if (m != null) {
             m.initData(this.emp, this.clientUi, this.isManager); 
         } else {
             System.out.println("error: MonthlyReportsController is null");
         }
    }

    @FXML
    void goToOrderDetailsBtn(ActionEvent event) {
        OrderUi_controller controller = super.loadScreen("reservation/orderUi", event, clientUi);
        if (controller != null) {
            controller.initData(emp, this.clientUi, this.isManager);
        } else {
            System.err.println("Failed to load OrderUi.");
        }
    }

    @FXML
    public void goToSignUpEmployee(ActionEvent event) {
        try {
            RegisterEmployeeController registerEmployee = super.loadScreen("managerTeam/RegisterEmployee", event, clientUi);
            registerEmployee.initData(emp, this.clientUi, this.isManager);
        } catch (NullPointerException e) {
            System.out.println("Error: the object RegisterEmployeeController is null");
        }
    }

    @FXML
    void goToRegisterSubscriberBtn(ActionEvent event) {
        RegisterSubscriberController r = super.loadScreen("user/RegisterSubscriber", event, clientUi);
        try {
            r.initData(emp, this.clientUi, this.isManager);
        } catch (NullPointerException e) {
            System.out.println("Error: RegisterSubscriberController is null");
        }
    }

    @FXML
    void goToReportsBtn(ActionEvent event) {
        ReportsController r = super.loadScreen("managerTeam/ReportsScreen", event, clientUi);
        if (r != null) {
            r.initData(emp, this.clientUi, this.isManager);
        } else {
            System.out.println("Error: ReportsController is null!!");
        }
    }

    @FXML
    void goToTableManagementBtn(ActionEvent event) {
        TableManagementController controller = super.loadScreen("managerTeam/TableManagement", event, clientUi);
        if (controller != null) {
            controller.initData(emp, clientUi);
        } else {
            System.err.println("Failed to load TableManagement.");
        }
    }

    @FXML
    void goBackBtn(ActionEvent event) {
        System.out.println("Going back / Signing out...");
        super.loadScreen("navigation/SelectionScreen", event, clientUi);
    }
    
    private void updateTickerFromList(List<OpeningHours> listOp) {
        if (listOp == null || listOp.isEmpty()) {
            Platform.runLater(() -> {
                tfTicker.getChildren().clear();
                Text t = new Text("No opening hours available.");
                t.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");
                tfTicker.getChildren().add(t);
            });
            return;
        }

        // מיון לפי ימים
        listOp.sort((o1, o2) -> Integer.compare(o1.getDayOfWeek(), o2.getDayOfWeek()));

        // אנחנו בונים את הרשימה מחדש
        Platform.runLater(() -> {
            tfTicker.getChildren().clear(); // ניקוי הטקסט הקודם

            for (OpeningHours oh : listOp) {
                // 1. בדיקה אם זה חג/תאריך מיוחד
                boolean isHoliday = (oh.getSpecialDate() != null);

                // 2. בניית המחרוזת ליום הספציפי
                StringBuilder sb = new StringBuilder();
                String dayName = getDayShortName(oh.getDayOfWeek());
                
                sb.append(dayName).append(": ");

                if (oh.isClosed()) {
                    sb.append("CLOSED");
                } else {
                    String start = oh.getOpenTime().toString();
                    String end = oh.getCloseTime().toString();
                    if (start.length() >= 5) start = start.substring(0, 5);
                    if (end.length() >= 5) end = end.substring(0, 5);
                    sb.append(start).append("-").append(end);
                }

                // הוספת כיתוב חג אם צריך
                if (isHoliday) {
                    sb.append(" (HOLIDAY)");
                }
                
                sb.append("   •   "); // מרווח

                // 3. יצירת אובייקט Text וצביעה
                Text textNode = new Text(sb.toString());
                
                if (isHoliday) {
                    // עיצוב לחג: אדום ומודגש
                    textNode.setStyle("-fx-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
                } else {
                    // עיצוב רגיל: כחול כהה
                    textNode.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");
                }

                // הוספה ל-TextFlow
                tfTicker.getChildren().add(textNode);
            }

            // התחלת אנימציה
            if (tickerPane.getWidth() > 0) {
                startAnimation(tickerPane.getWidth());
            } else {
                tickerPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() > 0) {
                        startAnimation(newVal.doubleValue());
                    }
                });
            }
        });
    }

    // מתודת עזר קטנה להמרת מספר יום לשם
    private String getDayShortName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Sun";
            case 2: return "Mon";
            case 3: return "Tue";
            case 4: return "Wed";
            case 5: return "Thu";
            case 6: return "Fri";
            case 7: return "Sat";
            default: return "Day" + dayOfWeek;
        }
    }

    @Override
    public void onMessageReceive(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Response) {
                Response res = (Response) msg;
                if (res.getResource() == entities.ResourceType.BUSINESS_HOUR) {
                    switch (res.getAction()) {
                    	case GET_ALL:
                    		if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                    			List<OpeningHours> listOp = (ArrayList<OpeningHours>)res.getData();
                    			updateTickerFromList(listOp);
                    			
                    		}
                        case CREATE:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                            	handleCreate(res);
                            }
                            else
                              	setStatus("Could not remove from DB", true);
                            break;
                      
                        case UPDATE:
                        	   if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                        		   
                                   setStatus(" date has been updated! ", false);
                               }
                               else {
                                   setStatus("Could not remove from DB", true);
                               }
                               break;
                        case DELETE:
                            if (res.getStatus() == Response.ResponseStatus.SUCCESS) {
                            	if(this.specialDate != null) {
                            		specialDatesModel.remove(this.specialDate);
                                    setStatus("Special date removed. Reverted to standard hours.", false);
                                    
                            	}else
                            		setStatus("Special date could not be removed.", true);
                            }
                            else {
                                setStatus("Could not remove from DB", true);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

	private void handleCreate(Response res) {
//		  OpeningHours data = (OpeningHours) res.getData();
          if(listEntry != null) {
          	specialDatesModel.add(0, listEntry); 
              setStatus("Schedule updated successfully!", false);
              txtManageOpen.clear();
              txtManageClose.clear();
          }
          else
          	 setStatus("Schedule update failed!", true);
        
    }
     
		
}

