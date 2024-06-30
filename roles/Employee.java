package roles;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.json.*;

public class Employee implements Role {
	private Connection connection;
	private BufferedReader in;
	private PrintWriter out;
	private final String username;

	public Employee(Connection connection, BufferedReader in, PrintWriter out, String username) {
		this.connection = connection;
		this.in = in;
		this.out = out;
		this.username = username;
	}

	@Override
	public void handleAction(String action) throws IOException, SQLException {
		switch (action) {
		case "1":
			giveFeedback();
			break;
		case "2":
			voteForTommorrowMenu();
			break;
		case "3":
			showMenu();
			break;
		case "4":
			updateProfile();
			break;
		case "5":
			viewNotification();
			break;
		case "exit":
			out.println("Exiting...");
			break;
		default:
			out.println("Invalid action.");
		}
	}

	private void viewNotification() throws IOException, SQLException {
		String checkNotificationQuery = "SELECT COUNT(*) AS count FROM notification WHERE DATE(current_date) = CURDATE()";
		try (PreparedStatement checkStatement = connection.prepareStatement(checkNotificationQuery);
				ResultSet checkResultSet = checkStatement.executeQuery()) {

			if (checkResultSet.next() && checkResultSet.getInt("count") > 0) {
				String query = "SELECT id, name FROM notification WHERE DATE(current_date) = CURDATE()";
				try (PreparedStatement statement = connection.prepareStatement(query);
						ResultSet resultSet = statement.executeQuery()) {

					StringBuilder resultBuilder = new StringBuilder();
					while (resultSet.next()) {
						String id = resultSet.getString("id");
						String typeName = resultSet.getString("name");
						resultBuilder.append(String.format("\n" + id + ". " + typeName));
					}
					out.println(resultBuilder.toString());
					out.println("END_OF_OPTIONS");

					// Wait for client input
					String clientInput = in.readLine();
					JSONObject request = new JSONObject(clientInput);

					String action = request.getString("action");
					if (action.equals("view_details")) {
						String notification_id = request.getString("notification_id");
						String detailsResponse = viewNotificationDetails(notification_id);
						out.println(detailsResponse); 
						out.println("END_OF_DETAILS");
					}
				}
			} else {
				out.println("You have no new notifications.");
				out.println("END_OF_OPTIONS");
			}
		}
	}

