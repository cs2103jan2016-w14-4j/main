package tableUi;

import defaultPart.Logic;
import defaultPart.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
	@FXML
	public TableView<TaskModel> floatingTaskTable;
	public TableView<TaskModel> eventsTable;
	public TableColumn<TaskModel, Number> floatingTaskId;
	public TableColumn<TaskModel, String> floatingTaskDescription;
	public TableColumn<TaskModel, Boolean> floatingTaskCheckbox;
	public TableColumn<TaskModel, Number> eventsId;
	public TableColumn<TaskModel, String> eventsDescription;
	public TableColumn<TaskModel, String> eventsDate;
	public TableColumn<TaskModel, String> eventsRecur;
	public TableColumn<TaskModel, Boolean> eventsCheckbox;
	public TextField inputBox;
	public Label userPrompt;
	public Button addFloatingTask;
	public Button addEvent;
	public Button deleteFloatingTask;
	public Button deleteEvent;
	public Button showAllEvents;
	public Button showIncompleteEvents;
	public Button showOverdueEvents;
	public Button showCompletedEvents;


	private Logic logic;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logic = new Logic();
		floatingTaskId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		floatingTaskDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		eventsId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		eventsDate.setCellValueFactory(cellData -> cellData.getValue().dateTime());
		eventsRecur.setCellValueFactory(cellData -> {
			if(cellData.getValue().getIsRecur()){
				return cellData.getValue().recur();
			}else{
				return null;
			}
		});
	}

}
