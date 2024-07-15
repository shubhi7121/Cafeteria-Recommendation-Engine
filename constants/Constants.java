package constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Constants {
	// Port and host
	public static final String HOST = "localhost";
	public static final int PORT = 12345;
	
	// Database connection constants
	public static final String JDBC_URL = "jdbc:mysql://localhost:3306/recommendationengine";
	public static final String JDBC_USER = "root";
	public static final String JDBC_PASSWORD = "root";
	public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	// Authentication constants
	public static final int MAX_ATTEMPTS = 3;
	public static final String AUTHENTICATION_SUCCESS = "authenticated";
	public static final String AUTHENTICATION_FAILED = "message";
	public static final String ROLE = "role";
	public static final String UNKNOWN_ROLE = "Unknown";
	public static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed. Attempts left: ";
	public static final String MAX_ATTEMPTS_EXCEEDED_MESSAGE = "Maximum authentication attempts reached. User blocked.";

	// SQL queries for user activity
	public static final String INSERT_LOGIN_INFO = "INSERT INTO user_sessions (email_id, login_time) VALUES (?, NOW())";
	public static final String UPDATE_LOGOUT_INFO = "UPDATE user_sessions SET logout_time = NOW() WHERE email_id = ? AND logout_time IS NULL";

	// Role constants
	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_CHEF = "chef";
	public static final String ROLE_EMPLOYEE = "employee";

	// Scheduler Constants
    public static final String MOVE_ITEMS_QUERY = "INSERT INTO discard_items (menu_item_id, discarded_date) "
            + "SELECT menu_item_id, CURDATE() "
            + "FROM meal_item "
            + "WHERE rating < 2 AND sentiment_score < 2";
    public static final int SCHEDULER_INITIAL_DELAY = 0;
    public static final int SCHEDULER_PERIOD = 30;
    public static final int SHUTDOWN_TIMEOUT = 30;

	// SQL queries for admin
	public static final String INSERT_MENU_ITEM = "INSERT INTO meal_item (name, price, type_id, availability_Status, foodCategory, spiceLevel, foodTypes, sweettooth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String SELECT_MENU_ITEM_ID_BY_NAME = "SELECT menu_item_id FROM meal_item WHERE name = ?";
	public static final String INSERT_NOTIFICATION = "INSERT INTO notification (name, created_by, item_id) VALUES (?, ?, ?)";
	public static final String UPDATE_MENU_ITEM = "UPDATE meal_item SET price = ?, availability_status = ? WHERE name = ?";
	public static final String DELETE_MENU_ITEM = "DELETE FROM meal_item WHERE name = ?";
	public static final String SHOW_MENU = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price, mi.availability_status FROM meal_item mi JOIN meal_type mt ON mi.type_id = mt.type_id ORDER BY mi.menu_item_id";
	public static final String LOGIN_ACTIVITY = "SELECT email_id, login_time, logout_time FROM user_sessions";

	//DiscardMenu
	public static final String CHECK_DISCARD_LIST = "SELECT COUNT(*) AS count FROM discard_items WHERE discarded_date <= CURDATE()";
	public static final String DISPLAY_DISCARD_LIST = "SELECT d.menu_item_id, m.name, mt.type_name, m.price, m.rating, m.sentiments FROM discard_items d JOIN meal_item m ON d.menu_item_id = m.menu_item_id JOIN meal_type mt ON m.type_id = mt.type_id ";
	public static final String SELECT_MENU_ITEM_NAME_BY_ID = "SELECT name FROM meal_item WHERE menu_item_id = ?";
	public static final String DELETE_MENU_ITEM_BY_ID = "DELETE FROM meal_item WHERE menu_item_id = ?";

	// Meal type constants
	public static final String[] MEAL_TYPES = { "breakfast", "lunch", "dinner" };

	// Command constants
	public static final String EXIT = "exit";
	public static final String END_OF_MENU = "END_OF_MENU";
	public static final String END_OF_OPTIONS = "END_OF_OPTIONS";
}
