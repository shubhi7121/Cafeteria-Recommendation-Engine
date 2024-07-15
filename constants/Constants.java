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
}
