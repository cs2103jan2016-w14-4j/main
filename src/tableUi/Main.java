package tableUi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	private static Controller controller;

	@Override
	public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout1.fxml"));
        Parent root = (Parent) loader.load();
		primaryStage.setTitle("WURI");
		Scene scene = new Scene(root, 800, 450);
		scene.getStylesheets().add(getClass().getResource("layout1.css").toExternalForm());
		primaryStage.setScene(scene);
		controller = loader.getController();
		controller.stage = primaryStage;
		primaryStage.show();
	}

	public static Controller getController() {
		return controller;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
