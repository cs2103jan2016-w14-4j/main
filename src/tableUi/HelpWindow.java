package tableUi;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by houruomu on 2016/3/28.
 */
public class HelpWindow implements Initializable {
	private static HelpWindow ourInstance = new HelpWindow();
	private Stage window;

	@FXML
	public AnchorPane pane;
	public ImageView image;

	public static HelpWindow getInstance() {
		return ourInstance;
	}

	private HelpWindow() {
		window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Help ESC to exit...");

		try {
			FXMLLoader loader = new FXMLLoader();
			Scene scene = loader.load(getClass().getResource("help.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		pane.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode().equals(KeyCode.ESCAPE)) {
				window.close();
			} else {
				System.out.println("Key pressed!");
			}
		});

		image.setImage(new Image(getClass().getResource("hello.jpg").toExternalForm()));
	}

	public static void show() {
		getInstance().showWindow();
	}

	private void showWindow() {
		window.showAndWait();
	}
}
