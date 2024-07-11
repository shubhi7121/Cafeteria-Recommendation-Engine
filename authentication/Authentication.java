package authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

public class Authentication {

	private Connection connection;
	private BufferedReader in;
	private PrintWriter out;
	private String email;
	private String username;

	public Authentication(Connection connection, BufferedReader in, PrintWriter out) {
		this.connection = connection;
		this.in = in;
		this.out = out;
	}

	public String authenticateUser() throws SQLException, IOException {
		int attempts = 0;
		while (attempts < 3) {
			String jsonString = in.readLine();
			JSONObject receivedJson = new JSONObject(jsonString);
			username = receivedJson.getString("username");
			email = receivedJson.getString("email");

			boolean authenticated = validateCredentials(username, email);
			if (authenticated) {
				String role = getUserRole(username);
				handleSuccessfulLogin(email);
				JSONObject response = new JSONObject();
				response.put("authenticated", true);
				response.put("role", role);
				out.println(response.toString());
				return role;
			} else {
				attempts++;
				if (attempts < 3) {
					JSONObject response = new JSONObject();
					response.put("authenticated", false);
					response.put("message", "Authentication failed. Attempts left: " + (3 - attempts));
					out.println(response.toString());
				}
			}
		}
		JSONObject response = new JSONObject();
		response.put("authenticated", false);
		response.put("message", "Maximum authentication attempts reached. User blocked.");
		out.println(response.toString());
		return null; // Max attempts reached
	}

	private boolean validateCredentials(String username, String email) throws SQLException {
		String sql = "SELECT COUNT(*) AS count FROM employee WHERE employee_id = ? and email = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setString(2, email);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int count = rs.getInt("count");
					return count > 0;
				}
			}
		}
		return false;
	}

	private String getUserRole(String username) throws SQLException {
		String query = "SELECT role.role_name from employee inner join role on employee.role_id = role.role_id where employee_id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, username);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("role.role_name");
				}
			}
		}
		return "Unknown";
	}

	private void handleSuccessfulLogin(String email) throws SQLException {
		UserActivity userActivity = new UserActivity(email, connection);
		userActivity.addLogInInfo();
	}

	public String getUserEmail() {
		return email;
	}

	public String getUserName() {
		return username;
	}
}
