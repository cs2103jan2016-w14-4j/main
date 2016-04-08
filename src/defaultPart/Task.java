package defaultPart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Task {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");

	private String _description;
	private boolean _isCompleted;

	private TaskDate _startDateAndTime;
	private TaskDate _endDateAndTime;
	
	private boolean _isStartDateSet;
	private boolean _isEndDateSet;
	private boolean _isStartTimeSet;
	private boolean _isEndTimeSet;
	
	private int _recurField;
	private int _recurFrequency;

	public String getDescription() {
		return _description;
	}
	
	public void setDescription(String description) {
		_description = description;
	}

	public boolean isCompleted() {
		return _isCompleted;
	}

	public void toggleCompleted() {
		_isCompleted = !_isCompleted;
	}
	
	public String getStartDateString() {
		assert _isStartDateSet;
		return dateFormat.format(_startDateAndTime);
	}
	
	public void setStartDateFromString(String dateString) throws ParseException {
		TaskDate date = new TaskDate();
		date.setTime(dateFormat.parse(dateString));
		setStartDate(date);
	}
	
	public String getEndDateString() {
		assert _isEndDateSet;
		return dateFormat.format(_endDateAndTime);
	}	
	
	
	public String getStartTimeString() {
		assert _isStartTimeSet;
		return timeFormat.format(_startDateAndTime);
	}
	
	public TaskDate getStartDate() {
		return _startDateAndTime;
	}

	public void setStartDate(TaskDate date) {
		setDateOnly(_startDateAndTime, date);
		_isStartDateSet = true;
	}
	
	public void setDateOnly(TaskDate destination, TaskDate source) {
		destination.set(Calendar.YEAR, source.get(Calendar.YEAR));
		destination.set(Calendar.DAY_OF_YEAR, source.get(Calendar.DAY_OF_YEAR));
	}

	public boolean isStartDateSet() {
		return _isStartDateSet;
	}

	public boolean isStartTimeSet() {
		return _isStartTimeSet;
	}

	public boolean isEndTimeSet() {
		return _isEndTimeSet;
	}

	public boolean isEndDateSet() {
		return _isEndDateSet;
	}

	public void setEndDate(TaskDate date) {
		setDateOnly(_endDateAndTime, date);
		_isEndDateSet = true; 
	}
	
	public TaskDate getEndDate() {
		return _endDateAndTime;
	}
	
	public void setStartTime(TaskDate date) {
		setTimeOnly(_startDateAndTime, date);
	}
	
	public void setEndTime(TaskDate date) {
		setTimeOnly(_endDateAndTime, date);
	}
	
	public void setTimeOnly(TaskDate destination, TaskDate source) {
		destination.set(Calendar.MINUTE, source.get(Calendar.MINUTE));
		destination.set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY));
	}
	
	public int getRecurField() {
		return _recurField;
	}

	public void setRecurField(int recurField) {
		_recurField = recurField;
	}

	public int getRecurFrequency() {
		return _recurFrequency;
	}

	public void setRecurFrequency(int recurFrequency) {
		_recurFrequency = recurFrequency;
	}

	public boolean isRecurSet() {
		return _recurFrequency > 0;
	}
	
	public boolean isStartDateAfterEndDate() {
		//todo
		return false;
	}

	public boolean isDateTimeAfterTask(Task task) {
		if (!task.isStartDateSet()) {
			return false;
		}
		if (!this.isStartDateSet()) {
			return true;
		}
		if (this.getStartDate().compareTo(task.getStartDate()) < 0) {
			return false;
		}
		if (this.getStartDate().compareTo(task.getStartDate()) > 0) {
			return true;
		}
		if (!this.isStartTimeSet()) {
			return false;
		}
		if (!task.isStartTimeSet()) {
			return true;
		}
		if (this._startDateAndTime.compareTo(task.getStartDate()) <= 0) {
			return false;
		}
		return true;
	}

//	@Override
//	public String toString() {
//		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//		String formattedDate = "";
//		if (_startDateAndTime != null) {
//			formattedDate = " | " + dateFormat.format(_startDateAndTime.getTime());
//		}
//		String timeString = "";
//		if (_startTime != null) {
//			SimpleDateFormat timeFormat = new SimpleDateFormat("h.mma");
//			timeString = " | " + timeFormat.format(_startTime.getTime());
//			if (_endTime != null) {
//				timeString += "-" + timeFormat.format(_endTime.getTime());
//			}
//		}
//		String recurString = "";
//		if (_recur != null) {
//			recurString = " | recur=" + _recur;
//		}
//		return _description + formattedDate + timeString + recurString;
//	}
	
	
	
	public boolean willRecur() {
		return (this.getNextRecur() != null);
	}

	public TaskDate getNextRecur() {
		if (!isStartDateSet() || !isRecurSet()) {
			return null;
		}

		TaskDate nextDate = (TaskDate) getStartDate().clone();

		incrementNextDate(nextDate);

		if (nextDateAfterEndDate(nextDate)) {
			return null;
		}
		return nextDate;
	}

	private void incrementNextDate(TaskDate nextDate) {
		nextDate.add(_recurField, _recurFrequency);
	}

	private boolean nextDateAfterEndDate(TaskDate nextDate) {
		return _endDateAndTime != null && nextDate.compareTo(_endDateAndTime) > 0;
	}

	private TaskDate getNextRecurAfterToday() {
		TaskDate nextDate = (TaskDate) getStartDate().clone();
		TaskDate today = initializeToday();

		/*
		 * if want to always recur at least 1ce, add: if(!nextDateBeforeToday(nextDate,today)){
		 * incrementNextDate(nextDate); }
		 */
		while (nextDateBeforeToday(nextDate, today)) {
			incrementNextDate(nextDate);
		}

		if (nextDateAfterEndDate(nextDate)) {
			return null;
		}
		return nextDate;
	}

	private boolean nextDateBeforeToday(TaskDate nextDate, TaskDate today) {
		return nextDate.compareTo(today) < 0;
	}

	private TaskDate initializeToday() {
		// initialize "today" to 00:00am of tomorrow
		TaskDate today = new TaskDate();
		today.set(TaskDate.DATE, today.get(TaskDate.DATE) + 1);
		today.set(TaskDate.HOUR_OF_DAY, 0);
		today.set(TaskDate.MINUTE, 0);
		today.set(TaskDate.SECOND, 0);
		today.set(TaskDate.MILLISECOND, 0);
		today.getTimeInMillis();
		return today;
	}
}
