package ui;


import defaultPart.Task;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Calendar;

/**
 * TasCellController is the Controller for the taskcell customized control
 * @author Hou Ruomu
 */
public class TaskCellController extends Region{

    @FXML
    public Text recurText;
    public Text dueText;
    public Text startText;
    public Text taskIdText;
    public TextArea descriptionText;
    public CheckBox checkBox;
    public VBox root;

    private Task task;

    public void refresh(){
        try {
           // taskIdText.setText("Task ID: " + task.getIndex());

            descriptionText.setText(task.getDescription());

            if (task.isCompleted())
                checkBox.setSelected(true);

            if (task.getRecur().willRecur())
                recurText.setText("Recur: " + task.getRecur().getTimeUnit().toString());
            
            Calendar calendar = task.getDate();
            if (calendar != null)
                dueText.setText("Due: " + calendar.getTime());

            calendar = task.getStartTime();
            if (calendar != null)
                startText.setText("Due: " + calendar.getTime());
        }catch(NullPointerException e){
            System.err.println("Error running: " + e.toString() + "\n check default constructor of Task!");
        }

        descriptionText.mouseTransparentProperty().bind(descriptionText.editableProperty().not());

        checkBox.selectedProperty().addListener((p,o,n)->{
            if(n)
                task.toggleCompleted();
        });
    }

    public void editTaskDescription(){
        descriptionText.setEditable(true);
        descriptionText.requestFocus();
        descriptionText.positionCaret(Integer.MAX_VALUE);
        descriptionText.focusedProperty().addListener((p,o,n)->{
            if(o && !n){
                descriptionText.setEditable(false);
                task.setDescription(descriptionText.getText());
            }
            descriptionText.deselect();
        });
    }

    public void setTask(Task task){
        this.task = task;
    }
}
