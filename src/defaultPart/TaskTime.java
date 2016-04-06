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

	@Override
	public int compareTo(Calendar taskTime) {
		int[] units = { Calendar.HOUR_OF_DAY, Calendar.MINUTE };
		for (int unit : units) {
			if (this.get(unit) < taskTime.get(unit)) {
				return -1;
			}
			if (this.get(unit) > taskTime.get(unit)) {
				return 1;
			}
		}
		return 0;
	}
	
	public void parse(String timeString) throws ParseException {
		this.setTime(timeFormat.parse(timeString));
	}
}