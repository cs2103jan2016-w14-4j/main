package defaultPart;

public class InputIndexOutOfBoundsException extends Exception {
	private int _index;
	public InputIndexOutOfBoundsException(int index) {
		_index = index;
	}
	public int getIndex() {
		return _index;
	}
}
