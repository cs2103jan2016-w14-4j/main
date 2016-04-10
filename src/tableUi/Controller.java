package tableUi;

import defaultPart.CommandInfo;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.xml.sax.SAXException;

//@@author Hou Ruomu

/**
 * Class for the main controller of the UI
 * 
 * @author Hou Ruomu A0131421B
 */
public class Controller implements Initializable {
	private static final int FEEDBACK_LENGTH_LIMIT = 100;
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
	public Button toggleButton;

	public Stage stage;

	public static final String EDIT_COMMAND = "e %d %s";
	public static final String DELETE_COMMAND = "d %d";
	public static final String TOGGLE_COMMAND = "c %d";
	public static final String INVALID_DATE_PROMPT = "\"%s\" is not a valid date format, use dd/MM/yy";
	public static final String INVALID_EDIT_DATE_PROMPT = "edit date action could not be done on id %d";
	public static final String INVALID_EDIT_DESCRIPTION_PROMPT = "edit date action could not be done on id %d";

	private ObservableList<TaskModel> floatingTaskList;
	private ObservableList<TaskModel> eventList;
	private ArrayList<TaskModel> taskModels;

	private int tablePosition;
	private Logic logic;

	private static final Logger logger = Logger.getLogger(Controller.class.getName());

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

		setupLogger();

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

