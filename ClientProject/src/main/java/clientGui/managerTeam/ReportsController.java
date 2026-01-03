package clientGui.managerTeam;

import java.util.Map;
import java.util.TreeMap;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import entities.ActionType;
import entities.Employee;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class ReportsController extends MainNavigator implements MessageListener<Object> {

    @FXML private BarChart<String, Number> barChartTimes;
    @FXML private LineChart<String, Number> lineChartOrders;
    @FXML private CategoryAxis xAxisTimes;
    @FXML private CategoryAxis xAxisDays;
    
    private Employee.Role isManager;

    @FXML
    public void initialize() {
        // Disable animation for smoother updates when data arrives
        barChartTimes.setAnimated(false);
        lineChartOrders.setAnimated(false);
    }

    /**
     * Initializes controller, registers listener, and requests report data.
     */
    public void initData(ClientUi c, Employee.Role isManager) {
        this.clientUi = c;
        this.isManager = isManager;
        
        // Register this controller to receive server messages
        this.clientUi.addListener(this);

        // Send request to server
        Request req = new Request(ResourceType.REPORT, ActionType.GET_MONTHLY_REPORT, null, null);
        this.clientUi.sendRequest(req);
    }

    /**
     * Handles the server response containing the nested Map structure.
     */
    @Override
    public void onMessageReceive(Object msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;

            // Check if this response is for the report request
            if (response.getAction() == ActionType.GET_MONTHLY_REPORT && 
                response.getStatus() == Response.ResponseStatus.SUCCESS) {

                // Unpack the main map (Map<String, Object>) from response.getData()
                @SuppressWarnings("unchecked")
                Map<String, Object> fullData = (Map<String, Object>) response.getData();
                
                if (fullData != null) {
                    // Extract inner maps using keys defined in Server ReportController
                    @SuppressWarnings("unchecked")
                    Map<Integer, Integer> arrivals = (Map<Integer, Integer>) fullData.get("arrivals");
                    @SuppressWarnings("unchecked")
                    Map<Integer, Integer> departures = (Map<Integer, Integer>) fullData.get("departures");
                    @SuppressWarnings("unchecked")
                    Map<Integer, Integer> cancellations = (Map<Integer, Integer>) fullData.get("cancellations");
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> dailyOrders = (Map<String, Integer>) fullData.get("dailyOrders");

                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        updateTimeChart(arrivals, departures, cancellations);
                        updateTrendsChart(dailyOrders);
                    });
                }
            }
        }
    }

    /**
     * Updates the BarChart with hourly activity data.
     */
    private void updateTimeChart(Map<Integer, Integer> arrivals, Map<Integer, Integer> departures, Map<Integer, Integer> cancellations) {
        barChartTimes.getData().clear();

        XYChart.Series<String, Number> seriesArrivals = new XYChart.Series<>();
        seriesArrivals.setName("Arrivals");
        
        XYChart.Series<String, Number> seriesDepartures = new XYChart.Series<>();
        seriesDepartures.setName("Departures");
        
        XYChart.Series<String, Number> seriesCancellations = new XYChart.Series<>();
        seriesCancellations.setName("Late/No-Show");

        // Iterate hours 0-23
        for (int i = 0; i < 24; i++) {
            String hourLabel = String.format("%02d:00", i);
            seriesArrivals.getData().add(new XYChart.Data<>(hourLabel, arrivals.getOrDefault(i, 0)));
            seriesDepartures.getData().add(new XYChart.Data<>(hourLabel, departures.getOrDefault(i, 0)));
            seriesCancellations.getData().add(new XYChart.Data<>(hourLabel, cancellations.getOrDefault(i, 0)));
        }

        barChartTimes.getData().addAll(seriesArrivals, seriesDepartures, seriesCancellations);
    }

    /**
     * Updates the LineChart with daily order trends.
     */
    private void updateTrendsChart(Map<String, Integer> dailyOrders) {
        lineChartOrders.getData().clear();

        XYChart.Series<String, Number> seriesOrders = new XYChart.Series<>();
        seriesOrders.setName("Total Orders");

        // Use TreeMap to sort dates chronologically (assuming format allows or TreeMap default sort)
        Map<String, Integer> sortedOrders = new TreeMap<>(dailyOrders);

        for (Map.Entry<String, Integer> entry : sortedOrders.entrySet()) {
            seriesOrders.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChartOrders.getData().add(seriesOrders);
    }

    @FXML
    void goBackBtn(ActionEvent event) {
        System.out.println("Going back...");
        // Navigate back to Manager Options
        ManagerOptionsController controller = 
                super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
        if (controller != null) {
            controller.initData(clientUi, this.isManager);
        } else {
            System.err.println("Error: Could not load ManagerOptionsController.");
        }
    }

    @FXML
    void closeScreen(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
}