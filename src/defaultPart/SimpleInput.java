package defaultPart;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;


import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class SimpleInput extends Application {
	ObservableList<String> items = null;
	ArrayList<String> undoBuffer = new ArrayList<String>();
	ArrayList<String> redoBuffer = new ArrayList<String>();
	boolean undoing = false;
	boolean redoing = false;
	Text prompt;

	class Delta {
		double x, y;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		VBox vBox = new VBox(0);
		Scene scene = new Scene(vBox, 400, 500);
		scene.getStylesheets().add(SimpleInput.class.getResource("simpleInput.css").toExternalForm());
		vBox.setAlignment(Pos.TOP_CENTER);

		Text title = new Text("Wuri");
		title.setId("titletxt");

		vBox.getChildren().add(title);

		ListView<String> list = new ListView<String>();
		items = FXCollections.observableArrayList("1.\thello", "2.\tworld");
		list.setItems(items);
		list.setEditable(false);
		list.setPrefHeight(800);

		primaryStage.setTitle("Wuri");
		primaryStage.setScene(scene);

		vBox.getChildren().add(list);

		TextField input = new TextField();
		vBox.getChildren().add(input);
		input.requestFocus();

		prompt = new Text("Enter something to start!");
		vBox.getChildren().add(prompt);
		prompt.setId("prompt");

		input.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(javafx.scene.input.KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getCode().equals(KeyCode.ENTER)) {
					try {
						processInput(input.getText());
					} catch (Exception e) {
						prompt.setText("invalid input!");
					}
					input.clear();
				}
			}
		});

		list.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// TODO Auto-generated method stub
				if (newValue) {
					input.requestFocus();
				}
			}
		});

		prompt.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// TODO Auto-generated method stub
				if (newValue) {
					input.requestFocus();
				}
			}
		});

		final Delta dragDelta = new Delta();
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragDelta.x = primaryStage.getX() - mouseEvent.getScreenX();
				dragDelta.y = primaryStage.getY() - mouseEvent.getScreenY();
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				primaryStage.setX(mouseEvent.getScreenX() + dragDelta.x);
				primaryStage.setY(mouseEvent.getScreenY() + dragDelta.y);
			}
		});

		final KeyCombination undoShortKey = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
		final KeyCombination redoShortKey = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
		scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// TODO Auto-generated method stub
				if (undoShortKey.match(event)) {
					processInput("undo");
				} else if (redoShortKey.match(event)) {
					processInput("redo");
				}
			}
		});

		primaryStage.setResizable(false);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.show();

	}

	private void processInput(String input) {
		if (input.toLowerCase().equals("exit") || input.toLowerCase().equals("quit")) {
			System.exit(0);
		}

		String[] param = input.trim().split(" ");
		String command = param[0].toLowerCase();
		String restString;
		if (param.length > 1) {
			restString = input.substring(input.trim().indexOf(" ")).trim();
		} else {
			restString = "";
		}

		if (command.equals("delete") || command.equals("remove")) {
			if (param.length == 1) {
				String removed = items.remove(items.size() - 1);
				removed = removed.substring(removed.indexOf('\t') + 1);
				undoBuffer.add("add " + removed);
				prompt.setText("Successfully removed: " + removed);
			} else {
				int index = Integer.parseInt(param[1]) - 1;
				String removed = items.remove(index);
				removed = removed.substring(removed.indexOf('\t') + 1);
				reorder();
				undoBuffer.add("addp " + (index + 1) + " " + removed);
				prompt.setText("Successfully removed: " + removed);
			}
		} else if (command.equals("update")) {
			int index = Integer.parseInt(param[1]) - 1;
			String target = restString.substring(restString.trim().indexOf(" ")).trim();
			String removed = items.remove(index);
			removed = removed.substring(removed.indexOf('\t') + 1);
			items.add(index, (index + 1) + ".\t" + target);
			undoBuffer.add("update " + (index + 1) + " " + removed);
			prompt.setText("Successfully updated task " + (index + 1));
		} else if (command.equals("add") || command.equals("-a")) {
			items.add(items.size() + 1 + ".\t" + restString);
			undoBuffer.add("remove");
			prompt.setText("New task added");
		} else if (command.equals("addp")) {
			int index = Integer.parseInt(param[1]) - 1;
			String target = restString.substring(restString.trim().indexOf(" ")).trim();
			items.add(index, (index + 1) + ".\t" + target);
			reorder();
			undoBuffer.add("remove " + (index + 1));
			prompt.setText("New task added");
		} else if (command.equals("undo")) {
			undoing = true;
			if (undoBuffer.size() == 0) {
				prompt.setText("No history found");
			} else {
				processInput(undoBuffer.remove(undoBuffer.size() - 1));
				prompt.setText("Undo Successful");
			}
			undoing = false;
		} else if (command.equals("redo")) {
			redoing = true;
			if (redoBuffer.isEmpty()) {
				prompt.setText("No Redo history found");
			} else {
				processInput(redoBuffer.remove(redoBuffer.size() - 1));
			}
			redoing = false;
		} else {
			items.add(items.size() + 1 + ".\t" + input);
			undoBuffer.add("remove");
		}

		if (undoing) {
			redoBuffer.add(undoBuffer.remove(undoBuffer.size() - 1));
		} else if (!redoing && !command.equals("undo") && !command.equals("redo")) {
			redoBuffer.clear();
		}
	}

	private void reorder() {
		for (int i = 0; i < items.size(); i++) {
			String newS = items.remove(i);
			newS = (i + 1) + newS.substring(newS.indexOf('.'));
			items.add(i, newS);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

}
