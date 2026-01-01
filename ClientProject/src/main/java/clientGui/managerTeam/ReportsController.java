package clientGui.managerTeam;


import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import entities.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class ReportsController extends MainNavigator implements MessageListener<Object>{

    @FXML private BarChart<String, Number> barChartTimes;
    @FXML private LineChart<String, Number> lineChartOrders;
    @FXML private CategoryAxis xAxisTimes;
    @FXML private CategoryAxis xAxisDays;
    private Employee.Role isManager;
    @FXML
    public void initialize() {
        loadTimeChartData();
        loadOrderTrendsData();
    }

    /**
     * גרף 1: טעינת נתוני הגעה, עזיבה ואיחורים
     */
    private void loadTimeChartData() {
        // סדרה 1: הגעות (Arrivals)
        XYChart.Series<String, Number> seriesArrivals = new XYChart.Series<>();
        seriesArrivals.setName("Arrivals");
        seriesArrivals.getData().add(new XYChart.Data<>("18:00", 12));
        seriesArrivals.getData().add(new XYChart.Data<>("19:00", 45));
        seriesArrivals.getData().add(new XYChart.Data<>("20:00", 30));
        seriesArrivals.getData().add(new XYChart.Data<>("21:00", 10));

        // סדרה 2: עזיבות (Departures)
        XYChart.Series<String, Number> seriesDepartures = new XYChart.Series<>();
        seriesDepartures.setName("Departures");
        seriesDepartures.getData().add(new XYChart.Data<>("18:00", 2));
        seriesDepartures.getData().add(new XYChart.Data<>("19:00", 10));
        seriesDepartures.getData().add(new XYChart.Data<>("20:00", 40));
        seriesDepartures.getData().add(new XYChart.Data<>("21:00", 35));

        // סדרה 3: איחורים (Late)
        XYChart.Series<String, Number> seriesLates = new XYChart.Series<>();
        seriesLates.setName("Late/No-Show");
        seriesLates.getData().add(new XYChart.Data<>("18:00", 1));
        seriesLates.getData().add(new XYChart.Data<>("19:00", 5));
        seriesLates.getData().add(new XYChart.Data<>("20:00", 2));
        seriesLates.getData().add(new XYChart.Data<>("21:00", 0));

        barChartTimes.getData().addAll(seriesArrivals, seriesDepartures, seriesLates);
    }

    /**
     * גרף 2: כמות הזמנות מול רשימת המתנה
     */
    private void loadOrderTrendsData() {
        // סדרה 1: הזמנות בפועל
        XYChart.Series<String, Number> seriesOrders = new XYChart.Series<>();
        seriesOrders.setName("Total Orders");
        
        // נתונים לדוגמה לאורך החודש (ימים 1, 5, 10...)
        seriesOrders.getData().add(new XYChart.Data<>("1", 50));
        seriesOrders.getData().add(new XYChart.Data<>("5", 80));
        seriesOrders.getData().add(new XYChart.Data<>("10", 65));
        seriesOrders.getData().add(new XYChart.Data<>("15", 120));
        seriesOrders.getData().add(new XYChart.Data<>("20", 90));
        seriesOrders.getData().add(new XYChart.Data<>("25", 110));

        // סדרה 2: אנשים ברשימת המתנה
        XYChart.Series<String, Number> seriesWaiting = new XYChart.Series<>();
        seriesWaiting.setName("Waiting List Count");
        
        seriesWaiting.getData().add(new XYChart.Data<>("1", 5));
        seriesWaiting.getData().add(new XYChart.Data<>("5", 12));
        seriesWaiting.getData().add(new XYChart.Data<>("10", 2));
        seriesWaiting.getData().add(new XYChart.Data<>("15", 30)); // יום עמוס
        seriesWaiting.getData().add(new XYChart.Data<>("20", 8));
        seriesWaiting.getData().add(new XYChart.Data<>("25", 25));

        lineChartOrders.getData().addAll(seriesOrders, seriesWaiting);
    }
    @FXML
    void goBackBtn(ActionEvent event) {
        // Logic for signing out or returning to the main selection screen
      
        System.out.println("Going back / Signing out...");
        //MainNavigator.loadScreen("managerTeam/EmployeeOption" ,clientUi);
        ManagerOptionsController controller = 
        		super.loadScreen("managerTeam/EmployeeOption", event,clientUi);
    	if (controller != null) {
            controller.initData(clientUi,this.isManager);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
    }
	

    @FXML
    void closeScreen(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
    public void initData(ClientUi c, Employee.Role isManager)
    {
    	this.clientUi=c;
    	this.isManager=isManager;
    }

	@Override
	public void onMessageReceive(Object msg) {
		// TODO Auto-generated method stub
		
	}

	
}
