package clientServer;

import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class Client {
	private static final String HOST = "localhost";
	private static final int PORT = 12345;

	public static void main(String[] args) {
		try (Socket socket = new Socket(HOST, PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

			int attempts = 0;
			boolean authenticated = false;

			while (attempts < 3 && !authenticated) {
				System.out.println("Enter your username:");
				String username = consoleInput.readLine();
				System.out.println("Enter your email:");
				String email = consoleInput.readLine();

				JSONObject data = new JSONObject();
				data.put("username", username);
				data.put("email", email);

				out.println(data.toString());

				String response = in.readLine();
				JSONObject jsonResponse = new JSONObject(response);

				if (jsonResponse.getBoolean("authenticated")) {
					authenticated = true;
					String role = jsonResponse.getString("role");
					System.out.println("Authentication successful.");
					if (role.equals("Admin")) {
						System.out.println("Hey Admin");
						AdminService admin = new AdminService(consoleInput, in, out, username);
						admin.handleCommands();
					} else if (role.equals("Chef")) {
						System.out.println("Hey Chef");
						ChefService chef = new ChefService(consoleInput, in, out, username);
						chef.handleCommands();
					} else if (role.equals("Employee")) {
						System.out.println("Hey Employee");
						EmployeeService emp = new EmployeeService(consoleInput, in, out, username);
						emp.handleCommands();
					}
				} else {
					attempts++;
					System.out.println("Authentication failed. Attempts left: " + (3 - attempts));
				}
			}
			if (!authenticated) {
				System.out.println("Maximum authentication attempts reached. User blocked.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
