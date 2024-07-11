package authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserActivity {

	private String email;
	private Connection connection;

	public UserActivity(String email, Connection connection) {
		this.email = email;
		this.connection = connection;
	}

	public boolean addLogInInfo() {
		try (PreparedStatement statement = connection.prepareStatement(Constants.INSERT_LOGIN_INFO)) {
			statement.setString(1, email);
			int rowsAffected = statement.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error adding login info: " + e.getMessage());
			return false;
		}
	}

	public boolean addLogOutInfo() {
		try (PreparedStatement statement = connection.prepareStatement(Constants.UPDATE_LOGOUT_INFO)) {
			statement.setString(1, email);
			int rowsAffected = statement.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error adding logout info: " + e.getMessage());
			return false;
		}
	}
}