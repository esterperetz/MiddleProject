package clientGui.managerTeam;

import java.time.LocalDate;
import java.util.ArrayList;
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
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;

public class ReportsController extends MainNavigator implements MessageListener<Object> {

    @FXML
    private AreaChart<String, Number> barChartTimes;
    @FXML
    private AreaChart<String, Number> lineChartOrders;
    @FXML
    private ComboBox<String> comboMonth;
    @FXML
    private ComboBox<String> comboYear;
    private Employee.Role role;

    @FXML
    public void initialize() {
        // Disable animations for performance/stability
        if (barChartTimes != null)
            barChartTimes.setAnimated(false);
        if (lineChartOrders != null)
            lineChartOrders.setAnimated(false);

        if (comboMonth != null) {
            comboMonth.setItems(FXCollections.observableArrayList("01", "02", "03", "04", "05", "06", "07", "08", "09",
                    "10", "11", "12"));
        }

        if (comboYear != null) {
            ArrayList<String> years = new ArrayList<>();
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 5; i++) {
                years.add(String.valueOf(currentYear - i));
            }
            comboYear.setItems(FXCollections.observableArrayList(years));
        }
    }

    public void initData(ClientUi c, Employee.Role role) {
        this.clientUi = c;
        this.role = role;

        // precise default value setting
        LocalDate now = LocalDate.now();
        if (comboMonth != null)
            comboMonth.setValue(String.format("%02d", now.getMonthValue()));
        if (comboYear != null)
            comboYear.setValue(String.valueOf(now.getYear()));

        this.clientUi.addListener(this);
        sendRequest();
    }

    @FXML
    void refreshReportsBtn(ActionEvent e) {
        sendRequest();
    }

    private void sendRequest() {
        if (comboMonth == null || comboYear == null)
            return;
        String filter = comboMonth.getValue() + "/" + comboYear.getValue();
        clientUi.sendRequest(new Request(ResourceType.REPORT, ActionType.GET_MONTHLY_REPORT, null, filter));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessageReceive(Object msg) {
        if (msg instanceof Response) {
            Response res = (Response) msg;
            if (res.getAction() == ActionType.GET_MONTHLY_REPORT
                    && res.getStatus() == Response.ResponseStatus.SUCCESS) {
                // We trust the server to send the Map<String, Object> structure
                Map<String, Object> data = (Map<String, Object>) res.getData();

                Platform.runLater(() -> {
                    try {
                        updateTimeChart((Map<Integer, Integer>) data.get("arrivals"),
                                (Map<Integer, Integer>) data.get("departures"),
                                (Map<Integer, Integer>) data.get("cancellations"));
                        updateTrendsChart((Map<String, Integer>) data.get("dailyOrders"));
                    } catch (Exception e) {
                        e.printStackTrace(); // Log casting errors if they occur
                    }
                });
            }
        }
    }

    private void updateTimeChart(Map<Integer, Integer> arr, Map<Integer, Integer> dep, Map<Integer, Integer> canc) {
        if (barChartTimes == null)
            return;

        barChartTimes.getData().clear();

        XYChart.Series<String, Number> s1 = new XYChart.Series<>();
        s1.setName("Arrivals");

        XYChart.Series<String, Number> s2 = new XYChart.Series<>();
        s2.setName("Departures");

        XYChart.Series<String, Number> s3 = new XYChart.Series<>();
        s3.setName("Late/No-Show");

        for (int i = 0; i < 24; i++) {
            String h = String.format("%02d:00", i);
            int val1 = arr != null ? arr.getOrDefault(i, 0) : 0;
            int val2 = dep != null ? dep.getOrDefault(i, 0) : 0;
            int val3 = canc != null ? canc.getOrDefault(i, 0) : 0;

            s1.getData().add(new XYChart.Data<>(h, val1));
            s2.getData().add(new XYChart.Data<>(h, val2));
            s3.getData().add(new XYChart.Data<>(h, val3));
        }
        barChartTimes.getData().addAll(s1, s2, s3);
    }

    private void updateTrendsChart(Map<String, Integer> daily) {
        if (lineChartOrders == null)
            return;

        lineChartOrders.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Total Orders");

        if (daily != null) {
            // Using TreeMap to sort by date string (simple sort, assumes DD/MM format holds
            // somewhat or is sorted by server)
            // Ideally server sorts it. ReportDAO already sorts by date.
            new TreeMap<>(daily).forEach((k, v) -> s.getData().add(new XYChart.Data<>(k, v)));
        }

        lineChartOrders.getData().add(s);
    }

    @FXML
    void goBackBtn(ActionEvent e) {
        // Assuming ManagerOptionsController exists and handles initData
        // We use super.loadScreen which returns MainNavigator, so we cast to
        // ManagerOptionsController
        // Correct usage depends on the actual return type of loadScreen being generic
        // or overridden
        // But based on user code it seemed to work.
        ManagerOptionsController c = super.loadScreen("managerTeam/EmployeeOption", e, clientUi);
        if (c != null)
            c.initData(clientUi, role);
    }
}