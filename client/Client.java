package client;

import java.io.*;
import java.net.Socket;

import org.json.JSONObject;

import clientConstants.Constants;
import exceptions.AdminServiceException;
import exceptions.ChefServiceException;
import exceptions.EmployeeServiceException;

public class Client {

    public static void main(String[] args) {
        try (Socket socket = new Socket(Constants.HOST, Constants.PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            authenticateAndHandleService(consoleInput, in, out);

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private static void authenticateAndHandleService(BufferedReader consoleInput, BufferedReader in, PrintWriter out)
            throws IOException {
        int attempts = 0;
        boolean authenticated = false;

        while (attempts < Constants.MAX_ATTEMPTS && !authenticated) {
            String username = promptInput(consoleInput, "Enter your username:");
            String email = promptInput(consoleInput, "Enter your email:");

            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("email", email);

            out.println(data.toString());

            String response = in.readLine();
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.getBoolean("authenticated")) {
                authenticated = true;
                String role = jsonResponse.getString("role");
                System.out.println("Authentication successful. Welcome " + role + ".");
                handleRole(role, consoleInput, in, out);
            } else {
                attempts++;
                System.out.println("Authentication failed. Attempts left: " + (Constants.MAX_ATTEMPTS - attempts));
            }
        }

        if (!authenticated) {
            System.out.println("Maximum authentication attempts reached. User blocked.");
        }
    }

    private static String promptInput(BufferedReader reader, String prompt) throws IOException {
        System.out.println(prompt);
        return reader.readLine();
    }

    private static void handleRole(String role, BufferedReader consoleInput, BufferedReader in, PrintWriter out)
            throws IOException {
        try {
            switch (role) {
                case "Admin":
                    handleAdminService(consoleInput, in, out);
                    break;
                case "Chef":
                    handleChefService(consoleInput, in, out);
                    break;
                case "Employee":
                    handleEmployeeService(consoleInput, in, out);
                    break;
                default:
                    System.out.println("Unknown role.");
                    break;
            }
        } catch (AdminServiceException | ChefServiceException | EmployeeServiceException e) {
            System.err.println("Service error: " + e.getMessage());
        }
    }

    private static void handleAdminService(BufferedReader consoleInput, BufferedReader in, PrintWriter out)
            throws IOException, AdminServiceException {
        AdminService admin = new AdminService(consoleInput, in, out);
        if (admin.handleCommands()) {
            System.out.println("Exiting your session.");
        }
    }

    private static void handleChefService(BufferedReader consoleInput, BufferedReader in, PrintWriter out)
            throws IOException, ChefServiceException {
        ChefService chef = new ChefService(consoleInput, in, out);
        if (chef.handleCommands()) {
            System.out.println("Exiting your session.");
        }
    }

    private static void handleEmployeeService(BufferedReader consoleInput, BufferedReader in, PrintWriter out)
            throws IOException, EmployeeServiceException {
        EmployeeService employee = new EmployeeService(consoleInput, in, out);
        if (employee.handleCommands()) {
            System.out.println("Exiting your session.");
        }
    }
}
