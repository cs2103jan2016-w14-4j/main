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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.SSLException;

import org.xml.sax.SAXException;

import com.sun.media.jfxmedia.logging.Logger;

/**
 * Class for the main controller of the UI
 * 
 * @author Hou Ruomu A0131421B
 */
public class Controller implements Initializable {
	@FXML
	public VBox root;
	public SplitPane pane;
	public VBox rightPane;
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
	public static final String INVALID_EDIT_DATE_PROMPT = "edit date action could not be done on id %d";
	public static final String INVALID_EDIT_DESCRIPTION_PROMPT = "edit date action could not be done on id %d";

	private List<Task> taskList;

	private ObservableList<TaskModel> floatingTaskList;
	private ObservableList<TaskModel> eventList;
	private ArrayList<TaskModel> taskModels;

	private int tablePosition;
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
		tablePosition = 0;

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
				String dateString = newDate.toString();
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
			} else if (e.getCode().equals(KeyCode.DOWN)) {
				scrollDown();
			} else if (e.getCode().equals(KeyCode.UP)) {
				scrollUp();
			} else if (e.getCode().equals(KeyCode.F1)) {
				showHelp();
			}
		});

		root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode().equals(KeyCode.F1)) {
				showHelp();
			}
		});

		pane.lookupAll(".split-pane-divider").stream().forEach(div -> div.setMouseTransparent(true));

		// delayed setter (set only after the loading of the software is done)
		new Thread(() -> {
			try {
				// Thread.sleep(100);
				// root.widthProperty().addListener(e -> {
				// resizeColumns();
				// });
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}).start();

		logic = new Logic();
		try {
			logic.loadTasksFromFile();
			showAllTasks();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			setUserPrompt("ERROR: " + e.getMessage());
		} catch (ParseException e) {
			setUserPrompt("Parse of tasklist failed: " + e.getMessage() + ". Overriding tasklist...");
			logic.deleteTaskListFile();
			// todo: override only if user agrees
		}

	}

	public void showHelp() {
		HelpWindow.show();
	}

	public Stage getStage() {
		return stage;
	}

	public void debug() {
		toggleRightPane();
		resizeColumns();
	}

	public void toggleRightPane() {
		if (rightPane.isVisible()) {
			rightPane.setVisible(false);
			pane.setDividerPosition(0, 1);
		} else {
			rightPane.setVisible(true);
			pane.setDividerPosition(0, 0.619);
		}

	}

	public void resizeColumns() {
		final double paneWidth = pane.getWidth();

		final double floatingTableWidth = rightPane.isVisible() ? 304.8 : 0;
		final double dividerPosition = 1.0 - (floatingTableWidth / paneWidth);

		floatingTaskTable.setPrefWidth(floatingTableWidth);
		pane.setDividerPositions(dividerPosition);

		final double eventsTableWidth = paneWidth * dividerPosition - 5; // 5 for padding
		final double idWidth = 20;
		final double checkBoxWidth = 20;
		final double recurWidth = Math.min(eventsTableWidth * 0.2, 100);
		final double dateWidth = Math.min(eventsTableWidth * 0.2, 200);
		final double descWidth = eventsTableWidth - idWidth - checkBoxWidth - recurWidth - dateWidth;

		eventsTable.setPrefWidth(eventsTableWidth);
		eventsId.setPrefWidth(idWidth);
		eventsCheckbox.setPrefWidth(checkBoxWidth);
		eventsRecur.setPrefWidth(recurWidth);
		eventsDate.setPrefWidth(dateWidth);
		eventsDescription.setPrefWidth(descWidth);
	}

	/**
	 * Set the user prompt to the value String in the parameter
	 * 
	 * @param prompt
	 *            The prompt String, length should be less than 100
	 */
	public void setUserPrompt(String prompt) {
		assert (prompt != null);
		// the length of feedback should not be longer than 100 characters
		if (prompt.length() > 100) {
			prompt = prompt.substring(0, 97) + "...";
		}
		if (DEVELOPER_MODE)
			System.out.println("Sent back to user: " + prompt);
		userPrompt.setText(prompt);
	}

	public void editDescriptionById(int id) {
		if (taskList.get(id).getDate() != null) {
			editEventDescriptionById(id);
		} else {
			editFloatingTaskDescriptionById(id);
		}
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
		clearInterface();
		taskList = logic.getTaskList();
		for (int i = 0; i < taskList.size(); i++) {
			addToTaskModels(i);
		}
		tablePosition = 0;
		scrollTo(tablePosition);
	}

	private void clearInterface() {
		eventList.clear();
		floatingTaskList.clear();
		taskModels.clear();
		tablePosition = 0;
		scrollTo(tablePosition);
	}

	private void scrollDown() {
		tablePosition += 5;
		if (tablePosition > Math.max(eventList.size(), floatingTaskList.size()))
			tablePosition = Math.max(eventList.size(), floatingTaskList.size());
		scrollTo(tablePosition);
	}

	private void scrollUp() {
		tablePosition -= 5;
		if (tablePosition < 0)
			tablePosition = 0;
		scrollTo(tablePosition);
	}

	private void scrollTo(int tablePosition) {
		eventsTable.scrollTo(tablePosition);
		floatingTaskTable.scrollTo(tablePosition);
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
				List<Integer> indexesFound = logic.getIndexesFound();
				editDescriptionById(indexesFound.get(0) + 1);
				break;

			case FIND :
				displayFoundTask();
				break;

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
