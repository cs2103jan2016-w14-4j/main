package sample;

import javafx.geometry.HPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ruomu on 2/24/16.
 */
public class TaskCell extends Region{
    private String taskID;
    private String title;
    private String desc;
    private Date dueDate;

    private GridPane pane;
    private CheckBox done;
    private Label titleLabel;
    private Label descLabel;
    private Label due;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd,MMM '['EEE']'");

    public TaskCell(String taskID, String title, String desc, Date dueDate) {
        this.taskID = taskID;
        this.title = title;
        this.desc = desc;
        this.dueDate = dueDate;

        this.setPrefWidth(200);

        descLabel = new Label(desc);
        pane = new GridPane();
        done = new CheckBox();
        titleLabel = new Label(title);
        due = new Label(dateFormat.format(dueDate));


        pane.add(done, 0, 0);
        pane.add(titleLabel, 1,0);
        pane.add(descLabel, 1, 1);
        pane.add(due, 1,2);

        this.getChildren().add(pane);
        this.getStyleClass().add("taskcell");
        descLabel.getStyleClass().add("desclabel");

        pane.setVgap(10);
        GridPane.setHalignment(due, HPos.RIGHT);

        titleLabel.getStyleClass().add("tasktitle");
        due.getStyleClass().add("taskdue");

        pane.getStyleClass().add("grid-pane");
        due.setPrefWidth(170);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, e->{
            done.setSelected(!done.isSelected());
        });

        done.selectedProperty().addListener((v, newValue, oldValue)->{
            if(newValue){
                this.getStyleClass().clear();
                this.getStyleClass().add("taskcell");
            }else{
                this.getStyleClass().add("completedtask");
            }
        });

    }
}
