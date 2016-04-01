package tableUi;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by houruomu on 2016/3/28.
 */
public class HelpWindow{
    private static HelpWindow ourInstance = new HelpWindow();
    private Stage window;

    @FXML
    public VBox root;
    public ImageView image;


	public static HelpWindow getInstance() {
		return ourInstance;
	}


    private HelpWindow() {
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UTILITY);
        window.setTitle("Help Window | Press ESC to exit...");

        try {
            image = new ImageView(getClass().getResource("help.jpg").toExternalForm());
            root = new VBox();
            root.getChildren().add(image);
            Scene scene = new Scene(root, 400, 600);
            window.setScene(scene);
            
            scene.addEventHandler(KeyEvent.KEY_PRESSED, e->{
                if(e.getCode().equals(KeyCode.ESCAPE) || e.getCode().equals(KeyCode.F1)){
                    window.close();
                }else{
                    System.out.println("Key pressed!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void show(){
        getInstance().showWindow();
    }

    public void showWindow(){
        window.showAndWait();
        root.requestFocus();
    }

}
