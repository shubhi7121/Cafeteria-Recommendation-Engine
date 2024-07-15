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

	// SQL Queries for employee
    public static final String CHECK_NOTIFICATIONS_TODAY = "SELECT COUNT(*) AS count FROM notifications WHERE DATE(notification_date) = CURDATE() AND username = ?";
    public static final String DISPLAY_NOTIFICATIONS_TODAY = "SELECT id, notification_name FROM notifications WHERE DATE(notification_date) = CURDATE() AND username = ?";
    public static final String NOTIFICATION_DETAILS_QUERY = "SELECT notification_name, item_name, price, availability_status FROM notifications WHERE id = ?";
    public static final String FETCH_MENU_ITEMS_QUERY = "SELECT meal_item_id, meal_item_name, rating, sentiment_score, sentiments FROM menu_items WHERE meal_type = ?";
    public static final String CHECK_VOTE_QUERY = "SELECT 1 FROM votes WHERE username = ? AND item_name = ? AND DATE(vote_date) = CURDATE()";
    public static final String INSERT_VOTE_QUERY = "INSERT INTO votes (item_id, username) VALUES (?, ?)";
    public static final String FETCH_ITEM_ID_QUERY = "SELECT menu_item_id FROM menu_items WHERE meal_item_name = ?";
    public static final String UPDATE_VOTE_COUNT_QUERY = "UPDATE menu_items SET vote_count = vote_count + 1 WHERE menu_item_id = ?";
    public static final String CHECK_FEEDBACK_QUERY = "SELECT 1 FROM feedback WHERE username = ? AND item_name = ? AND DATE(feedback_date) = CURDATE()";
    public static final String INSERT_FEEDBACK_QUERY = "INSERT INTO feedback (item_id, rating, comment, username, sentiment_score, sentiments) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String SHOW_MENU_QUERY = "SELECT name, type_name, rating, sentiment_score, sentiments FROM menu_items";
    public static final String UPDATE_PROFILE_QUERY = "UPDATE employee_profile SET foodcategory = ?, spicelevel = ?, foodtypes = ?, sweettooth = ? WHERE username = ?";
    public static final String SELECT_OPTIONS_QUERY = "SELECT id, type_name FROM %s";

	// Sentiment constants
	public static final int SCORE_NEGATIVE_STRONG = 1;
    public static final int SCORE_NEGATIVE_WEAK = 2;
    public static final int SCORE_NEUTRAL = 3;
    public static final int SCORE_POSITIVE_WEAK = 4;
    public static final int SCORE_POSITIVE_STRONG = 5;
	public static final Set<String> NEGATIVE_STRONG = new HashSet<>(
			Arrays.asList("Awful", "Horrible", "Terrible", "Disgusting", "Hate", "Unbearable", "Repulsive", "Atrocious",
					"Dreadful", "Appalling", "Abysmal", "Nauseating", "Revolting", "Loathsome", "Deplorable",
					"Abominable", "Vile", "Detestable", "Unpleasant", "Inferior", "Miserable", "Horrendous",
					"Distasteful", "Grotesque", "Horrific", "Foul", "Putrid", "Vicious", "Heinous", "Diabolical"))
			.stream().map(String::toLowerCase).collect(Collectors.toSet());

	public static final Set<String> NEGATIVE_WEAK = new HashSet<>(Arrays.asList("Bad", "Poor", "Disliked",
			"Unsatisfactory", "Subpar", "Mediocre", "Unpleasant", "Disappointing", "Inferior", "Lacking",
			"Unimpressive", "Deficient", "Lousy", "Substandard", "Unacceptable", "Faulty", "Flawed", "Inadequate",
			"Defective", "Unappealing", "Lamentable", "Unfortunate", "Second-rate", "Shoddy", "Mediocre", "Substandard",
			"Unfulfilling", "Regrettable", "Lackluster", "Passable")).stream().map(String::toLowerCase)
			.collect(Collectors.toSet());

	public static final Set<String> NEUTRAL = new HashSet<>(Arrays.asList("Average", "Okay", "Fine", "Satisfactory",
			"Indifferent", "Moderate", "Fair", "Unremarkable", "Tolerable", "Middling", "Passable", "Standard",
			"Acceptable", "Usual", "Ordinary", "Plain", "Commonplace", "Middling", "Routine", "Regular", "So-so",
			"Workable", "Decent", "Moderate", "Reasonable", "Mediocre", "Average", "All right", "Standard", "Adequate"))
			.stream().map(String::toLowerCase).collect(Collectors.toSet());

	public static final Set<String> POSITIVE_WEAK = new HashSet<>(Arrays.asList("Good", "Enjoyable", "Pleasant",
			"Satisfying", "Nice", "Liked", "Delightful", "Pleasing", "Admirable", "Commendable", "Worthy", "Gratifying",
			"Pleasurable", "Appealing", "Lovely", "Congenial", "Agreeable", "Charming", "Delightful", "Rewarding",
			"Pleasurable", "Favorable", "Admirable", "Superior", "Nice", "Praiseworthy", "Positive", "Gratifying",
			"Encouraging", "Pleasant")).stream().map(String::toLowerCase).collect(Collectors.toSet());

	public static final Set<String> POSITIVE_STRONG = new HashSet<>(Arrays.asList("Excellent", "Fantastic", "Amazing",
			"Wonderful", "Outstanding", "Superb", "Love", "Exceptional", "Marvelous", "Brilliant", "Terrific",
			"Remarkable", "Phenomenal", "Extraordinary", "Magnificent", "Perfect", "Splendid", "Glorious", "Stellar",
			"Exquisite", "Superb", "Unmatched", "Unbeatable", "Impressive", "Stunning", "Sensational", "Divine",
			"Awesome", "Superior", "Tasty", "Sweet", "Top-notch")).stream().map(String::toLowerCase)
			.collect(Collectors.toSet());
}
