package defaultPart;

import java.util.Calendar;

public class Recur {
	public enum TimeUnit {
		DAY, WEEK, MONTH, YEAR
	}

	private TimeUnit _unit;
	private int _frequency;
	private Calendar _endDate;

	public TimeUnit getUnit() {
		return _unit;
	}

	public void setUnit(TimeUnit unit) {
		_unit = unit;
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
}
