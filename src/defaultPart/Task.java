package defaultPart;

public class Task {

	private String _description;
	private TaskDate _taskDate;
	private boolean _isCompleted;
	private Recur _recur;

	public void setDescription(String description) {
		_description = description;
	}

	public String getDescription() {
		return _description;
	}

	public TaskDate getTaskDate() {
		return _taskDate;
	}

	public void setTaskDate(TaskDate taskDate) {
		_taskDate = taskDate;
	}

	public boolean isCompleted() {
		return _isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		_isCompleted = isCompleted;
	}

	public Recur getRecur() {
		return _recur;
	}

	public void setRecur(Recur recur) {
		_recur = recur;
	}

	@Override
	public String toString() {
		return "Task [description=" + _description + ", taskDate=" + _taskDate + ", isCompleted="
				+ _isCompleted + ", recur=" + _recur + "]";
	}
}
