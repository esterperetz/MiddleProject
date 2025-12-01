package clientUi;

import client.ChatClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientUi extends Application {

    private ChatClient chatClient;
    private TextArea messagesArea;
    private TextField inputField;
    private Button sendButton;
    private String prompt = "Please enter your command number:"
    		+ "\n" + "1. Add_An_Order"
    		+ "\n" + "2. Edit_An Order"
    		+ "\n" + "3. Delete_An_Order"
    		+ "\n" + "4. Show_All_Orders"
    		+ "\n" + "5. quit";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // UI setup
        messagesArea = new TextArea();
        messagesArea.setEditable(false);
        messagesArea.setWrapText(true);
        messagesArea.appendText(prompt + "\n");

        inputField = new TextField();
        inputField.setPromptText("Enter your number of the selected command.");

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);

        // Layout
        VBox root = new VBox(10, messagesArea, inputField, sendButton);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Connect to server
        try {
            chatClient = new ChatClient("localhost", 5555, this); // Change host/port if needed
        } catch (Exception e) {
            displayMessage("Cannot connect to server: " + e.getMessage());
            e.printStackTrace();
        }

        // Send message on button click
        sendButton.setOnAction(event -> sendMessage());

        // Send message on Enter key
        inputField.setOnAction(event -> sendMessage());
    }

    /**
     * 
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && chatClient != null) {
            chatClient.handleMessageFromClientUI(message);
            inputField.clear();
        }
    }

    // Method called by ChatClient to display messages from server
    public void displayMessage(String msg) {
        Platform.runLater(() -> {
            messagesArea.appendText(msg + "\n");
            messagesArea.appendText(prompt + "\n");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
