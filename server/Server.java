package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

import constants.Constants;
import database.DatabaseConnection;
import exceptions.DatabaseConnectionException;
import exceptions.ServerException;

public class Server {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(Constants.PORT)) {
            System.out.println("Server is listening on port " + Constants.PORT);
            Connection connection = initializeDatabaseConnection();

            while (true) {
                handleClientConnection(serverSocket, connection);
            }
        } catch (IOException e) {
            throw new ServerException("Server error occurred.", e);
        }
    }

    private static Connection initializeDatabaseConnection() {
        try {
            return DatabaseConnection.getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            throw new DatabaseConnectionException("Database connection error.", e);
        }
    }

    private static void handleClientConnection(ServerSocket serverSocket, Connection connection) {
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + clientSocket.getInetAddress());
            new ClientHandler(clientSocket, connection).start();
        } catch (IOException e) {
            throw new ServerException("Error accepting client connection.", e);
        }
    }
}
