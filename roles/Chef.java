package roles;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.*;

import server.RecommendationEngine;

public class Chef implements Role {
	private BufferedReader in;
	private PrintWriter out;
	private Connection connection;
	private final String username;

	public Chef(Connection connection, BufferedReader in, PrintWriter out, String username) {
		this.in = in;
		this.out = out;
		this.connection = connection;
		this.username = username;
	}

	@Override
	public void handleAction(String action) throws IOException, SQLException {
		switch (action) {
		case "1":
			viewRecommendations();
			break;
		case "2":
			showMenu();
			break;
		case "3":
			showVoting();
			break;
		case "4":
			discardList();
			break;
		case "exit":
			out.println("Exiting...");
			break;
		default:
			out.println("Invalid action.");
		}
	}

	private void viewRecommendations() throws SQLException, IOException {
		RecommendationEngine recommendationEngine = new RecommendationEngine(connection, out);
		recommendationEngine.viewRecommendations();
		for (int i = 0; i < 6; i++) {
			String clientInput = in.readLine();
			JSONObject request = new JSONObject(clientInput);

			String action = request.getString("action");
			if (action.equals("view_menu")) {
				String mealType = request.getString("meal_type");
				int limit = request.getInt("limit");
				JSONArray menuItems = getMenuItems(mealType, limit);
				out.println(menuItems.toString());
			} else if (action.equals("vote")) {
				String itemNames = request.getString("item_name");
				recordRollOutMenu(itemNames);
			}
		}
	}

	private void recordRollOutMenu(String itemNames) throws SQLException {
		String[] items = itemNames.split(",");
		for (String itemName : items) {
			String getItemIdQuery = "SELECT menu_item_id FROM meal_item WHERE name = ?";
			PreparedStatement getItemIdStmt = connection.prepareStatement(getItemIdQuery);
			getItemIdStmt.setString(1, itemName.trim());
			ResultSet itemResultSet = getItemIdStmt.executeQuery();

			if (itemResultSet.next()) {
				String itemId = itemResultSet.getString("menu_item_id");
				String updateVoteQuery = "Insert rolloutmenu SET votes = 0, meal_item_id = ? ";
				try (PreparedStatement statement = connection.prepareStatement(updateVoteQuery)) {
					statement.setString(1, itemId);
					statement.executeUpdate();
				}
			}
		}
	}

	private JSONArray getMenuItems(String mealType, int limit) throws SQLException {
		String query = "SELECT mi.menu_item_id, mi.name AS meal_item_name, mt.type_name AS meal_type, mi.rating, mi.sentiments "
				+ "FROM meal_item mi " + "JOIN meal_type mt ON mi.type_id = mt.type_id " + "where mt.type_name = ? "
				+ "limit ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, mealType);
		statement.setInt(2, limit);
		ResultSet resultSet = statement.executeQuery();
		JSONArray menuItems = new JSONArray();

		while (resultSet.next()) {
			JSONObject item = new JSONObject();
			item.put("item_id", resultSet.getInt("menu_item_id"));
			item.put("item_name", resultSet.getString("meal_item_name"));
			item.put("rating", resultSet.getDouble("rating"));
			item.put("sentiments", resultSet.getString("sentiments"));
			menuItems.put(item);
		}
		return menuItems;
	}

