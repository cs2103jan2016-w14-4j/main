package defaultPart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Task {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");

	private String _description;
	private boolean _isCompleted = false;

	private TaskDate _startDateAndTime = new TaskDate();
	private TaskDate _endDateAndTime = new TaskDate();
	
	private boolean _isStartDateSet = false;
	private boolean _isEndDateSet = false;
	private boolean _isStartTimeSet = false;
	private boolean _isEndTimeSet = false;
	
	private int _recurField;
	private int _recurFrequency = 0;

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
	
	public String getFormattedStartDate() {
		assert _isStartDateSet;
		return dateFormat.format(_startDateAndTime.getTime());
	}
	
	public String getFormattedEndDate() {
		assert _isEndDateSet;
		return dateFormat.format(_endDateAndTime.getTime());
	}	
		
	public String getFormattedStartTime() {
		assert _isStartTimeSet;
		return timeFormat.format(_startDateAndTime.getTime());
	}
	
	public String getFormattedEndTime() {
		assert _isEndTimeSet;
		return timeFormat.format(_endDateAndTime.getTime());
	}
	
	public void setStartDateFromFormattedString(String dateString) throws ParseException {
		setStartDate(getDateFromFormattedString(dateString));
	}	
	
	public void setEndDateFromFormattedString(String dateString) throws ParseException {
		setEndDate(getDateFromFormattedString(dateString));
	}	
	
	public TaskDate getDateFromFormattedString(String dateString) throws ParseException {
		return getDateOrTimeFromFormattedString(dateString, dateFormat);
	}
	
	public void setStartTimeFromFormattedString(String timeString) throws ParseException {
		setStartTime(getTimeFromFormattedString(timeString));
	}	
	
	public void setEndTimeFromFormattedString(String timeString) throws ParseException {
		setEndTime(getTimeFromFormattedString(timeString));
	}		
	
	public TaskDate getTimeFromFormattedString(String timeString) throws ParseException {
		return getDateOrTimeFromFormattedString(timeString, timeFormat);
	}

	private TaskDate getDateOrTimeFromFormattedString(String timeString, SimpleDateFormat format) throws ParseException {
		TaskDate date = new TaskDate();
		date.setTime(format.parse(timeString));
		return date;
	}
	
	public TaskDate getStartDate() {
		return _startDateAndTime;
	}

	public void setStartDate(TaskDate date) {
		setDateOnly(_startDateAndTime, date);
		_isStartDateSet = true;
	}
	
	private void setDateOnly(TaskDate destination, TaskDate source) {
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
		_isStartTimeSet = true;
	}
	
	public void setEndTime(TaskDate date) {
		setTimeOnly(_endDateAndTime, date);
		_isEndTimeSet = true;
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

	@Override
	public String toString() {
		return _description + " " + getDateTimeString() + getRecurString();
	}

	public String getDateTimeString() {
		String dateTimeString = "";
		if (isStartDateSet()) {
			dateTimeString += getFormattedStartDate() + " ";
			
			if (isStartTimeSet()) {
				dateTimeString += getFormattedStartTime() + " ";
			}
	
			if (isEndDateSet()) {
				dateTimeString += getFormattedEndDate() + " ";
			}
			
			if (isEndTimeSet()) {
				dateTimeString += getFormattedEndTime() + " ";
			}
		}
		return dateTimeString;
	}
	
	public String getRecurString() {
		String recurString = "";
		if (isRecurSet()) {
			recurString += _recurFrequency;
			//use hashmap maybe?
    		switch (_recurField) {
    			case TaskDate.DAY_OF_YEAR :
    				recurString += "d";
    				break;
    
    			case TaskDate.WEEK_OF_YEAR :
    				recurString += "w";
    				break;
    
    			case TaskDate.MONTH :
    				recurString += "m";
    				break;
    
    			case TaskDate.YEAR :
    				recurString += "y";
    				break;
    		}
    		assert recurString != "";
		}
		return recurString;
	}
	
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
