package tableUi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

//@@author Hou Ruomu A0131421B
/**
 * Main class for the application
 * @author houruomu
 *
 */
public class TableUiMain extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("layout1.fxml"));
		Parent root = (Parent) loader.load();
		primaryStage.setTitle("WURI");
		Scene scene = new Scene(root, 800, 450);
		scene.getStylesheets().add(getClass().getResource("layout1.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setMinWidth(700);
		// primaryStage.setMaxHeight(480);
		primaryStage.setMinHeight(480);
		Controller controller = loader.getController();
		controller.stage = primaryStage;
		Font.loadFont(getClass().getResource("fonts/Futura_LT_Book.ttf").toExternalForm(), 15.0);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
