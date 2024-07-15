package client;

import java.io.*;
import org.json.*;

import exceptions.EmployeeServiceException;
import clientConstants.Constants;

public class EmployeeService {
	private final BufferedReader userInput;
	private final BufferedReader serverInput;
	private final PrintWriter serverOutput;

	public EmployeeService(BufferedReader userInput, BufferedReader serverInput, PrintWriter serverOutput) {
		this.userInput = userInput;
		this.serverInput = serverInput;
		this.serverOutput = serverOutput;
	}

	public boolean handleCommands() throws EmployeeServiceException {
		while (true) {
			showMenuOptions();
			String choice = getUserInput();

			if (Constants.EXIT.equalsIgnoreCase(choice)) {
				return true;
			}

			try {
				processChoice(choice);
			} catch (IOException e) {
				throw new EmployeeServiceException("Error processing employee command", e);
			}
		}
	}

	private void showMenuOptions() {
		System.out.println("\nEmployee Service Menu:");
		System.out.println("1. Give Feedback");
		System.out.println("2. Vote for Tomorrow Menu");
		System.out.println("3. Show Menu");
		System.out.println("4. Update profile");
		System.out.println("5. View Notification");
		System.out.println("Type 'EXIT' to quit");
		System.out.print("Select an option: ");
	}

	private String getUserInput() throws EmployeeServiceException {
		try {
			return userInput.readLine().trim();
		} catch (IOException e) {
			throw new EmployeeServiceException("Error reading user input", e);
		}
	}

	private void processChoice(String choice) throws IOException, EmployeeServiceException {
		JSONObject data = new JSONObject();
		data.put("choice", choice);
		serverOutput.println(data.toString());

		switch (choice) {
		case "1":
			handleFeedbackForToday();
			break;
		case "2":
			handleVoteForTomorrow();
			break;
		case "3":
			handleShowMenu();
			break;
		case "4":
			updateProfile();
			break;
		case "5":
			viewNotification();
			break;
		default:
			System.out.println("Invalid command");
		}
	}

	private void handleFeedbackForToday() throws IOException, EmployeeServiceException {
		String name = promptUser("Enter name: ");
		String rating = promptUser("Enter rating: ");
		String feedback = promptUser("Enter comments: ");

		JSONObject data = new JSONObject();
		data.put("action", "feedback");
		data.put("name", name);
		data.put("rating", rating);
		data.put("comment", feedback);
		serverOutput.println(data.toString());

		String message = serverInput.readLine();
		System.out.println(message);
	}

	private void handleVoteForTomorrow() throws IOException, JSONException, EmployeeServiceException {
		for (String mealType : Constants.MEAL_TYPES) {
			System.out.println("Viewing options for: " + mealType);

			JSONObject request = new JSONObject();
			request.put("action", "view_menu");
			request.put("meal_type", mealType);
			serverOutput.println(request.toString());

			String response = serverInput.readLine();
			JSONArray menuItems = new JSONArray(response);

			System.out.println("Available Items:");
			for (int i = 0; i < menuItems.length(); i++) {
				JSONObject item = menuItems.getJSONObject(i);
				System.out.println((i + 1) + ". " + item.getString("item_name") + " - Rating: "
						+ item.getDouble("rating") + " - Sentiments: " + item.getString("sentiments"));
			}

			String item = promptUser(
					"Enter the name of the item you want to vote for (or type 'skip' to skip voting for this meal):");

			if (!item.equalsIgnoreCase("skip")) {
				JSONObject voteRequest = new JSONObject();
				voteRequest.put("action", "vote");
				voteRequest.put("item_name", item);
				serverOutput.println(voteRequest.toString());

				String voteResponse = serverInput.readLine();
				System.out.println(voteResponse);
			}
		}
	}

	private void handleShowMenu() throws IOException {
		printServerResponseUntilEnd(Constants.END_OF_MENU);
	}

	private void updateProfile() throws IOException, EmployeeServiceException {
		String foodCategory = promptForSelection("Please select one food category: ");
		sendSelection("foodCategory", foodCategory);

		String spiceLevel = promptForSelection("Please select your spice level: ");
		sendSelection("spiceLevel", spiceLevel);

		String foodType = promptForSelection("What do you prefer most?");
		sendSelection("foodType", foodType);

		String sweetTooth = promptUser("Do you have a sweet tooth?\n1. Yes\n0. No");
		sendSelection("sweetTooth", sweetTooth);

		String message = serverInput.readLine();
		System.out.println(message);
	}

	private void viewNotification() throws IOException, EmployeeServiceException {
		printServerResponseUntilEnd(Constants.END_OF_OPTIONS);

		String input = promptUser("Do you want to see any particular notification? (y/n)");

		if ("y".equalsIgnoreCase(input)) {
			String id = promptUser("Select a notification ID to view details:");

			JSONObject request = new JSONObject();
			request.put("action", "view_details");
			request.put("notification_id", id);
			serverOutput.println(request.toString());

			String serverResponse = serverInput.readLine();

			if (serverResponse.contains("Discard")) {
				JSONObject response = new JSONObject(serverResponse);
				String name = response.getString("item_name");

				promptUser("What didn’t you like about " + name + " ?");
				promptUser("How would you like " + name + " to taste?");
				promptUser("Share your mom’s recipe");

				System.out.println("Thank you for your response!!");
			} else {
				printServerResponseUntilEnd("END_OF_DETAILS");
			}
		}
	}

	private String promptUser(String prompt) throws EmployeeServiceException {
		try {
			System.out.print(prompt);
			return userInput.readLine().trim();
		} catch (IOException e) {
			throw new EmployeeServiceException("Error reading user input", e);
		}
	}

	private String promptForSelection(String prompt) throws IOException, EmployeeServiceException {
		String serverResponse;
		System.out.print(prompt);
		while (!(serverResponse = serverInput.readLine()).equals(Constants.END_OF_OPTIONS)) {
			System.out.println(serverResponse);
		}
		return getUserInput();
	}

	private void printServerResponseUntilEnd(String endMarker) throws IOException {
		String serverResponse;
		while (!(serverResponse = serverInput.readLine()).equals(endMarker)) {
			System.out.println(serverResponse);
		}
	}

	private void sendSelection(String key, String value) throws IOException {
		JSONObject selection = new JSONObject();
		selection.put(key, value);
		serverOutput.println(selection.toString());
	}
}
