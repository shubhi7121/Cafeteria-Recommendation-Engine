package clientServer;

import java.io.*;
import java.net.*;
import java.sql.*;

import databaseConnection.DatabaseConnection;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) throws ClassNotFoundException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            Connection connection = DatabaseConnection.getConnection();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new ClientHandler(socket, connection).start();
            }
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }
}
