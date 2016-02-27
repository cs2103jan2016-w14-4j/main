import java.util.LinkedList;
import java.util.List;

public class Task {

	private static List<Task> _taskList = new LinkedList<Task>();
	
	private int _index;
	private String _description;
	private TaskDate _startDate;
	private TaskDate _endDate;
	private boolean _isCompleted;
	private Recur _recur;

	public static void add(Task task) {
		_taskList.add(task);
	}
	
	public static void remove(int index) {
		_taskList.remove(index);
	}
	
	public static Task get(int index) {
		return _taskList.get(index);
	}
	
	public Task(String description) {
		_description = description;
	}

	public int getIndex() {
		return _index;
	}

	public void setIndex(int index) {
		_index = index;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		if (_description != null) {
			_description = description;
		}
	}

	public TaskDate getStartDate() {
		return _startDate;
	}

	public void setStartDate(TaskDate startDate) {
		_startDate = startDate;
	}

	public TaskDate getEndDate() {
		return _endDate;
	}

	public void setEndDate(TaskDate endDate) {
		_endDate = endDate;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _index;
		result = prime * result + ((_endDate == null) ? 0 : _endDate.hashCode());
		result = prime * result + ((_description == null) ? 0 : _description.hashCode());
		result = prime * result + ((_startDate == null) ? 0 : _startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (_index != other._index)
			return false;
		if (_endDate == null) {
			if (other._endDate != null)
				return false;
		} else if (!_endDate.equals(other._endDate))
			return false;
		if (_description == null) {
			if (other._description != null)
				return false;
		} else if (!_description.equals(other._description))
			return false;
		if (_startDate == null) {
			if (other._startDate != null)
				return false;
		} else if (!_startDate.equals(other._startDate))
			return false;
		return true;
	}

	/* returns a String formatted nicely to be displayed on UI */
	@Override
	public String toString() {
		// todo
		return null;
	}
}
