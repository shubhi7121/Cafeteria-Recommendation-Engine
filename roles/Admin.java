package roles;

import java.io.*;
import java.sql.*;
import org.json.JSONObject;

public class Admin implements Role {
	private Connection connection;
	private BufferedReader in;
	private PrintWriter out;
	private String username;

	public Admin(Connection connection, BufferedReader in, PrintWriter out, String username) {
		this.connection = connection;
		this.in = in;
		this.out = out;
		this.username = username;
	}

	@Override
	public void handleAction(String action) throws IOException, SQLException {
		switch (action) {
		case "1":
			addMenuItem();
			break;
		case "2":
			updateMenuItem();
			break;
		case "3":
			System.out.println("in 3");
			deleteMenuItem();
			break;
		case "4":
			showMenu();
			break;
		case "5":
			loginActivity();
			break;
		case "6":
			discardMenuList();
			break;
		case "exit":
			break;
		default:
			out.println("Invalid action.");
		}

	}

	private void addMenuItem() throws IOException, SQLException {
		String jsonString = in.readLine();
		JSONObject receivedJson = new JSONObject(jsonString);

		String name = receivedJson.getString("name");
		String priceStr = receivedJson.getString("price");
		String mealType = receivedJson.getString("mealType");
		String availabilityStatus = receivedJson.getString("availabilityStatus");

		double price = Double.parseDouble(priceStr);
		int meal_type = Integer.parseInt(mealType);
		boolean availability_Status = Boolean.parseBoolean(availabilityStatus);

		String insertQuery = "INSERT INTO meal_item (name, price, type_id, availability_Status) VALUES (?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
			statement.setString(1, name);
			statement.setDouble(2, price);
			statement.setInt(3, meal_type);
			statement.setBoolean(4, availability_Status);
			statement.executeUpdate();
			out.println("Menu item added successfully.");
		}

		String getItemIdQuery = "SELECT menu_item_id FROM meal_item WHERE name = ?";
		try (PreparedStatement getItemIdStmt = connection.prepareStatement(getItemIdQuery)) {
			getItemIdStmt.setString(1, name);
			ResultSet itemResultSet = getItemIdStmt.executeQuery();

			if (itemResultSet.next()) {
				int itemId = itemResultSet.getInt("menu_item_id");

				String insertNotificationQuery = "INSERT INTO notification (name, created_by, item_id) VALUES (?, ?, ?)";
				try (PreparedStatement notificationStmt = connection.prepareStatement(insertNotificationQuery)) {
					notificationStmt.setString(1, "A new item added - " + name);
					notificationStmt.setString(2, username);
					notificationStmt.setInt(3, itemId);
					notificationStmt.executeUpdate();
					out.println("Notification sent.");
				}
			}
		}
	}

	private void updateMenuItem() throws IOException, SQLException {
		String jsonString = in.readLine();
		JSONObject receivedJson = new JSONObject(jsonString);

		String name = receivedJson.getString("name");
		String priceStr = receivedJson.getString("price");
		String availabilityStatus = receivedJson.getString("availabilityStatus");
		double price = Double.parseDouble(priceStr);
		boolean availability_Status = Boolean.parseBoolean(availabilityStatus);

		String insertQuery = "UPDATE meal_item SET price = ?, availability_status = ? WHERE name = ?";
		try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
			statement.setDouble(1, price);
			statement.setBoolean(2, availability_Status);
			statement.setString(3, name);
			statement.executeUpdate();
			out.println("Menu item updated successfully.");
		}

		String getItemIdQuery = "SELECT menu_item_id FROM meal_item WHERE name = ?";
		try (PreparedStatement getItemIdStmt = connection.prepareStatement(getItemIdQuery)) {
			getItemIdStmt.setString(1, name);
			ResultSet itemResultSet = getItemIdStmt.executeQuery();

			if (itemResultSet.next()) {
				int itemId = itemResultSet.getInt("menu_item_id");

				String insertNotificationQuery = "INSERT INTO notification (name, created_by, item_id) VALUES (?, ?, ?)";
				try (PreparedStatement notificationStmt = connection.prepareStatement(insertNotificationQuery)) {
					notificationStmt.setString(1, name + " has something new");
					notificationStmt.setString(2, username);
					notificationStmt.setInt(3, itemId);
					notificationStmt.executeUpdate();
					out.println("Notification sent.");
				}
			}
		}
	}

	private void deleteMenuItem() throws IOException, SQLException {
		String jsonString = in.readLine();
		JSONObject receivedJson = new JSONObject(jsonString);

		String name = receivedJson.getString("name");

		String deleteQuery = "DELETE FROM meal_item WHERE name = ?";
		try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
			statement.setString(1, name);
			statement.executeUpdate();
			out.println("Menu item deleted successfully.");
		}
	}

	private void showMenu() throws IOException, SQLException {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(
				String.format("%-10s%-20s%-15s%-10s%-15s\n", "ID", "Meal Item", "Meal Type", "Price", "Availability"));
		String query = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price, mi.availability_status "
				+ "FROM meal_item mi " + "JOIN meal_type mt ON mi.type_id = mt.type_id ORDER BY mi.menu_item_id";
		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				int id = resultSet.getInt("menu_item_id");
				String name = resultSet.getString("name");
				String mealType = resultSet.getString("mt.type_name");
				Double price = resultSet.getDouble("price");
				Boolean availaibility_status = resultSet.getBoolean("availability_status");
				resultBuilder.append(String.format("%-10d%-20s%-15s%-10.2f%-10b%n", id, name, mealType, price,
						availaibility_status));
			}
		}
		out.println(resultBuilder.toString());
		out.println("END_OF_MENU");
	}

	private void loginActivity() throws IOException, SQLException {
		
	}

	private void discardMenuList() throws IOException {
		
	}
}
