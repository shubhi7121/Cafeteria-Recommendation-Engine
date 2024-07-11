package clientServer;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import org.json.JSONObject;

public class ClientHandler extends Thread {
	private Socket clientSocket;
	private Connection connection;

	public ClientHandler(Socket clientSocket, Connection connection) {
		this.clientSocket = clientSocket;
		this.connection = connection;
	}

	@Override
	public void run() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
			Authentication auth = new Authentication(connection, in, out);
			String role = auth.authenticateUser();
			String email = auth.getUserEmail();
			String username = auth.getUserName();
			if (role != null) {
				RoleHandler roleHandler = new RoleHandler(connection, in, out, username);
				Role userRole = roleHandler.getRoleObject(role);
				if (userRole != null) {
					boolean exit = false;
					while (!exit) {
						String jsonString = in.readLine();
						JSONObject receivedJson = new JSONObject(jsonString);
						String choice = receivedJson.getString("choice");
						if (choice.equalsIgnoreCase("exit")) {
							exit = true;
							UserActivity userActivity = new UserActivity(email, connection);
							userActivity.addLogOutInfo();
						} else {
							userRole.handleAction(choice);
						}
					}
				}
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}