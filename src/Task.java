import java.util.Date;

public class Task {
	
	public enum taskState {
		OPEN, COMPLETED, DELETED
	};

	private int ID;
	private String _name;
	private Date _startDate;
	private Date _endDate;
	private taskState _state;
	private Date _dateAdded;
	
	// Overload Constructor 1 - New Floating Task 
	public Task(String name){
		this.set_name(name);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state(taskState.OPEN);
	}
	
	// Overload Constructor 2 - New Deadline Task 
	public Task(String name, Date endDate){
		this.set_name(name);
		this.set_endDate(endDate);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state(taskState.OPEN);
	}
	
	// Overload Constructor 3 - New Event Task 
	public Task(String name, Date startDate, Date endDate){
		this.set_name(name);
		this.set_startDate(startDate);
		this.set_endDate(endDate);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state(taskState.OPEN);
	}
	
	// Overload Constructor 4 - Import Floating Task 
	public Task(String name, Date dateAdded, String state){
		this.set_name(name);
		this.set_dateAdded(dateAdded);
		this.set_state(_state);
	}
	
	// Overload Constructor 5 - Import Deadline Task 
	public Task(String name, Date endDate, Date dateAdded, String state){
		this.set_name(name);
		this.set_endDate(endDate);
		this.set_dateAdded(dateAdded);
		this.set_state(_state);
	}
	
	// Overload Constructor 6 - Import Event Task 
	public Task(String name, Date startDate, Date endDate, Date dateAdded, String state){
		this.set_name(name);
		this.set_startDate(startDate);
		this.set_endDate(endDate);
		this.set_dateAdded(new Date());
		this.set_state(taskState.OPEN);
	}
	
	private int getID() {
		return ID;
	}
	private void setID(int iD) {
		ID = iD;
	}
	private Date getStartDate() {
		return get_startDate();
	}
	private void setStartDate(Date startDate) {
		this.set_startDate(startDate);
	}
	private Date getEndDate() {
		return get_endDate();
	}
	private void setEndDate(Date endDate) {
		this.set_endDate(endDate);
	}

	private String get_name() {
		return _name;
	}

	private void set_name(String _name) {
		this._name = _name;
	}

	private taskState get_state() {
		return _state;
	}

	private void set_state(taskState _state) {
		this._state = _state;
	}

	private Date get_dateAdded() {
		return _dateAdded;
	}

	private void set_dateAdded(Date _dateAdded) {
		this._dateAdded = _dateAdded;
	}

	private Date get_endDate() {
		return _endDate;
	}

	private void set_endDate(Date _endDate) {
		this._endDate = _endDate;
	}

	private Date get_startDate() {
		return _startDate;
	}

	private void set_startDate(Date _startDate) {
		this._startDate = _startDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + ((_dateAdded == null) ? 0 : _dateAdded.hashCode());
		result = prime * result + ((_endDate == null) ? 0 : _endDate.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_startDate == null) ? 0 : _startDate.hashCode());
		result = prime * result + ((_state == null) ? 0 : _state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (ID != other.ID)
			return false;
		if (_dateAdded == null) {
			if (other._dateAdded != null)
				return false;
		} else if (!_dateAdded.equals(other._dateAdded))
			return false;
		if (_endDate == null) {
			if (other._endDate != null)
				return false;
		} else if (!_endDate.equals(other._endDate))
			return false;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (_startDate == null) {
			if (other._startDate != null)
				return false;
		} else if (!_startDate.equals(other._startDate))
			return false;
		if (_state != other._state)
			return false;
		return true;
	}
	
	
}
