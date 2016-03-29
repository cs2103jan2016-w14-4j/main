package defaultPart;

import java.text.SimpleDateFormat;

public class Task {

	private String _description;

	private TaskDate _date;
	private TaskTime _startTime;
	private TaskTime _endTime;

	private Recur _recur;
	private boolean _isCompleted;

	public void setDescription(String description) {
		_description = description;
	}

	public TaskDate getDate() {
		return _date;
	}

	public void setDate(TaskDate date) {
		_date = date;
	}

	public TaskTime getStartTime() {
		return _startTime;
	}

	public void setStartTime(TaskTime startTime) {
		_startTime = startTime;
	}

	public TaskTime getEndTime() {
		return _endTime;
	}

	public void setEndTime(TaskTime endTime) {
		_endTime = endTime;
	}

	public String getDescription() {
		return _description;
	}

	public Recur getRecur() {
		return _recur;
	}

	public void setRecur(Recur recur) {
		_recur = recur;
	}

	public boolean isCompleted() {
		return _isCompleted;
	}

	public void toggleCompleted() {
		_isCompleted = !_isCompleted;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = "";
		if (_date != null) {
			formattedDate = " | " + dateFormat.format(_date.getTime());
		}
		String timeString = "";
		if (_startTime != null) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("h.mma");
			timeString = " | " + timeFormat.format(_startTime.getTime());
			if (_endTime != null) {
				timeString += "-" + timeFormat.format(_endTime.getTime());
			}
		}
		String recurString = "";
		if (_recur != null) {
			recurString = " | recur=" + _recur;
		}
		return _description + formattedDate + timeString + recurString;
	}
}
