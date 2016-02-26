import java.util.List;

public class Wuri {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	// Searches the taskList for a specific task ID to delete that task
	public static boolean deleteTask(List<Task> taskList, int taskID) {

		boolean taskFound = false;
		// Searches
		for (Task t : taskList) {
			if (t.getID() == taskID) {
				taskFound = taskList.remove(t);
			}
		}

		return taskFound;
	}

	// Assign all the tasks an ID
	public static void assignID(List<Task> taskList){

		int idCount = 0;
		for (Task t : taskList) {
			t.setID(idCount);
			idCount ++;			
		}
	}
}
