import java.util.GregorianCalendar;

public class TaskDate extends GregorianCalendar {

	private boolean _isStartDateSet;
	private boolean _isEndDateSet;

	private boolean _isStartTimeSet;
	private boolean _isEndTimeSet;

	public boolean isStartDateSet() {
		return _isStartDateSet;
	}

	public void setStartDateSet(boolean isStartDateSet) {
		_isStartDateSet = isStartDateSet;
	}

	public boolean isEndDateSet() {
		return _isEndDateSet;
	}

	public void setEndDateSet(boolean isEndDateSet) {
		_isEndDateSet = isEndDateSet;
	}

	public boolean isStartTimeSet() {
		return _isStartTimeSet;
	}

	public void setStartTimeSet(boolean isStartTimeSet) {
		_isStartTimeSet = isStartTimeSet;
	}

	public boolean isEndTimeSet() {
		return _isEndTimeSet;
	}

	public void setEndTimeSet(boolean isEndTimeSet) {
		_isEndTimeSet = isEndTimeSet;
	}

}
