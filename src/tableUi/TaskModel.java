package tableUi;

import defaultPart.Task;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by houruomu on 2016/3/12.
 */
public class TaskModel {
    private IntegerProperty taskId;
    private StringProperty taskDescription;
    private BooleanProperty isEvent;
    private StringProperty dateTime;
    private BooleanProperty isRecur;
    private StringProperty recur;
    private BooleanProperty isComplete;

    private Task task;

    public TaskModel(Task task, int id){
        this.task = task;
        taskId.setValue(id);
        taskDescription.setValue(task.getDescription());
        isComplete.setValue(task.isCompleted());

        if(task.getDate() != null){
            isEvent.setValue(true);
            dateTime.set(task.getEndTime().toString());
        }

        if(task.getRecur() != null && task.getRecur().willRecur()){
            isRecur.setValue(true);
            recur.setValue(task.getRecur().toString());
        }
    }

    public IntegerProperty taskId(){
        return taskId;
    }

    public StringProperty taskDescription(){
        return taskDescription;
    }

    public StringProperty dateTime(){
        return dateTime;
    }

    public StringProperty recur(){
        return recur;
    }

    public int getTaskId() {
        return taskId.get();
    }

    public IntegerProperty taskIdProperty() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId.set(taskId);
    }

    public String getTaskDescription() {
        return taskDescription.get();
    }

    public StringProperty taskDescriptionProperty() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription.set(taskDescription);
    }

    public boolean getIsEvent() {
        return isEvent.get();
    }

    public BooleanProperty isEventProperty() {
        return isEvent;
    }

    public void setIsEvent(boolean isEvent) {
        this.isEvent.set(isEvent);
    }

    public String getDateTime() {
        return dateTime.get();
    }

    public StringProperty dateTimeProperty() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime.set(dateTime);
    }

    public boolean getIsRecur() {
        return isRecur.get();
    }

    public BooleanProperty isRecurProperty() {
        return isRecur;
    }

    public void setIsRecur(boolean isRecur) {
        this.isRecur.set(isRecur);
    }

    public String getRecur() {
        return recur.get();
    }

    public StringProperty recurProperty() {
        return recur;
    }

    public void setRecur(String recur) {
        this.recur.set(recur);
    }

    public boolean getIsComplete() {
        return isComplete.get();
    }

    public BooleanProperty isCompleteProperty() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete.set(isComplete);
    }
}
