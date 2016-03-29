package defaultPart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TaskDate extends GregorianCalendar {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");

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
}
