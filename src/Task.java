import java.util.Date;

public class Task {
	

	private int ID;
	private String _name;
	private Date _startDate;
	private Date _endDate;
	private String _state;
	private Date _dateAdded;
	private String _type;
	
	// Overload Constructor 1 - New Floating Task 
	public Task(String name){
		this.set_name(name);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state("OPEN");
		this.set_type("floating");
	}
	
	// Overload Constructor 2 - New Deadline Task 
	public Task(String name, Date endDate){
		this.set_name(name);
		this.set_endDate(endDate);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state("OPEN");
		this.set_type("deadline");
	}
	
	// Overload Constructor 3 - New Event Task 
	public Task(String name, Date startDate, Date endDate){
		this.set_name(name);
		this.set_startDate(startDate);
		this.set_endDate(endDate);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state("OPEN");
		this.set_type("event");
	}
	
	// Overload Constructor 4 - Import Floating Task 
	public Task(String name, Date dateAdded, String state){
		this.set_name(name);
		this.set_dateAdded(dateAdded);
		this.set_state(state);
		this.set_type("floating");
	}
	
	// Overload Constructor 5 - Import Deadline Task 
	public Task(String name, Date endDate, Date dateAdded, String state){
		this.set_name(name);
		this.set_endDate(endDate);
		this.set_dateAdded(dateAdded);
		this.set_state(state);
		this.set_type("deadline");
	}
	
	// Overload Constructor 6 - Import Event Task 
	public Task(String name, Date startDate, Date endDate, Date dateAdded, String state){
		this.set_name(name);
		this.set_startDate(startDate);
		this.set_endDate(endDate);
		this.set_dateAdded(dateAdded);
		this.set_state(state);
		this.set_type("event");
	}
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public Date getStartDate() {
		return get_startDate();
	}
	public void setStartDate(Date startDate) {
		this.set_startDate(startDate);
	}
	public Date getEndDate() {
		return get_endDate();
	}
	public void setEndDate(Date endDate) {
		this.set_endDate(endDate);
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public Date get_dateAdded() {
		return _dateAdded;
	}

	public void set_dateAdded(Date _dateAdded) {
		this._dateAdded = _dateAdded;
	}

	public Date get_endDate() {
		return _endDate;
	}

	public void set_endDate(Date _endDate) {
		this._endDate = _endDate;
	}

	public Date get_startDate() {
		return _startDate;
	}

	public void set_startDate(Date _startDate) {
		this._startDate = _startDate;
	}
	
	public String get_state() {
		return _state;
	}

	public void set_state(String _state) {
		this._state = _state;
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
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
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
		if (_state == null) {
			if (other._state != null)
				return false;
		} else if (!_state.equals(other._state))
			return false;
		if (_type == null) {
			if (other._type != null)
				return false;
		} else if (!_type.equals(other._type))
			return false;
		return true;
	}

	public String get_type() {
		return _type;
	}

	public void set_type(String _type) {
		this._type = _type;
	}

}