		Callback<TableColumn<TaskModel, Boolean>, TableCell<TaskModel, Boolean>> checkBoxCellFactory = e -> new CheckBoxTableCell<TaskModel, Boolean>() {
			@Override
			public void updateItem(final Boolean item, final boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					this.getTableRow().getStyleClass().remove("completedTaskRow");
					this.getTableRow().getStyleClass().remove("incompleteTaskRow");
					this.getTableRow().getStyleClass().add(item ? "completedTaskRow" : "incompleteTaskRow");
				}

			}
		};

		floatingTaskCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		floatingTaskCheckbox.setCellFactory(checkBoxCellFactory);

		eventsCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		eventsCheckbox.setCellFactory(checkBoxCellFactory);

		eventsDate.setCellValueFactory(cellData -> cellData.getValue().dateTime());
		eventsDate.setCellFactory(TextFieldTableCell.forTableColumn());
		eventsDate.setOnEditCommit(e -> {
			TaskModel taskModel = e.getTableView().getItems().get(e.getTablePosition().getRow());
			int id = taskModel.getTaskId();
			try {
				Calendar newDate = logic.getWrappedDateFromString(e.getNewValue());
				String dateString = newDate.toString();
				sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, dateString));
				logger.fine("Edited date of task " + id + ". New date is " + dateString);
			} catch (Exception exception) {
				// if the date format is invalid
				setUserPrompt(String.format(INVALID_DATE_PROMPT, e.getNewValue()));
				e.consume();
				// refresh the column to undo the change
				eventsDate.setVisible(false);
				eventsDate.setVisible(true);
				logger.fine("Bad Input for Date: " + e.getNewValue());
			}
		});

		StringConverter<String> simpleStringConverter = new StringConverter<String>() {
			@Override
			public String toString(String object) {
				return object.toString();
			}

			@Override
			public String fromString(String string) {
				return string;
			}
		};

		Callback<TableColumn<TaskModel, String>, TableCell<TaskModel, String>> descriptionCellFactory = e -> new TextFieldTableCell<TaskModel, String>(
				simpleStringConverter) {
			private Text textDisplay;

			@Override
			public void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(null);
				if (!isEmpty()) {
					textDisplay = new Text(item.toString());
					textDisplay.setWrappingWidth(this.getTableColumn().getWidth() - 5); // 5 is the padding
					setGraphic(textDisplay);
					textDisplay.getStyleClass().clear();
					textDisplay.getStyleClass().add("text");
				}

			}

			@Override
			public void commitEdit(String item) {
				super.commitEdit(item);
				updateItem(item, false);
			}

			@Override
			public void cancelEdit() {
				updateItem(getItem(), false);
			}
		};

		floatingTaskDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		floatingTaskDescription.setCellFactory(descriptionCellFactory);
		floatingTaskDescription.setOnEditCommit(e -> {
			int id = e.getTableView().getItems().get(e.getTablePosition().getRow()).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, e.getNewValue()));
		});

		eventsDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		eventsDescription.setCellFactory(descriptionCellFactory);
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

		// delayed setter (set only after the loading of the software is done)
		new Thread(() -> {
			try {
				Thread.sleep(1000);
				root.widthProperty().addListener(e -> {
					resizeColumns();
				});
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}).start();

		logic = new Logic(logger);
		try {
			showAllTasks(logic.loadTasksFromFile());
		} catch (SAXException e) {
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

	public void toggleRightPane() {
		if (rightPane.isVisible()) {
			rightPane.setVisible(false);
			pane.setDividerPosition(0, 1);
			toggleButton.setText("<");
		} else {
			rightPane.setVisible(true);
			pane.setDividerPosition(0, 0.619);
			toggleButton.setText(">");
		}
		resizeColumns();
	}

	public void resizeColumns() {
		final double paneWidth = pane.getWidth();

		final double floatingTableWidth = rightPane.isVisible() ? 304.8 : 0;
		final double dividerPosition = 1.0 - (floatingTableWidth / paneWidth);

		floatingTaskTable.setPrefWidth(floatingTableWidth);
		pane.setDividerPositions(dividerPosition);

		final double eventsTableWidth = paneWidth * dividerPosition - 5; // 5 for padding
		final double idWidth = 30;
		final double checkBoxWidth = 0;
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
	 *            The prompt String, length should be less than FEEDBACK_LENGTH_LIMIT
	 */
	public void setUserPrompt(String prompt) {
		if (prompt == null) {
			return;
		}
		// the length of feedback should not be longer than 100 characters
		assert (prompt.length() <= FEEDBACK_LENGTH_LIMIT);
		logger.fine("Sent back to user: " + prompt);
		userPrompt.setText(prompt);
	}

	public void editDescriptionById(List<Task> taskList, int id) {
		if (taskList.get(id).isStartDateSet()) {
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
	private void editEventDescriptionById(int id) {
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
	private void editFloatingTaskDescriptionById(int id) {
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
		for (TaskModel model : taskModels) {
			if (model.getTaskId() == id + 1)
				return model;
		}
		return null;
	}

	/**
	 * Let the tableView show all the tasks
	 */
	public void showAllTasks(List<Task> taskList) {
		clearInterface();
		retrieveTaskFromStorage(taskList);
		inputBox.requestFocus();
	}

	/**
	 * refresh the tasks shown on UI based on the current storage (by default all tasks are shown)
	 */
	private void retrieveTaskFromStorage(List<Task> taskList) {
		clearInterface();
		for (int i = 0; i < taskList.size(); i++) {
			addToTaskModels(taskList.get(i), i);
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

	private void addToTaskModels(Task task, int i) {
		TaskModel newModel = new TaskModel(task, i + 1, this);
		if (task.isStartDateSet()) {
			eventList.add(newModel);
		} else {
			floatingTaskList.add(newModel);
		}
		taskModels.add(newModel);
	}

	/**
	 * Send a command to the Logic and update the tasks and prompt shown in the UI
	 * 
	 * @param userInput
	 *            The command that is going to be send to the parser
	 */
	public void sendToLogicAndUpdatePrompt(String userInput) {
		logger.fine("Sending to logic" + userInput);
		CommandInfo commandInfo = logic.executeCommand(userInput);

		switch (commandInfo.getCommandType()) {
			case BLANK :
				return;

			case EDIT_DESCRIPTION :
				editDescriptionById(commandInfo.getTaskList(), commandInfo.getTaskToEdit());
				break;

			case FIND :
				displayFoundTask(commandInfo.getTaskList(), commandInfo.getIndexesFound());
				break;

			case HELP :
				showHelp();
				break;

			default :
				showAllTasks(commandInfo.getTaskList());
		}

		setUserPrompt(commandInfo.getFeedback());
		logic.saveTasksToFile();
	}

	private void displayFoundTask(List<Task> taskList, List<Integer> indexesFound) {
		clearInterface();
		for (int index : indexesFound) {
			addToTaskModels(taskList.get(index), index);
		}
		inputBox.requestFocus();
	}

	public void close() {
		if (stage != null) {
			stage.close();
		}
	}

	private void setupLogger() {
		try {
			Handler handler = new FileHandler("logs/log.txt");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
			logger.setLevel(Level.ALL);
		} catch (SecurityException e) {
			//todo: cant log if log setup failed!
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		}
	}

	public void highlightTaskWithId(Task task, int id) {
		int row = getRowFromModel(getTaskModelFromId(id));
		if (task.isStartDateSet()) {
			eventsTable.getSelectionModel().select(row);
			scrollTo(row);
		} else {
			floatingTaskTable.getSelectionModel().select(row);
			scrollTo(row);
		}

	}
}
