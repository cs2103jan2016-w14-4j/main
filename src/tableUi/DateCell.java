package tableUi;

import defaultPart.Task;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

/**
 * Created by houruomu on 2016/3/16.
 */
public class DateCell extends TableCell<TaskModel, String> {
    private TextField textField;
    public DateCell(){};

    @Override
    public void startEdit() {
        this.requestFocus();
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.focusedProperty().addListener((p,o,n)->{
            if(!n)
                commitEdit(textField.getText());
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
