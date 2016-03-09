package ui;

import defaultPart.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import sample.CalendarDate;
import sample.TaskCell;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {
	@FXML
	public ListView<Task> overDueTaskList;
	public ListView<Task> todayTaskList;
	public ListView<Task> floatingTaskList;

	public Label overdueCountLabel;
	public Label todayCountLabel;
	public Label floatingCountLabel;

	public StackPane root;
	public Label title;
	public TextField inputBox;

	// a cell factory to format the task into the displayable content in the ListView
	private static final Callback<ListView<Task>, ListCell<Task>> TASKCELL_FACTORY = (e) -> {
		return new ui.TaskCell();
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		overDueTaskList.setCellFactory(TASKCELL_FACTORY);
		todayTaskList.setCellFactory(TASKCELL_FACTORY);
		floatingTaskList.setCellFactory(TASKCELL_FACTORY);
		
		Task task = new Task();
		task.setDescription("hello");
	    overDueTaskList.setItems(FXCollections.observableArrayList(
                task
        ));
	}

}
