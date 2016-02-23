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
	
	// Overload Constructor 1 - Floating Task 
	public Task(String name){
		this.set_name(name);
		this.set_dateAdded(new Date()); // Current date time
		this.set_state(taskState.OPEN);
	}
	
	// Overload Constructor 2 - Deadline Task 
	public Task(String name, Date endDate){
		this.set_name(name);
		this._endDate = endDate;
		this.set_dateAdded(new Date()); // Current date time
		this.set_state(taskState.OPEN);
	}
	
	// Overload Constructor 3 - Event Task 
	public Task(String name, Date startDate, Date endDate){
		this.set_name(name);
		this._startDate = startDate;
		this._endDate = endDate;
		this.set_dateAdded(new Date()); // Current date time
		this.set_state(taskState.OPEN);
	}
	
	private int getID() {
		return ID;
	}
	private void setID(int iD) {
		ID = iD;
	}
	private Date getStartDate() {
		return _startDate;
	}
	private void setStartDate(Date startDate) {
		this._startDate = startDate;
	}
	private Date getEndDate() {
		return _endDate;
	}
	private void setEndDate(Date endDate) {
		this._endDate = endDate;
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

	
	
}
