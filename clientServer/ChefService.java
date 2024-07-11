package clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChefService {
	private final BufferedReader userInput;
	private final BufferedReader in;
	private final PrintWriter out;
	private final String username;

	public ChefService(BufferedReader userInput, BufferedReader in, PrintWriter out, String username) {
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
			case "exit":
				break;
			default:
				System.out.println("Invalid command");
			}
		}
	}

	private void showMenuOptions() {
		System.out.println("\nEmployee Service Menu:");
		System.out.println("1. View Recommendation");
		System.out.println("2. Show Menu");
		System.out.println("3. Show Voting");
		System.out.println("4. Discard Menu");
		System.out.println("Type 'EXIT' to quit");
		System.out.print("Select an option: ");
	}

	private void discardMenuList() throws IOException {
	    String serverResponse;
	    int count = 0;

	    while (!(serverResponse = in.readLine()).equals("END_OF_MENU")) {
	        System.out.println(serverResponse);
	        count++;
	    }

	    for (int i = 0; i < count; i++) {
	        System.out.println("Do you want to take action on any item? (y/n)");
	        String response = userInput.readLine().trim().toLowerCase();
	        if (response.equals("n")) {
	            break; // Exit the loop if user inputs "n"
	        } else if (response.equals("y")) {
	            System.out.println("Enter the item ID you want to take action on:");
	            String item = userInput.readLine().trim();

	            System.out.println("Enter the action you want to perform:");
	            System.out.println("1. Roll out survey");
	            System.out.println("2. Delete item");
	            String action = userInput.readLine().trim();

	            JSONObject request = new JSONObject();
	            request.put("action", "discard_item");
	            request.put("item_id", item);
	            request.put("action_type", action);
	            out.println(request.toString());

	            String serverResponse2 = in.readLine();
	            System.out.println(serverResponse2);
	        }
	        else {
	            System.out.println("Invalid input. Please enter 'y' or 'n'.");
	        }
	    }
	}


	private void handleShowMenu() throws IOException {
		String serverResponse;
		while (!(serverResponse = in.readLine()).equals("END_OF_MENU")) {
			System.out.println(serverResponse);
		}
	}

	private void showVoting() throws IOException {
		String serverResponse;
		while (!(serverResponse = in.readLine()).equals("END_OF_MENU")) {
			System.out.println(serverResponse);
		}
	}

	private void viewRecommendation() throws IOException {
		System.out.println("Enter no of items you want to recommend: ");
		String noOfItems = userInput.readLine();

		String[] mealTypes = { "breakfast", "lunch", "dinner" };
		for (String mealType : mealTypes) {
			System.out.println("Viewing options for: " + mealType);

			JSONObject request = new JSONObject();
			request.put("action", "view_menu");
			request.put("meal_type", mealType);
			request.put("limit", noOfItems);
			out.println(request.toString());

			String response = in.readLine();
			JSONArray menuItems = new JSONArray(response);

			System.out.println("Available Items:");
			for (int i = 0; i < menuItems.length(); i++) {
				JSONObject item = menuItems.getJSONObject(i);
				System.out.println((i + 1) + ". " + item.getString("item_name") + " - Rating: "
						+ item.getDouble("rating") + " - Sentiments: " + item.getString("sentiments"));
			}

			System.out.println("Enter the names of the items you want to vote for, separated by commas:");
			String items = userInput.readLine();

			JSONObject voteRequest = new JSONObject();
			voteRequest.put("action", "vote");
			voteRequest.put("item_name", items);
			System.out.println(voteRequest + "vote");
			out.println(voteRequest.toString());
		}
	}
}
