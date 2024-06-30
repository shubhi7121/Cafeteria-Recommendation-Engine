package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;

public class AdminService {
	private final BufferedReader userInput;
	private final BufferedReader in;
	private final PrintWriter out;
	private final String username;

	public AdminService(BufferedReader userInput, BufferedReader in, PrintWriter out, String username) {
		this.userInput = userInput;
		this.in = in;
		this.out = out;
		this.username = username;
	}

	public void handleCommands() throws IOException {
		while (true) {
			showMenuOptions();

			String choice = userInput.readLine().trim();
			JSONObject data = new JSONObject();
			data.put("choice", choice);
			out.println(data.toString());

			switch (choice) {
			case "1":
				handleAddMenuItem();
				break;
			case "2":
				handleUpdateMenuItem();
				break;
			case "3":
				handleDeleteMenuItem();
				break;
			case "4":
				handleShowMenu();
				break;
			case "5":
				viewLoginActivity();
				break;
			case "6":
				discardMenuList();
				break;
			case "exit":
				break;
			default:
				System.out.println("Invalid command");
			}
		}
	}

	private void showMenuOptions() {
		System.out.println("1. Add menu item");
		System.out.println("2. Update menu item");
		System.out.println("3. Delete menu item");
		System.out.println("4. Show menu");
		System.out.println("5. Login Activity");
		System.out.println("6. Discard Menu List");
		System.out.println("Type EXIT to logout");
		System.out.print("Enter your choice: ");
	}

	private void handleAddMenuItem() throws IOException {
		System.out.print("Enter name: ");
		String name = userInput.readLine();

		System.out.print("Enter price: ");
		String price = userInput.readLine();

		System.out.print("Enter Meal Type (1-3): ");
		String mealType = userInput.readLine();

		System.out.print("Enter availability status (0-1): ");
		String availabilityStatus = userInput.readLine();

		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("price", price);
		data.put("mealType", mealType);
		data.put("availabilityStatus", availabilityStatus);
		out.println(data.toString());

		String message = in.readLine();
		System.out.println(message);
	}

	private void handleUpdateMenuItem() throws IOException {
		System.out.print("Enter name: ");
		String name = userInput.readLine();

		System.out.print("Enter price: ");
		String price = userInput.readLine();

		System.out.print("Enter availability status (0-1): ");
		String availabilityStatus = userInput.readLine();

		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("price", price);
		data.put("availabilityStatus", availabilityStatus);
		out.println(data.toString());

		String message = in.readLine();
		System.out.println(message);
	}

	private void handleDeleteMenuItem() throws IOException {
		System.out.print("Enter name: ");
		String name = userInput.readLine();

		JSONObject data = new JSONObject();
		data.put("name", name);
		out.println(data.toString());

		String message = in.readLine();
		System.out.println(message);
	}

	private void handleShowMenu() throws IOException {
		String serverResponse;
		while (!(serverResponse = in.readLine()).equals("END_OF_MENU")) {
			System.out.println(serverResponse);
		}
	}

	private void viewLoginActivity() throws IOException {
		
	}

	private void discardMenuList() throws IOException {
		
	}
}
