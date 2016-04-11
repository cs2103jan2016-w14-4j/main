package defaultPart;

//@@author A0135766W
public class InputIndexOutOfBoundsException extends Exception {
	private int _index;

	public InputIndexOutOfBoundsException(int index) {
		_index = index;
	}

	public int getIndex() {
		return _index;
	}
}
