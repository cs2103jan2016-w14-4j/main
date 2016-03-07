package defaultPart;

import java.util.Calendar;

public class TaskDate {

	private Calendar _date;
	private Calendar _startTime;
	private Calendar _endTime;

	public Calendar getDate() {
		return _date;
	}

	public void setDate(Calendar date) {
		_date = date;
	}

	public Calendar getStartTime() {
		return _startTime;
	}

	public void setStartTime(Calendar startTime) {
		_startTime = startTime;
	}

	public Calendar getEndTime() {
		return _endTime;
	}

	public void setEndTime(Calendar endTime) {
		_endTime = endTime;
	}

}
