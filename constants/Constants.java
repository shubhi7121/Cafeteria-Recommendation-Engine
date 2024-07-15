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
}
