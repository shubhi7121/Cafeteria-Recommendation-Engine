package clientServer;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmployeeService {
	private final BufferedReader userInput;
	private final BufferedReader in;
	private final PrintWriter out;
	private final String username;

	public EmployeeService(BufferedReader userInput, BufferedReader in, PrintWriter out, String username) {
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
			case "exit":
				break;
			default:
				System.out.println("Invalid command");
			}
		}
	}

	private void viewNotification() throws IOException {
		String serverResponse;
		while (!(serverResponse = in.readLine()).equals("END_OF_OPTIONS")) {
			System.out.println(serverResponse);
		}
		System.out.println("Do you want to see any particular notification? (y/n)");
		String input = userInput.readLine().trim();

		if (input.equalsIgnoreCase("y")) {
			System.out.println("Select a notification ID to view details:");
			String id = userInput.readLine().trim();

			JSONObject request = new JSONObject();
			request.put("action", "view_details");
			request.put("notification_id", id);
			out.println(request.toString());

			String serverResponse2 = in.readLine();

			if (serverResponse2.contains("Discard")) {
				JSONObject response = new JSONObject(serverResponse2);
				String action = response.getString("action");
				String name = response.getString("item_name");

				System.out.println("Please answer the following questions:");

				System.out.println(" What didn’t you like about " + name + " ?");
				String answer1 = userInput.readLine().trim();

				System.out.println("How would you like " + name + " to taste?");
				String answer2 = userInput.readLine().trim();

				System.out.println("Share your mom’s recipe");
				String answer3 = userInput.readLine().trim();

				System.out.println("Thank you for your response!!");
			} else {
				while (!(serverResponse2 = in.readLine()).equals("END_OF_DETAILS")) {
					System.out.println(serverResponse2);
				}
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

	private void handleFeedbackForToday() throws IOException {
		System.out.print("Enter name: ");
		String name = userInput.readLine();

		System.out.print("Enter rating: ");
		String rating = userInput.readLine();

		System.out.print("Enter comments: ");
		String feedback = userInput.readLine();

		JSONObject data = new JSONObject();
		data.put("action", "feedback");
		data.put("name", name);
		data.put("rating", rating);
		data.put("comment", feedback);
		System.out.println("data: " + data);
		out.println(data.toString());

		String message = in.readLine();
		System.out.println(message);
	}

	private void handleVoteForTomorrow() throws IOException, JSONException {
		String[] mealTypes = { "breakfast", "lunch", "dinner" };
		for (String mealType : mealTypes) {
			System.out.println("Viewing options for: " + mealType);

			JSONObject request = new JSONObject();
			request.put("action", "view_menu");
			request.put("meal_type", mealType);
			out.println(request.toString());

			String response = in.readLine();
			JSONArray menuItems = new JSONArray(response);

			System.out.println("Available Items:");
			for (int i = 0; i < menuItems.length(); i++) {
				JSONObject item = menuItems.getJSONObject(i);
				System.out.println((i + 1) + ". " + item.getString("item_name") + " - Rating: "
						+ item.getDouble("rating") + " - Sentiments: " + item.getString("sentiments")
						+ " - Sentiments: " + item.getString("sentimentSet"));
			}

			System.out.println(
					"Enter the name of the item you want to vote for (or type 'skip' to skip voting for this meal):");
			String item = userInput.readLine();

			if (!item.equalsIgnoreCase("skip")) {
				JSONObject voteRequest = new JSONObject();
				voteRequest.put("action", "vote");
				voteRequest.put("item_name", item);
				System.out.println(voteRequest + "vote");
				out.println(voteRequest.toString());

				String voteResponse = in.readLine();
				System.out.println(voteResponse);
			}
		}
	}

	private void handleShowMenu() throws IOException {
		String serverResponse;
		while (!(serverResponse = in.readLine()).equals("END_OF_MENU")) {
			System.out.println(serverResponse);
		}
	}

	private void updateProfile() throws IOException {
		
	}

}
