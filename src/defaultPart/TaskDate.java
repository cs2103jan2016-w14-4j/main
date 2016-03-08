package defaultPart;

import java.text.SimpleDateFormat;
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

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = dateFormat.format(_date.getTime());
		String timeString = "";
		if (_startTime != null) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("K.mma");
			timeString = dateFormat.format(_startTime.getTime());
			if (_endTime != null) {
				timeString += "-" + dateFormat.format(_endTime.getTime());
			}
		}
		return "[" + formattedDate + timeString + "]";
	}

}
