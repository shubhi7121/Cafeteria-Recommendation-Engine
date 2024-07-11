package server;

import java.io.*;
import java.sql.Connection;

public class RoleHandler {
    private final Connection connection;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String username;

    public RoleHandler(Connection connection, BufferedReader in, PrintWriter out, String username) {
        this.connection = connection;
        this.in = in;
        this.out = out;
        this.username = username;
    }

    public Role getRoleObject(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                return new Admin(connection, in, out, username);
            case "chef":
                return new Chef(connection, in, out, username);
            case "employee":
                return new Employee(connection, in, out, username);
            default:
                out.println("Unknown role. Access denied.");
                return null;
        }
    }
}