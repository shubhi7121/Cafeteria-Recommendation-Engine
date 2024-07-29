package authentication;

import java.sql.*;

import constants.Constants;
import exceptions.UserActivityException;

public class UserActivity {

    private final String email;
    private final Connection connection;

    public UserActivity(String email, Connection connection) {
        this.email = email;
        this.connection = connection;
    }

    public void logIn() throws UserActivityException {
        executeUpdate(Constants.INSERT_LOGIN_INFO, "Error adding login info");
    }

    public void logOut() throws UserActivityException {
        executeUpdate(Constants.UPDATE_LOGOUT_INFO, "Error adding logout info");
    }

    private void executeUpdate(String query, String errorMessage) throws UserActivityException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new UserActivityException(errorMessage, null);
            }
        } catch (SQLException e) {
            throw new UserActivityException(errorMessage, e);
        }
    }
}
