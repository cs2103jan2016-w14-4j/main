package tableUi;

import defaultPart.Logic;
import defaultPart.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import org.xml.sax.SAXException;

/**
 * Class for the main controller of the UI
 * 
 * @author Hou Ruomu A0131421B
 */
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
	public Button deleteFloatingTask;
	public Button deleteEvent;
	public Button showAllEvents;
	public Button showIncompleteEvents;
	public Button showOverdueEvents;
	public Button showCompletedEvents;

	public Stage stage;

	public static final boolean DEVELOPER_MODE = true;
	public static final String EDIT_COMMAND = "e %d %s";
	public static final String DELETE_COMMAND = "d %d";
	public static final String TOGGLE_COMMAND = "t %d";
	public static final String INVALID_DATE_PROMPT = "\"%s\" is not a valid date format, use dd/MM/yy";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("K.mma");
	public static final String INVALID_EDIT_DATE_PROMPT = "edit date action could not be done on id %d";
	public static final String INVALID_EDIT_DESCRIPTION_PROMPT = "edit date action could not be done on id %d";

	private List<Task> taskList;

	private ObservableList<TaskModel> floatingTaskList;
	private ObservableList<TaskModel> eventList;
	private ArrayList<TaskModel> taskModels;

	private int lastId;

	private Logic logic;

	/**
	 * Initialize the controllers, define the listeners for each control
	 * 
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		floatingTaskList = FXCollections.observableArrayList();
		eventList = FXCollections.observableArrayList();
		taskModels = new ArrayList<>();
		lastId = 0;

		floatingTaskTable.setItems(floatingTaskList);
		eventsTable.setItems(eventList);

		floatingTaskId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsRecur.setCellValueFactory(cellData -> {
			if (cellData.getValue().getIsRecur()) {
				return cellData.getValue().recur();
			} else {
				return null;
			}
		});

		floatingTaskCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		floatingTaskCheckbox.setCellFactory(e -> new CheckBoxTableCell());

		eventsCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		eventsCheckbox.setCellFactory(e -> new CheckBoxTableCell());

		eventsDate.setCellValueFactory(cellData -> cellData.getValue().dateTime());
		eventsDate.setCellFactory(TextFieldTableCell.forTableColumn());
		eventsDate.setOnEditCommit(e -> {
			TaskModel taskModel = e.getTableView().getItems().get(e.getTablePosition().getRow());
			int id = taskModel.getTaskId();
			try {
				Calendar newDate = logic.getWrappedDateFromString(e.getNewValue());
				String dateString = DATE_FORMAT.format(newDate.getTime());
				sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, dateString));
			} catch (Exception exception) {
				// if the date format is invalid
				setUserPrompt(String.format(INVALID_DATE_PROMPT, e.getNewValue()));
				e.consume();
				// refresh the column to undo the change
				eventsDate.setVisible(false);
				eventsDate.setVisible(true);
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

		inputBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				String text = inputBox.getText();
				inputBox.clear();
				e.consume();
				sendToLogicAndUpdatePrompt(text);
			}
		});

		logic = new Logic();
		try {
			logic.loadTasksFromFile();
			showAllTasks();	
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void debug() {
		List<Integer> indexesFound = logic.getIndexesFound();
		editEventDescriptionById(indexesFound.get(0));
	}

	/**
	 * Set the user prompt to the value String in the parameter
	 * 
	 * @param prompt
	 *            The prompt String, length should be less than 100
	 */
	public void setUserPrompt(String prompt) {
		// the length of feedback should not be longer than 100 characters
		if (prompt.length() > 100) {
			prompt = prompt.substring(0, 97) + "...";
		}
		if (DEVELOPER_MODE)
			System.out.println("Sent back to user: " + prompt);
		userPrompt.setText(prompt);
	}

	/**
	 * This method gets the row number of a task from the corresponding task Model
	 * 
	 * @param task
	 * @return The row index of the task
	 */
	private int getRowFromModel(TaskModel task) {
		if (task.getIsEvent()) {
			return eventsTable.getItems().indexOf(task);
		} else {
			return floatingTaskTable.getItems().indexOf(task);
		}
	}

	/**
	 * Initialize the editing event of a date tableCell related to the task ID, the task is an event
	 * 
	 * @param id
	 */
	public void editEventDateById(int id) {
		try {
			eventsTable.edit(getRowFromModel(getTaskModelFromId(id)), eventsDate);
		} catch (Exception e) {
			setUserPrompt(String.format(INVALID_EDIT_DATE_PROMPT, id));
		}
	}

	/**
	 * Initialize the editing event of a Description tableCell related to the task ID, the task is an event
	 * 
	 * @param id
	 */
	public void editEventDescriptionById(int id) {
		try {
			eventsTable.edit(getRowFromModel(getTaskModelFromId(id)), eventsDescription);
		} catch (Exception e) {
			setUserPrompt(String.format(INVALID_EDIT_DESCRIPTION_PROMPT, id));
		}
	}

	/**
	 * Initialize the editing event of a Description tableCell related to the task ID, the task is a floating
	 * task
	 * 
	 * @param id
	 */
	public void editFloatingTaskDescriptionById(int id) {
		try {
			floatingTaskTable.edit(getRowFromModel(getTaskModelFromId(id)), floatingTaskDescription);
		} catch (Exception e) {
			setUserPrompt(String.format(INVALID_EDIT_DESCRIPTION_PROMPT, id));
		}
	}

	/**
	 * Get the taskModel from its id
	 * 
	 * @param id
	 * @return
	 */
	private TaskModel getTaskModelFromId(int id) {
		return taskModels.get(id - 1);
	}

	/**
	 * Delete a floating task which has been selected in the tableView
	 */
	public void deleteFloatingTask() {
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

	/**
	 * Delete an event which has been selected in the tableView
	 */
	public void deleteEvent() {
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

	/**
	 * Let the tableView show all the tasks
	 */
	public void showAllTasks() {
		clearInterface();
		retrieveTaskFromStorage();
		inputBox.requestFocus();
	}

	/**
	 * refresh the tasks shown on UI based on the current storage (by default all tasks are shown)
	 */
	private void retrieveTaskFromStorage() {
		lastId = 0;
		taskList = logic.getTaskList();

		for (int i = 0; i < taskList.size(); i++) {
			addToTaskModels(i);
		}
	}

	private void clearInterface() {
		eventList.clear();
		floatingTaskList.clear();
		taskModels.clear();
	}

	private void addToTaskModels(int i) {
		Task task = taskList.get(i);
		TaskModel newModel = new TaskModel(task, i + 1, this);
		if (task.getDate() == null) {
			floatingTaskList.add(newModel);
		} else {
			eventList.add(newModel);
		}
		taskModels.add(newModel);
		lastId++;
	}

	/**
	 * Send a command to the Logic and update the tasks and prompt shown in the UI
	 * 
	 * @param command
	 *            The command that is going to be send to the parser
	 */
	public void sendToLogicAndUpdatePrompt(String command) {
		logic.executeCommand(command);
		if (DEVELOPER_MODE) {
			System.out.println("Send to logic: " + command);
		}
		switch (logic.getCommandType()) {
			case EDIT_SHOW_TASK :
				debug();
			case QUIT :
				System.exit(0);
			case FIND :
				displayFoundTask();
			default :
				showAllTasks();
		}
		setUserPrompt(logic.getFeedback());
		logic.saveTasksToFile();
	}

	private void displayFoundTask() {
		clearInterface();
		List<Integer> indexesFound = logic.getIndexesFound();
		for (int index : indexesFound) {
			addToTaskModels(index);
		}
		inputBox.requestFocus();
	}

	/**
	 * show all tasks that are incompleted
	 */
	public void showIncompleteEvents() {
		retrieveTaskFromStorage();
		eventList.removeIf(e -> e.getIsComplete());
		floatingTaskList.removeIf(e -> e.getIsComplete());
	}

	/**
	 * Show all the tasks which have end time before today
	 */
	public void showOverdueEvents() {
		Calendar today = new GregorianCalendar();
		retrieveTaskFromStorage();
		eventList.removeIf(e -> e.getTask().getEndTime().compareTo(today) == 1);
		floatingTaskList.removeIf(e -> e.getTask().getEndTime().compareTo(today) == 1);
	}

	/**
	 * Show all the tasks that are completed
	 */
	public void setShowCompletedEvents() {
		retrieveTaskFromStorage();
		eventList.removeIf(e -> !e.getIsComplete());
		floatingTaskList.removeIf(e -> !e.getIsComplete());
	}

	public void close() {
		if (stage != null) {
			stage.close();
		}
	}
}
