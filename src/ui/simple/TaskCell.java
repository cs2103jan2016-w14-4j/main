package ui.simple;

import defaultPart.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

import java.io.IOException;
import java.net.URL;

/**
 * Created by houruomu on 2016/3/5.
 */
public class TaskCell extends ListCell<Task> {
    @Override
    protected void updateItem(Task task, boolean isEmpty){
        super.updateItem(task, isEmpty);

        if(task != null){
            URL location = TaskCellController.class.getResource("taskItem.fxml");

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

            try
            {
                Node root = fxmlLoader.load(location.openStream());
                TaskCellController controller = fxmlLoader.getController();
                controller.setTask(task);
                controller.refresh();
                setGraphic(root);
            }
            catch(IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}
