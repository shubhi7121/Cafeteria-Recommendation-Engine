package clientServer;

import java.io.*;
import java.net.*;
import java.sql.*;

import authentication.Authentication;
import roles.Role;

public class ClientHandler extends Thread {
    private Socket socket;
    private Connection connection;

    public ClientHandler(Socket socket, Connection connection) {
        this.socket = socket;
        this.connection = connection;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Waiting for client input...");
            
            Authentication auth = new Authentication(connection, in, out);
            String role = auth.authenticateUser();
            if (role != null) {
                RoleHandler roleHandler = new RoleHandler(connection, in, out);
                Role userRole = roleHandler.getRoleObject(role);

                if (userRole != null) {
                    boolean exit = false;
                    while (!exit) {
                        userRole.showOptions();
                        String clientResponse = in.readLine();
                        if (clientResponse == null) {
                            System.out.println("Client disconnected.");
                            break;
                        }
                        if (clientResponse.equalsIgnoreCase("exit")) {  
                            exit = true;
                            out.println("Exiting...");
                        } else {
                            userRole.handleAction(clientResponse);
                        }
                    }
                }
            }

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
