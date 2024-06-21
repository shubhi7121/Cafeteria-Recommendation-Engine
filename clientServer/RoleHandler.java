package clientServer;

import java.io.*;
import java.sql.*;

import roles.*;

public class RoleHandler {
    private Connection connection;
    private BufferedReader in;
    private PrintWriter out;

    public RoleHandler(Connection connection, BufferedReader in, PrintWriter out) {
        this.connection = connection;
        this.in = in;
        this.out = out;
    }

    public Role getRoleObject(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                return new Admin(connection, in, out);
            case "chef":
                return new Chef(connection, in, out);
            case "employee":
                return new Employee(connection, in, out);
            default:
                out.println("Unknown role. Access denied.");
                return null;
        }
    }
}
