package tableUi;

import defaultPart.Logic;
import defaultPart.Storage;
import defaultPart.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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

	public static final boolean DEVELOPER_MODE = true;
	public static final String EDIT_COMMAND = "e %d %s";
	public static final String EDIT_DATE = "e %d %s %s";
	public static final String DELETE_COMMAND = "d %d";
	public static final String TOGGLE_COMMAND = "t %d";
	public static final String INVALID_DATE_PROMPT = "\"%s\" is not a valid date format, use dd/MM/yy";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

	private List<Task> taskList;

	private ObservableList<TaskModel> floatingTaskList;
	private ObservableList<TaskModel> eventList;

	private int lastId;

	private Logic logic;
	private Storage storage;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		floatingTaskList = FXCollections.observableArrayList();
		eventList = FXCollections.observableArrayList();
		lastId = 0;

		floatingTaskTable.setItems(floatingTaskList);
		eventsTable.setItems(eventList);

		floatingTaskId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsRecur.setCellValueFactory(cellData -> {
			if(cellData.getValue().getIsRecur()){
				return cellData.getValue().recur();
			}else{
				return null;
			}
		});

		floatingTaskCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		floatingTaskCheckbox.setCellFactory(e -> new CheckBoxTableCell());

		eventsCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		eventsCheckbox.setCellFactory(e -> new CheckBoxTableCell());

		eventsDate.setCellValueFactory(cellData -> cellData.getValue().dateTime());
		eventsDate.setCellFactory(TextFieldTableCell.forTableColumn());
		eventsDate.setOnEditCommit(e ->{
			TaskModel taskModel = e.getTableView().getItems().get(e.getTablePosition().getRow());
			int id = taskModel.getTaskId();
			try{
				Calendar newDate = logic.getDateFromString(e.getNewValue());
				String dateString = DATE_FORMAT.format(newDate.getTime());
				sendToLogicAndUpdatePrompt(String.format(EDIT_DATE, id, taskModel.getTaskDescription(), dateString));
			}catch(Exception exception){
				setUserPrompt(String.format(INVALID_DATE_PROMPT, e.getNewValue()));
				e.consume();
			}
		});


		floatingTaskDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		floatingTaskDescription.setCellFactory(TextFieldTableCell.forTableColumn());
		floatingTaskDescription.setOnEditCommit(e -> {
			int id = e.getTableView().getItems().get(e.getTablePosition().getRow()).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, e.getNewValue()));
		});

		eventsDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		eventsDescription.setCellFactory(TextFieldTableCell.forTableColumn());
		eventsDescription.setOnEditCommit(e -> {
			int id = e.getTableView().getItems().get(e.getTablePosition().getRow()).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, e.getNewValue()));
		});


		storage = new Storage();
		logic = new Logic(storage);
		inputBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if(e.getCode().equals(KeyCode.ENTER)){
				sendToLogicAndUpdatePrompt(inputBox.getText());
				inputBox.clear();
			}
		});

		inputBox.requestFocus();
	}

	public void setUserPrompt(String prompt){
		// the length of feedback should not be longer than 100 characters
		assert(prompt.length() <= 100);
		if(DEVELOPER_MODE)
			System.out.println("Sent back to user: " + prompt);
		userPrompt.setText(prompt);
	}

	public void addFloatingTask(){
		Task newTask = new Task();
		newTask.setDescription("sample Task");
		floatingTaskList.add(new TaskModel(newTask, ++lastId, this));
	}

	public void addEvent(){
		Task newTask = new Task();
		newTask.setDescription("sample Task");
		newTask.setEndTime(Calendar.getInstance());
		eventList.add(new TaskModel(newTask, ++lastId, this));
	}

	public void deleteFloatingTask(){
		int selectedIndex = floatingTaskTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			int id = floatingTaskTable.getItems().get(selectedIndex).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(DELETE_COMMAND, id));
		} else {
			// Nothing selected
			setUserPrompt("No floating task is selected");
		}
		inputBox.requestFocus();
	}

	public void deleteEvent(){
		int selectedIndex = eventsTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			int id = eventsTable.getItems().get(selectedIndex).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(DELETE_COMMAND, id));
		} else {
			// Nothing selected
			setUserPrompt("No event is selected");
		}
		inputBox.requestFocus();
	}

	public void updateTaskList(List<Task> tasks){
		this.taskList = tasks;
	}

	public void showAllTasks(){
		lastId = 0;
		eventList.clear();
		floatingTaskList.clear();
		taskList = storage.getTaskList();

		for(int i = 0; i < taskList.size(); i++){
			Task task = taskList.get(i);
			if(task.getDate() == null){
				floatingTaskList.add(new TaskModel(task, i+1, this));
			}else{
				eventList.add(new TaskModel(task, i+1, this));
			}
			lastId++;
		}
		inputBox.requestFocus();
	}

	public void sendToLogicAndUpdatePrompt(String command){
		logic.executeCommand(command);
		if(DEVELOPER_MODE){
			System.out.println("Send to logic: " + command);
		}
		setUserPrompt(logic.getFeedback());
		showAllTasks();
	}

	public void showIncompleteEvents(){
		showAllTasks();
		eventList.removeIf(e->e.getIsComplete());
		floatingTaskList.removeIf(e->e.getIsComplete());
	}

	public void showOverdueEvents(){
		Calendar today = new GregorianCalendar();
		showAllTasks();
		eventList.removeIf(e -> e.getTask().getEndTime().compareTo(today) == 1);
		floatingTaskList.removeIf(e -> e.getTask().getEndTime().compareTo(today) == 1);
	}

	public void setShowCompletedEvents(){
		showAllTasks();
		eventList.removeIf(e->!e.getIsComplete());
		floatingTaskList.removeIf(e->!e.getIsComplete());
	}

}
