package authentication;

import java.io.*;
import java.sql.*;
import org.json.JSONObject;

import constants.Constants;
import exceptions.AuthenticationException;
import exceptions.MaxAttemptsExceededException;
import exceptions.UserActivityException;

public class Authentication {

    private final Connection connection;
    private final BufferedReader inputReader;
    private final PrintWriter outputWriter;
    private String userEmail;
    private String userName;

    public Authentication(Connection connection, BufferedReader inputReader, PrintWriter outputWriter) {
        this.connection = connection;
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
    }

    public String authenticateUser() throws IOException, AuthenticationException, SQLException, UserActivityException {
        int loginAttempts = 0;

        while (loginAttempts < Constants.MAX_ATTEMPTS) {
            JSONObject userCredentials = readUserCredentials();
            userName = userCredentials.getString("username");
            userEmail = userCredentials.getString("email");

            if (validateCredentials(userName, userEmail)) {
                String userRole = getUserRole(userName);
                handleSuccessfulLogin(userEmail);
                sendAuthenticationSuccessResponse(userRole);
                return userRole;
            } else {
                loginAttempts++;
                if (loginAttempts < Constants.MAX_ATTEMPTS) {
                    sendAuthenticationFailureResponse(Constants.MAX_ATTEMPTS - loginAttempts);
                }
            }
        }

        sendMaxAttemptsExceededResponse();
        throw new MaxAttemptsExceededException();
    }

    private JSONObject readUserCredentials() throws IOException {
        String jsonInput = inputReader.readLine();
        return new JSONObject(jsonInput);
    }

    private boolean validateCredentials(String username, String email) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM employee WHERE employee_id = ? AND email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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
        String query = "SELECT role.role_name FROM employee INNER JOIN role ON employee.role_id = role.role_id WHERE employee_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role.role_name");
                }
            }
        }
        return Constants.UNKNOWN_ROLE;
    }

    private void handleSuccessfulLogin(String userEmail) throws SQLException, UserActivityException {
        UserActivity userActivity = new UserActivity(userEmail, connection);
        userActivity.logIn();
    }

    private void sendAuthenticationSuccessResponse(String userRole) {
        JSONObject response = new JSONObject();
        response.put(Constants.AUTHENTICATION_SUCCESS, true);
        response.put(Constants.ROLE, userRole);
        outputWriter.println(response.toString());
    }

    private void sendAuthenticationFailureResponse(int attemptsLeft) {
        JSONObject response = new JSONObject();
        response.put(Constants.AUTHENTICATION_SUCCESS, false);
        response.put(Constants.AUTHENTICATION_FAILED, Constants.AUTHENTICATION_FAILED_MESSAGE + attemptsLeft);
        outputWriter.println(response.toString());
    }

    private void sendMaxAttemptsExceededResponse() {
        JSONObject response = new JSONObject();
        response.put(Constants.AUTHENTICATION_SUCCESS, false);
        response.put(Constants.AUTHENTICATION_FAILED, Constants.MAX_ATTEMPTS_EXCEEDED_MESSAGE);
        outputWriter.println(response.toString());
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }
}
