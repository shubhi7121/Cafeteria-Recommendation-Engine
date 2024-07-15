package client;

import java.io.*;
import org.json.JSONObject;

import exceptions.AdminServiceException;
import clientConstants.Constants;

public class AdminService {
	private final BufferedReader userInput;
	private final BufferedReader serverInput;
	private final PrintWriter serverOutput;

	public AdminService(BufferedReader userInput, BufferedReader serverInput, PrintWriter serverOutput) {
		this.userInput = userInput;
		this.serverInput = serverInput;
		this.serverOutput = serverOutput;
	}

	public boolean handleCommands() throws AdminServiceException {
		while (true) {
			showMenuOptions();
			String choice = getUserInput();

			if (Constants.EXIT.equalsIgnoreCase(choice)) {
				return true;
			}

			try {
				processChoice(choice);
			} catch (IOException e) {
				throw new AdminServiceException("Error processing admin command", e);
			}
		}
	}

	private void showMenuOptions() {
		System.out.println("\nAdmin Service Menu:");
		System.out.println("1. Add menu item");
		System.out.println("2. Update menu item");
		System.out.println("3. Delete menu item");
		System.out.println("4. Show menu");
		System.out.println("5. Login Activity");
		System.out.println("6. Discard Menu List");
		System.out.println("Type EXIT to logout");
		System.out.print("Enter your choice: ");
	}

	private String getUserInput() throws AdminServiceException {
		try {
			return userInput.readLine().trim();
		} catch (IOException e) {
			throw new AdminServiceException("Error reading user input", e);
		}
	}

	private void processChoice(String choice) throws IOException, AdminServiceException {
		JSONObject data = new JSONObject();
		data.put("choice", choice);
		serverOutput.println(data.toString());

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
		default:
			System.out.println("Invalid command");
		}
	}

	private void handleAddMenuItem() throws IOException, AdminServiceException {
		String name = promptUser("Enter name: ");
		String price = promptUser("Enter price: ");
		String mealType = promptUser("Enter Meal Type (1-3): ");
		String availabilityStatus = promptUser("Enter availability status (0-1): ");
		String foodCategory = promptUser("Enter food category (1-3): ");
		String spiceLevel = promptUser("Enter spice level (1-3): ");
		String foodType = promptUser("Enter food type (1-3): ");
		String sweetTooth = promptUser("Enter sweetTooth (0-1): ");

		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("price", price);
		data.put("mealType", mealType);
		data.put("availabilityStatus", availabilityStatus);
		data.put("foodCategory", foodCategory);
		data.put("spiceLevel", spiceLevel);
		data.put("foodType", foodType);
		data.put("sweetTooth", sweetTooth);
		serverOutput.println(data.toString());

		printServerResponse();
	}

	private void handleUpdateMenuItem() throws IOException, AdminServiceException {
		String name = promptUser("Enter name: ");
		String price = promptUser("Enter price: ");
		String availabilityStatus = promptUser("Enter availability status (0-1): ");

		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("price", price);
		data.put("availabilityStatus", availabilityStatus);
		serverOutput.println(data.toString());

		printServerResponse();
	}

	private void handleDeleteMenuItem() throws IOException, AdminServiceException {
		String name = promptUser("Enter name: ");

		JSONObject data = new JSONObject();
		data.put("name", name);
		serverOutput.println(data.toString());

		System.out.println(serverInput.readLine());
	}

	private void handleShowMenu() throws IOException, AdminServiceException {
		printServerResponseUntilEndOfMenu();
	}

	private void viewLoginActivity() throws IOException, AdminServiceException {
		printServerResponseUntilEndOfMenu();
	}

	private void discardMenuList() throws IOException, AdminServiceException {
		int count = countMenuItems();
		handleItemActions(count);
	}

	private String promptUser(String prompt) throws IOException {
		System.out.print(prompt);
		return userInput.readLine().trim();
	}

	private void printServerResponse() throws IOException {
		System.out.println(serverInput.readLine());
		System.out.println(serverInput.readLine());
	}

	private void printServerResponseUntilEndOfMenu() throws IOException {
		String serverResponse;
		while (!(serverResponse = serverInput.readLine()).equals(Constants.END_OF_MENU)) {
			System.out.println(serverResponse);
		}
	}

	private int countMenuItems() throws IOException {
		String serverResponse;
		int count = 0;
		while (!(serverResponse = serverInput.readLine()).equals("END_OF_MENU")) {
			System.out.println(serverResponse);
			count++;
		}
		JSONObject countResponse = new JSONObject();
		countResponse.put("count", count);
		serverOutput.println(countResponse.toString());
		return count;
	}

	private void handleItemActions(int itemCount) throws IOException, AdminServiceException {
		for (int i = 0; i < itemCount; i++) {
			String response = promptUser("Do you want to take action on any item? (y/n)").toLowerCase();
			JSONObject actionResponse = new JSONObject();
			actionResponse.put("response", response);
			serverOutput.println(actionResponse.toString());

			if ("n".equals(response)) {
				break;
			} else if ("y".equals(response)) {
				String itemId = promptUser("Enter the item ID you want to take action on:");
				String action = promptUser("Enter the action you want to perform:\n1. Roll out survey\n2. Delete item");

				JSONObject request = new JSONObject();
				request.put("action", "discard_item");
				request.put("item_id", itemId);
				request.put("action_type", action);
				serverOutput.println(request.toString());

				System.out.println(serverInput.readLine());
			} else {
				System.out.println("Invalid input. Please enter 'y' or 'n'.");
			}
		}
	}
}
