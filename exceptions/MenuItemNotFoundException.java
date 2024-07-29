package exceptions;

public class MenuItemNotFoundException extends Exception {
	public MenuItemNotFoundException(String message) {
		super(message);
	}
}