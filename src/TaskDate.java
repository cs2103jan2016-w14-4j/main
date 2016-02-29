import java.util.GregorianCalendar;

public class TaskDate extends GregorianCalendar {

	private boolean _dateSet;
	private boolean _timeSet;
	
	public boolean isDateSet() {
		return _dateSet;
	}

	public void setIsDateSet(boolean isDateSet) {
		_dateSet = isDateSet;
	}

	public boolean isTimeSet() {
		return _timeSet;
	}

	public void setTimeSet(boolean isTimeSet) {
		_timeSet = isTimeSet;
	}

}
