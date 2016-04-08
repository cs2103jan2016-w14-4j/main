package defaultPart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TaskDate extends GregorianCalendar {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mma");

	@Override
	public boolean equals(Object taskDate) {
		return get(Calendar.DAY_OF_MONTH) == ((Calendar) taskDate).get(Calendar.DAY_OF_MONTH)
				&& get(Calendar.MONTH) == ((Calendar) taskDate).get(Calendar.MONTH)
				&& get(Calendar.YEAR) == ((Calendar) taskDate).get(Calendar.YEAR);
	}

	@Override
	public String toString() {
		return dateFormat.format(this.getTime());
	}

	@Override
	public int compareTo(Calendar taskDate) {
		int[] units = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH };
		for (int unit : units) {
			if (get(unit) < taskDate.get(unit)) {
				return -1;
			}
			if (get(unit) > taskDate.get(unit)) {
				return 1;
			}
		}
		return 0;
	}

	public void setDateFromString(String dateString) throws ParseException {
		setTime(dateFormat.parse(dateString));
	}
}
