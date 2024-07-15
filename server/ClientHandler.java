package server;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

import org.json.JSONObject;

import authentication.Authentication;
import authentication.UserActivity;
import exceptions.AuthenticationException;
import exceptions.MenuItemNotFoundException;
import exceptions.NotificationException;
import exceptions.UserActivityException;
import interfaces.Role;

public class ClientHandler extends Thread {

    private final Socket clientSocket;
    private final Connection connection;
    private String role;

    public ClientHandler(Socket clientSocket, Connection connection) {
        this.clientSocket = clientSocket;
        this.connection = connection;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            authenticateAndHandleUser(in, out);
        } catch (IOException | SQLException | NotificationException | MenuItemNotFoundException |
                 AuthenticationException | UserActivityException e) {
            handleException(e);
        } finally {
            closeClientSocket();
        }
    }

    private void authenticateAndHandleUser(BufferedReader in, PrintWriter out) 
            throws IOException, SQLException, NotificationException, MenuItemNotFoundException, AuthenticationException, UserActivityException {
        Authentication auth = new Authentication(connection, in, out);
        role = auth.authenticateUser();
        String email = auth.getUserEmail();
        String username = auth.getUserName();

        if (role != null) {
            processUserRole(username, in, out, email);
        }
    }

    private void processUserRole(String username, BufferedReader in, PrintWriter out, String email) 
            throws IOException, SQLException, NotificationException, MenuItemNotFoundException, UserActivityException {
        RoleHandler roleHandler = new RoleHandler(connection, in, out, username);
        Role userRole = roleHandler.getRole(role);

        if (userRole != null) {
            handleUserRoleActions(userRole, in, out, email);
        }
    }

    private void handleUserRoleActions(Role userRole, BufferedReader in, PrintWriter out, String email) 
            throws IOException, UserActivityException {
        boolean exit = false;

        while (!exit) {
            String jsonString = in.readLine();
            if (jsonString == null) {
                System.out.println("Client disconnected.");
                exit = true;
            } else {
                JSONObject receivedJson = new JSONObject(jsonString);
                if (!receivedJson.has("choice")) {
                    System.out.println("Received JSON does not contain 'choice' key.");
                    continue;
                }

                String choice = receivedJson.getString("choice");
                if (choice.equalsIgnoreCase("exit")) {
                    exit = true;
                } else {
                    try {
                        userRole.handleAction(choice);
                    } catch (Exception e) {
                        handleException(e);
                    }
                }
            }
        }

        if (exit) {
            logUserOut(email);
        }
    }

    private void logUserOut(String email) throws UserActivityException {
        UserActivity userActivity = new UserActivity(email, connection);
        userActivity.logOut();
    }

    private void handleException(Exception e) {
        System.err.println("An error occurred: " + e.getMessage());
        e.printStackTrace();
    }

    private void closeClientSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close client socket: " + e.getMessage());
        }
    }
}
