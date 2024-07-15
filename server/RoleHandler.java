package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.Connection;
import exceptions.UnknownRoleException;
import interfaces.Role;
import constants.Constants;

public class RoleHandler {
    private final RoleFactory roleFactory;

    public RoleHandler(Connection connection, BufferedReader in, PrintWriter out, String username) {
        this.roleFactory = new RoleFactory(connection, in, out, username);
    }

    public Role getRole(String role) {
        return roleFactory.createRole(role);
    }

    private static class RoleFactory {
        private final Connection connection;
        private final BufferedReader in;
        private final PrintWriter out;
        private final String username;

        public RoleFactory(Connection connection, BufferedReader in, PrintWriter out, String username) {
            this.connection = connection;
            this.in = in;
            this.out = out;
            this.username = username;
        }

        public Role createRole(String role) {
            switch (role.toLowerCase()) {
                case Constants.ROLE_ADMIN:
                    return new Admin(connection, in, out, username);
                case Constants.ROLE_CHEF:
                    return new Chef(connection, in, out, username);
                case Constants.ROLE_EMPLOYEE:
                    return new Employee(connection, in, out, username);
                default:
                    throw new UnknownRoleException(role);
            }
        }
    }
}
