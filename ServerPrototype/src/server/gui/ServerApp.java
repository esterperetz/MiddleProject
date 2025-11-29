package server.gui;
import javafx.application.Application; // עבור extends Application
import javafx.stage.Stage;             // עבור Stage primaryStage
import javafx.scene.Scene;             // עבור Scene scene
import javafx.fxml.FXMLLoader;         // עבור FXMLLoader loader

import java.sql.Connection;          // עבור Connection con

// ייבוא המחלקות שנוצרו בפרויקט שלך:
import DBConnection.DBConnection;          // עבור ServerModel
import server.controller.ServerController;  // עבור ServerController
// עבור DBConnectionFactory/DBConnection
// נניח ש-DBConnectionFactory היא מחלקה באחת החבילות שראינו קודם:
public class ServerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/view/server_view.fxml"));
        Scene scene = new Scene(loader.load());
        ServerViewController viewController = loader.getController();
        DBConnection model = new DBConnection("root","159357","mid_project_prototype");
        ServerController server = new ServerController(5555, model, viewController);
        server.listen();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.show();

        viewController.log("Server listening on port 5555");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
