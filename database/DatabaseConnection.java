package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import constants.Constants;

public class DatabaseConnection {

    private DatabaseConnection() {
        // private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        try {
            Class.forName(Constants.JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("MySQL JDBC Driver not found. Ensure it's in the classpath.", e);
        }

        try {
            return DriverManager.getConnection(Constants.JDBC_URL, Constants.JDBC_USER, Constants.JDBC_PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("Unable to establish a connection to the database.", e);
        }
    }
}