	private String viewNotificationDetails(String notificationId) throws SQLException, IOException {
		String query = "SELECT n.name AS notification_name, m.name AS item_name, m.price, m.availability_status "
				+ "FROM notification n " + "JOIN meal_item m ON n.item_id = m.menu_item_id " + "WHERE n.id = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, notificationId);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				String notificationName = resultSet.getString("notification_name");
				if (!notificationName.contains("Discard")) {
					String itemName = resultSet.getString("item_name");
					double price = resultSet.getDouble("price");
					boolean availabilityStatus = resultSet.getBoolean("availability_status");

					StringBuilder responseBuilder = new StringBuilder();
					responseBuilder.append(String.format("Notification: %s\n", notificationName));
					responseBuilder.append(String.format("Item Name: %s\n", itemName));
					responseBuilder.append(String.format("Price: %.2f\n", price));
					responseBuilder.append(
							String.format("Availability: %s\n", availabilityStatus ? "Available" : "Not Available"));

					return responseBuilder.toString();
				} else {
					String itemName = resultSet.getString("item_name");
					JSONObject request = new JSONObject();
					request.put("action", "Discard");
					request.put("item_name", itemName);
					out.println(request.toString());
				}
			} else {
				return "Notification details not found.";
			}
		}
		return null;
	}

	private void voteForTommorrowMenu() throws IOException, SQLException {
		for (int i = 0; i < 6; i++) {
			String clientInput = in.readLine();
			JSONObject request = new JSONObject(clientInput);

			String action = request.getString("action");
			if (action.equals("view_menu")) {
				String mealType = request.getString("meal_type");
				JSONArray menuItems = getMenuItems(mealType);
				out.println(menuItems.toString());
			} else if (action.equals("vote")) {
				String itemName = request.getString("item_name");
				String response = recordVote(itemName);
				out.println(response);
			}
		}
	}

	private JSONArray getMenuItems(String mealType) throws SQLException {
		String query = "SELECT Id, r.meal_item_id, mi.name AS meal_item_name, mt.type_name AS meal_type, mi.rating, mi.sentiment_score, mi.sentiments, r.date "
				+ "FROM rolloutmenu r JOIN meal_item mi ON r.meal_item_id = mi.menu_item_id JOIN meal_type mt ON mi.type_id = mt.type_id "
				+ "where mt.type_name = ? and date = CURDATE()";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, mealType);
		ResultSet resultSet = statement.executeQuery();
		JSONArray menuItems = new JSONArray();

		while (resultSet.next()) {
			JSONObject item = new JSONObject();
			item.put("item_id", resultSet.getInt("meal_item_id"));
			item.put("item_name", resultSet.getString("meal_item_name"));
			item.put("rating", resultSet.getDouble("rating"));
			item.put("sentiments", resultSet.getString("sentiment_score"));
			item.put("sentimentSet", resultSet.getString("sentiments"));
			menuItems.put(item);
		}
		return menuItems;
	}

	private String recordVote(String itemName) throws SQLException {
		String checkVoteQuery = "SELECT v.user_id, e.name AS employee_name, mi.name AS meal_item_name, v.vote_date "
				+ "FROM votes v " + "JOIN employee e ON v.user_id = e.employee_id "
				+ "JOIN meal_item mi ON v.item_id = mi.menu_item_id "
				+ "WHERE v.user_id = ? AND mi.name = ? AND DATE(v.vote_date) = CURDATE() "
				+ "AND (SELECT COUNT(*) FROM votes WHERE user_id = ? AND DATE(vote_date) = CURDATE()) > 0";
		PreparedStatement checkVoteStatement = connection.prepareStatement(checkVoteQuery);
		checkVoteStatement.setString(1, username);
		checkVoteStatement.setString(2, itemName);
		checkVoteStatement.setString(3, username);
		ResultSet rs = checkVoteStatement.executeQuery();

		if (rs.next()) {
			return "You have already voted for this meal type today.";
		} else {
			String getItemIdQuery = "SELECT menu_item_id FROM meal_item WHERE name = ?";
			PreparedStatement getItemIdStmt = connection.prepareStatement(getItemIdQuery);
			getItemIdStmt.setString(1, itemName);
			ResultSet itemResultSet = getItemIdStmt.executeQuery();

			if (itemResultSet.next()) {
				String itemId = itemResultSet.getString("menu_item_id");

				String voteQuery = "INSERT INTO votes (item_id, user_id) VALUES (?, ?)";
				PreparedStatement voteStatement = connection.prepareStatement(voteQuery);
				voteStatement.setString(1, itemId);
				voteStatement.setString(2, username);
				int rowsAffected = voteStatement.executeUpdate();

				String updateVoteCountQuery = "UPDATE rolloutmenu SET votes = votes + 1 WHERE meal_item_id = ?";
				PreparedStatement updateVoteCountStatement = connection.prepareStatement(updateVoteCountQuery);
				updateVoteCountStatement.setString(1, itemId);
				updateVoteCountStatement.executeUpdate();

				if (rowsAffected > 0) {
					return "Vote recorded successfully.";
				} else {
					return "Failed to record vote.";
				}
			} else {
				return "Item not found.";
			}
		}
	}

	private void giveFeedback() throws IOException, SQLException {
		String clientInput = in.readLine();
		JSONObject request = new JSONObject(clientInput);

		String action = request.getString("action");
		if (action.equals("feedback")) {
			String name = request.getString("name");
			String rating = request.getString("rating");
			String comment = request.getString("comment");

			String feedbackQuery = "SELECT f.user_id, mi.name AS meal_item_name, f.feedback_date " + "FROM feedback f "
					+ "JOIN employee e ON f.user_id = f.user_id "
					+ "JOIN meal_item mi ON f.menu_item_id = mi.menu_item_id "
					+ "WHERE f.user_id = ? AND mi.name = ? AND DATE(f.feedback_date) = CURDATE() "
					+ "AND (SELECT COUNT(*) FROM feedback WHERE user_id = ? AND DATE(feedback_date) = CURDATE()) > 0";
			PreparedStatement checkVoteStatement = connection.prepareStatement(feedbackQuery);
			checkVoteStatement.setString(1, username);
			checkVoteStatement.setString(2, name);
			checkVoteStatement.setString(3, username);
			ResultSet rs = checkVoteStatement.executeQuery();

			if (rs.next()) {
				out.println("You have already given feedback for this meal today.");
			} else {
				String getItemIdQuery = "SELECT menu_item_id FROM meal_item WHERE name = ?";
				PreparedStatement getItemIdStmt = connection.prepareStatement(getItemIdQuery);
				getItemIdStmt.setString(1, name);
				ResultSet itemResultSet = getItemIdStmt.executeQuery();

				if (itemResultSet.next()) {
					Map<Integer, Set<String>> finalmap = SentimentAnalyzer.calculateSentimentScore(comment);
					String itemId = itemResultSet.getString("menu_item_id");
					String insertQuery = "INSERT INTO feedback (menu_item_id, rating, comment, user_id, sentiment_score, sentiments) VALUES (?, ?, ?, ?, ?, ?)";
					try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
						statement.setString(1, itemId);
						statement.setString(2, rating);
						statement.setString(3, comment);
						statement.setString(4, username);
						for (Map.Entry<Integer, Set<String>> entry : finalmap.entrySet()) {
							Integer score = entry.getKey();
							Set<String> sentimentsSet = entry.getValue();
							StringJoiner joiner = new StringJoiner(", ");
							for (String sentiment : sentimentsSet) {
								joiner.add(sentiment);
							}
							String sentiments = joiner.toString();
							statement.setInt(5, score);
							statement.setString(6, sentiments);
						}
						statement.executeUpdate();
						out.println("You have given feedback");
					}
				}
			}
		}
	}

	private void showMenu() throws IOException, SQLException {
		System.out.println("showmenu");
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(String.format("%-20s%-20s%-15s%-40s\n", "Meal Item", "Meal Type", "Rating", "Sentiments"));

		String query = "SELECT mi.name, mt.type_name, mi.rating, mi.sentiment_score, mi.sentiments "
				+ "FROM meal_item mi " + "JOIN meal_type mt ON mi.type_id = mt.type_id " + "ORDER BY mi.menu_item_id";
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				String name = resultSet.getString("name");
				String mealType = resultSet.getString("type_name");
				int rating = resultSet.getInt("rating");
				String sentiments = resultSet.getString("sentiment_score");
				String sentimentSet = resultSet.getString("sentiments");
				resultBuilder.append(
						String.format("%-20s%-20s%-15d%-20s%-20s\n", name, mealType, rating, sentiments, sentimentSet));
			}
		}

		out.println(resultBuilder.toString());
		out.println("END_OF_MENU");
	}

	private void updateProfile() throws IOException, SQLException {
		
	}

	
}
