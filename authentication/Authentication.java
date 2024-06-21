package authentication;

import java.io.*;
import java.sql.*;

public class Authentication {
    private Connection connection;
    private BufferedReader in;
    private PrintWriter out;

    public Authentication(Connection connection, BufferedReader in, PrintWriter out) {
        this.connection = connection;
        this.in = in;
        this.out = out;
    }

    public String authenticateUser() throws IOException, SQLException {
        out.println("Enter your email: ");
        String email = in.readLine();
        System.out.println("Received email: " + email);

        if (emailExists(email)) {
        	String role = getUserRole(email);
            out.println("Login successful");
            return role;
        } else {
            out.println("Email not found. Please try again.");
            System.out.println("Email not found for: " + email);
            return null;
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM employee WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private String getUserRole(String email) throws SQLException {
        String query = "SELECT role.role_name from employee inner join role on employee.role_id = role.role_id where email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role.role_name");
                }
            }
        }
        return "Unknown";
    }
}
