package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import constants.Constants;
import exceptions.MenuItemNotFoundException;
import exceptions.NotificationException;
import interfaces.Role;

public class Chef implements Role {
    private final BufferedReader inputReader;
    private final PrintWriter outputWriter;
    private final Connection connection;
    private final String username;

    public Chef(Connection connection, BufferedReader inputReader, PrintWriter outputWriter, String username) {
        this.connection = connection;
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
        this.username = username;
    }

    @Override
    public void handleAction(String action) throws IOException, SQLException, NotificationException, MenuItemNotFoundException {
        switch (action) {
            case "1":
                handleRecommendations();
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
                outputWriter.println("Exiting...");
                break;
            default:
                outputWriter.println("Invalid action.");
        }
    }

    private void handleRecommendations() throws SQLException, IOException, JSONException, MenuItemNotFoundException {
        RecommendationEngine recommendationEngine = new RecommendationEngine(connection, outputWriter);
        recommendationEngine.processRecommendations();

        for (int i = 0; i < 6; i++) {
            String clientInput = inputReader.readLine();
            JSONObject request = new JSONObject(clientInput);

            String action = request.getString("action");
            if (action.equals("view_menu")) {
                String mealType = request.getString("meal_type");
                int limit = request.getInt("limit");
                JSONArray menuItems = fetchMenuItems(mealType, limit);
                outputWriter.println(menuItems.toString());
            } else if (action.equals("vote")) {
                String itemNames = request.getString("item_name");
                recordRollOutMenu(itemNames);
            }
        }
    }

    private void recordRollOutMenu(String itemNames) throws SQLException, MenuItemNotFoundException {
        String[] items = itemNames.split(",");
        for (String itemName : items) {
            int itemId = getMenuItemIdByName(itemName.trim());
            if (itemId != -1) {
                try (PreparedStatement statement = connection.prepareStatement(Constants.INSERT_ROLLOUT_MENU)) {
                    statement.setInt(1, 0); // Default votes
                    statement.setInt(2, itemId);
                    statement.executeUpdate();
                }
            }
        }
    }

    private int getMenuItemIdByName(String itemName) throws SQLException, MenuItemNotFoundException {
        try (PreparedStatement statement = connection.prepareStatement(Constants.SELECT_MENU_ITEM_ID_BY_NAME)) {
            statement.setString(1, itemName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("menu_item_id");
            } else {
                throw new MenuItemNotFoundException("Menu item not found for name: " + itemName);
            }
        }
    }

    private JSONArray fetchMenuItems(String mealType, int limit) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Constants.SELECT_MENU_ITEMS)) {
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
    }

    private void showMenu() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-10s%-20s\n",
                "ID", "Meal Item", "Meal Type", "Price", "Available", "Rating", "Sentiment_score"));

        try (PreparedStatement statement = connection.prepareStatement(Constants.SELECT_MENU_ITEMS_FULL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("menu_item_id");
                String name = resultSet.getString("name");
                String mealType = resultSet.getString("type_name");
                double price = resultSet.getDouble("price");
                boolean availabilityStatus = resultSet.getBoolean("availability_status");
                int rating = resultSet.getInt("rating");
                String sentiments = resultSet.getString("sentiment_score");

                resultBuilder.append(String.format("%-5d%-20s%-15s%-10.2f%-10b%-10d%-20s\n",
                        id, name, mealType, price, availabilityStatus, rating, sentiments));
            }
        }
        outputWriter.println(resultBuilder.toString());
        outputWriter.println("END_OF_MENU");
    }

    private void showVoting() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(String.format("%-5s%-20s%-15s%-10s%-10s%-20s\n",
                "ID", "Meal Item", "Meal Type", "Votes", "Rating", "Sentiments"));

        try (PreparedStatement statement = connection.prepareStatement(Constants.SELECT_VOTING_INFO);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String mealType = resultSet.getString("type_name");
                int votes = resultSet.getInt("votes");
                int rating = resultSet.getInt("rating");
                String sentiments = resultSet.getString("sentiments");

                resultBuilder.append(String.format("%-5d%-20s%-15s%-10d%-10d%-20s\n",
                        id, name, mealType, votes, rating, sentiments));
            }
        }
        outputWriter.println(resultBuilder.toString());
        outputWriter.println("END_OF_MENU");
    }

    private void discardList() throws SQLException, IOException, NotificationException, MenuItemNotFoundException {
        DiscardMenu discardMenu = new DiscardMenu(connection, inputReader, outputWriter, username);
        discardMenu.discardMenuList();
    }
}
