package client;

import java.io.*;
import org.json.*;

import exceptions.ChefServiceException;
import clientConstants.Constants;

public class ChefService {
	private final BufferedReader userInput;
	private final BufferedReader serverInput;
	private final PrintWriter serverOutput;

	public ChefService(BufferedReader userInput, BufferedReader serverInput, PrintWriter serverOutput) {
		this.userInput = userInput;
		this.serverInput = serverInput;
		this.serverOutput = serverOutput;
	}

	public boolean handleCommands() throws ChefServiceException {
		while (true) {
			showMenuOptions();
			String choice = getUserInput();

			if (Constants.EXIT.equalsIgnoreCase(choice)) {
				return true;
			}

			try {
				processChoice(choice);
			} catch (IOException e) {
				throw new ChefServiceException("Error processing chef command", e);
			}
		}
	}

	private void showMenuOptions() {
		System.out.println("\nChef Service Menu:");
		System.out.println("1. View Recommendation");
		System.out.println("2. Show Menu");
		System.out.println("3. Show Voting");
		System.out.println("4. Discard Menu");
		System.out.println("Type 'EXIT' to quit");
		System.out.print("Select an option: ");
	}

	private String getUserInput() throws ChefServiceException {
		try {
			return userInput.readLine().trim();
		} catch (IOException e) {
			throw new ChefServiceException("Error reading user input", e);
		}
	}

	private void processChoice(String choice) throws IOException, ChefServiceException {
		JSONObject data = new JSONObject();
		data.put("choice", choice);
		serverOutput.println(data.toString());

		switch (choice) {
		case "1":
			viewRecommendation();
			break;
		case "2":
			handleShowMenu();
			break;
		case "3":
			showVoting();
			break;
		case "4":
			discardMenuList();
			break;
		default:
			System.out.println("Invalid command");
		}
	}

	private void viewRecommendation() throws IOException, ChefServiceException {
		System.out.println("Enter the number of items you want to recommend: ");
		String noOfItems = getUserInput();

		String[] mealTypes = { "breakfast", "lunch", "dinner" };
		for (String mealType : mealTypes) {
			System.out.println("Viewing options for: " + mealType);

			JSONObject request = new JSONObject();
			request.put("action", "view_menu");
			request.put("meal_type", mealType);
			request.put("limit", noOfItems);
			serverOutput.println(request.toString());

			String response = serverInput.readLine();
			JSONArray menuItems = new JSONArray(response);

			System.out.println("Available Items:");
			for (int i = 0; i < menuItems.length(); i++) {
				JSONObject item = menuItems.getJSONObject(i);
				System.out.println((i + 1) + ". " + item.getString("item_name") + " - Rating: "
						+ item.getDouble("rating") + " - Sentiments: " + item.getString("sentiments"));
			}

			System.out.println("Enter the names of the items you want to vote for, separated by commas:");
			String items = getUserInput();

			JSONObject voteRequest = new JSONObject();
			voteRequest.put("action", "vote");
			voteRequest.put("item_name", items);
			serverOutput.println(voteRequest.toString());
		}
	}

	private void handleShowMenu() throws IOException {
		printServerResponseUntilEndOfMenu();
	}

	private void showVoting() throws IOException {
		printServerResponseUntilEndOfMenu();
	}

	private void discardMenuList() throws IOException, ChefServiceException {
		int count = countMenuItems();
		handleItemActions(count);
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

	private void handleItemActions(int itemCount) throws IOException, ChefServiceException {
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

	private String promptUser(String prompt) throws ChefServiceException {
		try {
			System.out.print(prompt);
			return userInput.readLine().trim();
		} catch (IOException e) {
			throw new ChefServiceException("Error reading user input", e);
		}
	}
}