package defaultPart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TaskTime extends GregorianCalendar {
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");

	@Override
	public boolean equals(Object taskTime) {
		return get(Calendar.HOUR_OF_DAY) == ((Calendar) taskTime).get(Calendar.HOUR_OF_DAY)
				&& get(Calendar.MINUTE) == ((Calendar) taskTime).get(Calendar.MINUTE);
	}

	@Override
	public String toString() {
		return timeFormat.format(this.getTime());
	}
	
	public void setTimeFromString(String timeString) throws ParseException {
		timeFormat.parse(timeString);
	}
}