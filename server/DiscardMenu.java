package server;

import java.io.*;
import java.sql.*;

import org.json.JSONObject;

import constants.Constants;
import exceptions.MenuItemNotFoundException;
import exceptions.NotificationException;
import exceptions.SQLCustomException;

public class DiscardMenu {
    private final Connection connection;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String username;

    public DiscardMenu(Connection connection, BufferedReader in, PrintWriter out, String username) {
        this.connection = connection;
        this.in = in;
        this.out = out;
        this.username = username;
    }

    public void discardMenuList() throws SQLException, IOException, NotificationException, MenuItemNotFoundException {
        try {
            if (isDiscardListAvailable()) {
                displayDiscardList();
                processDiscardListActions();
            } else {
                out.println("No items available in discard list.");
            }
        } catch (SQLException e) {
            throw new SQLCustomException("Error handling discard menu list", e);
        }
    }

    private boolean isDiscardListAvailable() throws SQLException {
        String query = Constants.CHECK_DISCARD_LIST;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt("count") > 0;
        }
    }

    private void displayDiscardList() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n", "ID", "Meal Item", "Meal Type", "Price",
                "Rating", "Sentiments"));

        String query = Constants.DISPLAY_DISCARD_LIST;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                appendItemDetails(resultBuilder, resultSet);
            }
        }
        out.println(resultBuilder.toString());
        out.println("END_OF_MENU");
    }

    private void appendItemDetails(StringBuilder resultBuilder, ResultSet resultSet) throws SQLException {
        int itemId = resultSet.getInt("menu_item_id");
        String itemName = resultSet.getString("name");
        String mealType = resultSet.getString("type_name");
        double price = resultSet.getDouble("price");
        int rating = resultSet.getInt("rating");
        String sentiments = resultSet.getString("sentiments");
        resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n", itemId, itemName, mealType, price, rating, sentiments));
    }

    private void processDiscardListActions()
            throws IOException, SQLException, NotificationException, MenuItemNotFoundException {
        String jsonString = in.readLine();
        JSONObject receivedJson = new JSONObject(jsonString);
        int count = receivedJson.getInt("count");

        for (int i = 0; i < count; i++) {
            handleUserResponse();
        }
    }

    private void handleUserResponse() throws IOException, SQLException, NotificationException, MenuItemNotFoundException {
        String responseString = in.readLine();
        JSONObject responseJson = new JSONObject(responseString);
        String actionResponse = responseJson.getString("response");

        if ("n".equalsIgnoreCase(actionResponse)) {
            return;
        } else if ("y".equalsIgnoreCase(actionResponse)) {
            handleUserAction();
        } else {
            out.println("Invalid response.");
        }
    }

    private void handleUserAction() throws IOException, SQLException, NotificationException, MenuItemNotFoundException {
        String clientInput = in.readLine();
        JSONObject request = new JSONObject(clientInput);
        String action = request.getString("action");
        String itemId = request.getString("item_id");
        String actionType = request.getString("action_type");

        if (isItemAvailable(itemId)) {
            processAction(itemId, action, actionType);
        } else {
            out.println("Invalid Id.");
        }
    }

    private boolean isItemAvailable(String itemId) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM rolloutmenu WHERE meal_item_id = ? AND Date = CURDATE()";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("count") > 0;
            }
        }
    }

    private void processAction(String itemId, String action, String actionType)
            throws SQLException, NotificationException, MenuItemNotFoundException {
        if ("discard_item".equalsIgnoreCase(action)) {
            handleDiscardAction(itemId, actionType);
        } else {
            out.println("Invalid action.");
        }
    }

    private void handleDiscardAction(String itemId, String actionType)
            throws SQLException, NotificationException, MenuItemNotFoundException {
        switch (actionType) {
            case "1":
                rollOutSurvey(itemId);
                break;
            case "2":
                deleteItem(itemId);
                break;
            default:
                out.println("Invalid action type.");
                break;
        }
    }

    private void rollOutSurvey(String itemId) throws SQLException, NotificationException {
        String query = Constants.SELECT_MENU_ITEM_NAME_BY_ID;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String itemName = resultSet.getString("name");
                sendSurveyNotification(itemId, itemName);
            }
        }
    }

    private void sendSurveyNotification(String itemId, String itemName) throws SQLException, NotificationException {
        String query = Constants.INSERT_NOTIFICATION;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "Discard Survey " + itemName);
            statement.setString(2, username);
            statement.setInt(3, Integer.parseInt(itemId));
            statement.executeUpdate();
            out.println("Survey rolled out successfully.");
        } catch (SQLException e) {
            throw new NotificationException("Failed to roll out survey", e);
        }
    }

    private void deleteItem(String itemId) throws SQLException, MenuItemNotFoundException {
        String query = Constants.DELETE_MENU_ITEM_BY_ID;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(itemId));
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Item deleted successfully.");
            } else {
                throw new MenuItemNotFoundException("Item not found for id: " + itemId);
            }
        }
    }
}
