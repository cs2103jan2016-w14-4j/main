package defaultPart;

public class Recur {

	public enum TimeUnit {
		DAY, WEEK, MONTH, YEAR
	}

	private TimeUnit _timeUnit;
	private int _frequency = 1;
	private TaskDate _endDate;
	private TaskDate _startDate;

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

	public TaskDate getEndDate() {
		return _endDate;
	}

	public void setEndDate(TaskDate endDate) {
		_endDate = endDate;
	}

	public boolean willRecur() {
		// todo
		if (this.getNextRecur() != null) {
			return true;
		}
		return false;
	}

	public TaskDate getNextRecur() {
		TaskDate nextDate = (TaskDate) getStartDate().clone();
		TaskDate today = initializeToday();
		getNextRecurAfterToday(nextDate, today);

		if (nextDateAfterEndDate(nextDate)) {
			return null;
		}
		return nextDate;
	}

	private boolean nextDateAfterEndDate(TaskDate nextDate) {
		return nextDate.compareTo(_endDate) > 0;
	}

	private void getNextRecurAfterToday(TaskDate nextDate, TaskDate today) {
		while (nextDateBeforeToday(nextDate, today)) {
			switch (_timeUnit) {
				case DAY :
					nextDate.set(TaskDate.DATE, nextDate.get(TaskDate.DATE) + this._frequency);
					break;
				case WEEK :
					nextDate.set(TaskDate.DATE, nextDate.get(TaskDate.DATE) + this._frequency * 7);
					break;
				case MONTH :
					nextDate.set(TaskDate.MONTH, nextDate.get(TaskDate.MONTH) + this._frequency);
					break;
				case YEAR :
					nextDate.set(TaskDate.YEAR, nextDate.get(TaskDate.YEAR) + this._frequency);
					break;
			}
			nextDate.getTimeInMillis();
		}
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

	@Override
	public String toString() {
		String formattedDate = _endDate.toString();
		return _frequency + "" + _timeUnit.name().toLowerCase().charAt(0) + " " + formattedDate;
	}

	public TaskDate getStartDate() {
		return _startDate;
	}

	public void setStartDate(TaskDate startDate) {
		_startDate = startDate;
	}
}
