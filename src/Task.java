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

	
	
}
