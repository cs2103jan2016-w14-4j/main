package defaultPart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Recur {

	public enum TimeUnit {
		DAY, WEEK, MONTH, YEAR
	}

	private TimeUnit _timeUnit;
	private int _frequency = 1;
	private Calendar _endDate;
	private Calendar _startDate;

	public Recur() {

	}

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
		if (this.getNextRecur() != null) {
			return true;
		}
		return false;
	}

	public Calendar getNextRecur() {
		// todo
		Calendar nextDate = (GregorianCalendar) getStartDate().clone();
		Calendar today = new GregorianCalendar();
		// need find some way to handle strictly after today
		// consider initialize "today" to 00:00am of tomorrow?
		while (nextDate.compareTo(today) < 0) {
			switch (_timeUnit) {
				case DAY :
					nextDate.set(Calendar.DATE, nextDate.get(Calendar.DATE) + this._frequency);
					break;
				case WEEK :
					nextDate.set(Calendar.DATE, nextDate.get(Calendar.DATE) + this._frequency * 7);
					break;
				case MONTH :
					nextDate.set(Calendar.MONTH, nextDate.get(Calendar.MONTH) + this._frequency);
					break;
				case YEAR :
					nextDate.set(Calendar.YEAR, nextDate.get(Calendar.YEAR) + this._frequency);
					break;
			}
			nextDate.getTimeInMillis();
		}

		if (nextDate.compareTo(_endDate) > 0) {
			return null;
		}
		return nextDate;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = dateFormat.format(_endDate.getTime());
		return "Recur [timeUnit=" + _timeUnit + ", frequency=" + _frequency + ", endDate=" + formattedDate
				+ "]";
	}

	public Calendar getStartDate() {
		return _startDate;
	}

	public void setStartDate(Calendar startDate) {
		_startDate = startDate;
	}
}
