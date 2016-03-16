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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.net.URL;
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

	private static final boolean DEVELOPER_MODE = true;
	private static final String EDIT_COMMAND = "e %d %s";
	private static final String DELETE_COMMAND = "d %d";

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
		eventsDate.setCellValueFactory(cellData -> cellData.getValue().dateTime());
		eventsRecur.setCellValueFactory(cellData -> {
			if(cellData.getValue().getIsRecur()){
				return cellData.getValue().recur();
			}else{
				return null;
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
		userPrompt.setText(prompt);
	}

	public void addFloatingTask(){
		Task newTask = new Task();
		newTask.setDescription("sample Task");
		floatingTaskList.add(new TaskModel(newTask, ++lastId));
	}

	public void addEvent(){
		Task newTask = new Task();
		newTask.setDescription("sample Task");
		newTask.setEndTime(Calendar.getInstance());
		eventList.add(new TaskModel(newTask, ++lastId));
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
				floatingTaskList.add(new TaskModel(task, i+1));
			}else{
				eventList.add(new TaskModel(task, i+1));
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
		eventList.stream().filter(e -> e.getIsComplete()).forEach(e -> eventList.remove(e));
		floatingTaskList.stream().filter(e -> e.getIsComplete()).forEach(e -> eventList.remove(e));
	}

	public void showOverdueEvents(){
		Calendar today = new GregorianCalendar();
		showAllTasks();
		eventList.stream().filter(e -> e.getTask().getEndTime().compareTo(today) == 1).forEach(e -> eventList.remove(e));
		floatingTaskList.stream().filter(e -> e.getTask().getEndTime().compareTo(today) == 1).forEach(e -> eventList.remove(e));
	}

	public void setShowCompletedEvents(){
		showAllTasks();
		eventList.stream().filter(e -> !e.getIsComplete()).forEach(e -> eventList.remove(e));
		floatingTaskList.stream().filter(e -> !e.getIsComplete()).forEach(e -> eventList.remove(e));
	}

}
