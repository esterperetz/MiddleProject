package clientGui.reservation;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import clientGui.navigation.MainNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class WaitingListController implements Initializable {

    @FXML
    private DatePicker filterDate;

    @FXML
    private TableView<?> waitingListTable; // Change <?> to your Entity class (e.g., <WaitingListEntry>)

    @FXML
    private TableColumn<?, ?> colPosition;
    @FXML
    private TableColumn<?, ?> colName;
    @FXML
    private TableColumn<?, ?> colPhone;
    @FXML
    private TableColumn<?, ?> colGuests;
    @FXML
    private TableColumn<?, ?> colTime;
    @FXML
    private TableColumn<?, ?> colStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns here
        // Set date picker to today's date by default if you want:
        // filterDate.setValue(LocalDate.now());
    }

    /**
     * Triggered when a date is picked from the calendar.
     */
    @FXML
    void handleDateSelect(ActionEvent event) {
        LocalDate selectedDate = filterDate.getValue();
        if (selectedDate != null) {
            System.out.println("Filtering list for date: " + selectedDate);
            // TODO: Call server to get waiting list for this specific date
            // loadWaitingList(selectedDate);
        }
    }

    /**
     * Clears the date filter and shows all entries.
     */
    @FXML
    void handleClearFilter(ActionEvent event) {
        filterDate.setValue(null);
        System.out.println("Filter cleared. Showing all entries.");
        // TODO: Load all entries
    }

    @FXML
    void handleAssignTable(ActionEvent event) {
        System.out.println("Assign Table Clicked");
        // Logic to move from waiting list to active table
    }

    @FXML
    void handleRemoveEntry(ActionEvent event) {
        System.out.println("Remove Entry Clicked");
        // Logic to remove selected row
    }

    @FXML
    void handleBackBtn(ActionEvent event) {
        MainNavigator.loadScene("managerTeam/workerOption");
    }
}