	private void showMenu() throws SQLException {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-10s%-20s\n", "ID", "Meal Item", "Meal Type",
				"Price", "Availabile", "Rating", "Sentiment_score"));
		String query = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price, mi.availability_status, mi.rating, mi.sentiment_score "
				+ "FROM meal_item mi " + "JOIN meal_type mt ON mi.type_id = mt.type_id ORDER BY mi.menu_item_id";
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				int id = resultSet.getInt("menu_item_id");
				String name = resultSet.getString("name");
				String mealType = resultSet.getString("mt.type_name");
				Double price = resultSet.getDouble("price");
				Boolean availaibility_status = resultSet.getBoolean("availability_status");
				int rating = resultSet.getInt("rating");
				String sentiments = resultSet.getString("sentiment_score");
				resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-10s%-20s\n", id, name, mealType, price,
						availaibility_status, rating, sentiments));
			}
		}
		out.println(resultBuilder.toString());
		out.println("END_OF_MENU");
	}

	private void showVoting() throws SQLException {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n", "ID", "Meal Item", "Meal Type", "Votes",
				"Rating", "Sentiments"));
		String query = "SELECT rm.id, mi.name, mt.type_name, rm.votes, mi.rating, mi.sentiments "
				+ "FROM rolloutmenu rm " + "JOIN meal_item mi ON rm.meal_item_id = mi.menu_item_id "
				+ "JOIN meal_type mt ON mi.type_id = mt.type_id ORDER BY rm.id";
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				String mealType = resultSet.getString("mt.type_name");
				int votes = resultSet.getInt("votes");
				int rating = resultSet.getInt("rating");
				String sentiments = resultSet.getString("sentiments");
				resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n", id, name, mealType, votes, rating,
						sentiments));
			}
		}
		out.println(resultBuilder.toString());
		out.println("END_OF_MENU");
	}

	private void discardList() throws IOException {
		try {
			String checkQuery = "SELECT COUNT(*) AS count FROM discard_items WHERE discarded_date = CURDATE()";
			try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
					ResultSet resultSet = checkStmt.executeQuery()) {

				if (resultSet.next() && resultSet.getInt("count") > 0) {
					StringBuilder resultBuilder = new StringBuilder();
					resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n", "ID", "Meal Item",
							"Meal Type", "Price", "Rating", "Sentiments"));
					String displayQuery = "SELECT d.menu_item_id, m.name, mt.type_name, m.price, m.rating, m.sentiments "
							+ "FROM discard_items d " + "JOIN meal_item m ON d.menu_item_id = m.menu_item_id "
							+ "JOIN meal_type mt ON m.type_id = mt.type_id ";
					try (PreparedStatement displayStmt = connection.prepareStatement(displayQuery);
							ResultSet displayResultSet = displayStmt.executeQuery()) {

						while (displayResultSet.next()) {
							int itemId = displayResultSet.getInt("menu_item_id");
							String itemName = displayResultSet.getString("name");
							String availabilityStatus = displayResultSet.getString("mt.type_name");
							double price = displayResultSet.getDouble("price");
							int rating = displayResultSet.getInt("rating");
							String sentiments = displayResultSet.getString("sentiments");
							resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n", itemId, itemName,
									availabilityStatus, price, rating, sentiments));
						}
						out.println(resultBuilder.toString());
						out.println("END_OF_MENU");
					}
				} else {
					out.println("No items available in discard list.");
				}
			}

			String clientInput;
			while ((clientInput = in.readLine()) != null) {
				JSONObject request = new JSONObject(clientInput);
				String action = request.getString("action");

				if (action.equals("discard_item")) {
					String itemId = request.getString("item_id");
					String actionType = request.getString("action_type");
					if (actionType.equals("1")) { // Roll out survey
						System.out.println("roll out ");
						rollOutSurvey(itemId);
					} else if (actionType.equals("2")) { // Delete item
						deleteItem(itemId);
					} else {
						out.println("Invalid action type.");
					}
				} else {
					out.println("Invalid action.");
				}
			}
		} catch (SQLException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	private void rollOutSurvey(String itemId) {
		String query = "SELECT name FROM meal_item WHERE menu_item_id = ?";
		try (PreparedStatement statement2 = connection.prepareStatement(query)) {
			statement2.setString(1, itemId);
			ResultSet resultSet = statement2.executeQuery();

			if (resultSet.next()) {
				String itemName = resultSet.getString("name");
				String surveyQuery = "INSERT INTO notification (item_id, name, created_by) VALUES (?, ?, ?)";
				try (PreparedStatement statement = connection.prepareStatement(surveyQuery)) {
					statement.setString(1, itemId);
					statement.setString(2, "Discard " + itemName);
					statement.setString(3, username);
					statement.executeUpdate();
					out.println("Survey rolled out successfully.");

				} catch (SQLException e) {
					e.printStackTrace();
					out.println("Error rolling out survey.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			out.println("Error rolling out survey.");
		}
	}

	private void deleteItem(String itemId) {
		String deleteQuery = "DELETE FROM meal_item WHERE menu_item_id = ?";
		try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
			statement.setString(1, itemId);
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected > 0) {
				out.println("Item deleted successfully.");
			} else {
				out.println("Item not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			out.println("Error deleting item.");
		}

		out.println("Delete item functionality not implemented.");
	}

}
