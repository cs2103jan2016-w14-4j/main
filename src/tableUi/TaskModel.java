package tableUi;

import defaultPart.Task;
import javafx.beans.property.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by houruomu on 2016/3/12.
 */
public class TaskModel {
    private final SimpleIntegerProperty taskId;
    private final SimpleStringProperty taskDescription;
    private final SimpleBooleanProperty isEvent;
    private final SimpleStringProperty dateTime;
    private final SimpleBooleanProperty isRecur;
    private final SimpleStringProperty recur;
    private final SimpleBooleanProperty isComplete;

    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd.MM");

    private Task task;

    public Task getTask(){
        return task;
    }

    public TaskModel(Task task, int id){
        this.task = task;
        taskId = new SimpleIntegerProperty(id);
        taskDescription = new SimpleStringProperty(task.getDescription());
        isComplete = new SimpleBooleanProperty(task.isCompleted());

        if(task.getDate() != null){
            isEvent = new SimpleBooleanProperty(true);
            dateTime = new SimpleStringProperty(DATE_FORMAT.format(task.getDate().getTime()));
        }else{
            isEvent = new SimpleBooleanProperty(false);
            dateTime = new SimpleStringProperty("");
        }

        if(task.getRecur() != null && task.getRecur().willRecur()){
            isRecur = new SimpleBooleanProperty(true);
            recur = new SimpleStringProperty(task.getRecur().toString());
        }else{
            isRecur = new SimpleBooleanProperty(false);
            recur = new SimpleStringProperty("");
        }
    }

    public void update(){
        taskDescription.setValue(task.getDescription());
        isComplete.setValue(task.isCompleted());
        if(task.getDate() != null){
            isEvent.setValue(true);
            dateTime.setValue(DATE_FORMAT.format(task.getDate().getTime()));
        }else{
            isEvent.setValue(false);
            dateTime.setValue("");
        }

        if(task.getRecur() != null && task.getRecur().willRecur()){
            isRecur.setValue(true);
            recur.setValue(task.getRecur().toString());
        }else{
            isRecur.setValue(false);
            recur.setValue("");
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
