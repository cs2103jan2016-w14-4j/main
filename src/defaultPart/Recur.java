package defaultPart;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Recur {

	public enum TimeUnit {
		DAY, WEEK, MONTH, YEAR
	}

	private TimeUnit _timeUnit;
	private int _frequency = 1;
	private Calendar _endDate;

	public TimeUnit getTimeUnit() {
		return _timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		_timeUnit = timeUnit;
	}

	public int getFrequency() {
		return _frequency;
	}

	public void setFrequency(int frequency) {
		_frequency = frequency;
	}

	public Calendar getEndDate() {
		return _endDate;
	}

	public void setEndDate(Calendar endDate) {
		_endDate = endDate;
	}

	public boolean willRecur() {
		// todo
		return false;
	}

	public TaskDate getNextRecur() {
		// todo
		return null;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = dateFormat.format(_endDate.getTime());
		return "Recur [timeUnit=" + _timeUnit + ", frequency=" + _frequency + ", endDate=" + formattedDate
				+ "]";
	}
}
