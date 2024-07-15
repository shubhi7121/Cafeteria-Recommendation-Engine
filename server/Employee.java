package server;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.json.*;

import constants.Constants;
import exceptions.ProfileUpdateException;
import exceptions.MenuItemNotFoundException;
import interfaces.Role;

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
    public void handleAction(String action) throws IOException, SQLException, ProfileUpdateException, MenuItemNotFoundException {
        switch (action) {
            case "1":
                processFeedback();
                break;
            case "2":
                processVote();
                break;
            case "3":
                showMenu();
                break;
            case "4":
                updateProfile();
                break;
            case "5":
                viewNotifications();
                break;
            case "exit":
                out.println("Exiting...");
                break;
            default:
                out.println("Invalid action.");
        }
    }

    private void viewNotifications() throws IOException, SQLException {
        if (hasNotifications()) {
            displayNotifications();
            String clientInput = in.readLine();
            JSONObject request = new JSONObject(clientInput);
            String action = request.getString("action");
            if ("view_details".equals(action)) {
                String notificationId = request.getString("notification_id");
                String detailsResponse = getNotificationDetails(notificationId);
                out.println(detailsResponse);
                out.println("END_OF_DETAILS");
            }
        } else {
            out.println("You have no new notifications.");
            out.println("END_OF_OPTIONS");
        }
    }

    private boolean hasNotifications() throws SQLException {
        String query = Constants.CHECK_NOTIFICATIONS_TODAY;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt("count") > 0;
        }
    }

    private void displayNotifications() throws SQLException {
        String query = Constants.DISPLAY_NOTIFICATIONS_TODAY;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            StringBuilder resultBuilder = new StringBuilder();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                resultBuilder.append(String.format("\n%s. %s", id, name));
            }
            out.println(resultBuilder.toString());
            out.println("END_OF_OPTIONS");
        }
    }

    private String getNotificationDetails(String notificationId) throws SQLException {
        String query = Constants.NOTIFICATION_DETAILS_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, notificationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return buildNotificationDetails(resultSet);
                } else {
                    return "Notification details not found.";
                }
            }
        }
    }

    private String buildNotificationDetails(ResultSet resultSet) throws SQLException {
        String notificationName = resultSet.getString("notification_name");
        if (notificationName.contains("Discard")) {
            String itemName = resultSet.getString("item_name");
            JSONObject discardRequest = new JSONObject();
            discardRequest.put("action", "Discard");
            discardRequest.put("item_name", itemName);
            out.println(discardRequest.toString());
            return null;
        } else {
            String itemName = resultSet.getString("item_name");
            double price = resultSet.getDouble("price");
            boolean availabilityStatus = resultSet.getBoolean("availability_status");

            return String.format("Notification: %s\nItem Name: %s\nPrice: %.2f\nAvailability: %s",
                    notificationName, itemName, price, availabilityStatus ? "Available" : "Not Available");
        }
    }

    private void processVote() throws IOException, SQLException {
        for (int i = 0; i < 6; i++) {
            String clientInput = in.readLine();
            JSONObject request = new JSONObject(clientInput);

            String action = request.getString("action");
            if ("view_menu".equals(action)) {
                String mealType = request.getString("meal_type");
                JSONArray menuItems = fetchMenuItems(mealType);
                out.println(menuItems.toString());
            } else if ("vote".equals(action)) {
                String itemName = request.getString("item_name");
                String response = recordVote(itemName);
                out.println(response);
            }
        }
    }

    private JSONArray fetchMenuItems(String mealType) throws SQLException {
        String query = Constants.FETCH_MENU_ITEMS_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, mealType);
            try (ResultSet resultSet = statement.executeQuery()) {
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
        }
    }

    private String recordVote(String itemName) throws SQLException {
        if (hasVotedToday(itemName)) {
            return "You have already voted for this meal type today.";
        } else {
            return processVote(itemName);
        }
    }

    private boolean hasVotedToday(String itemName) throws SQLException {
        String query = Constants.CHECK_VOTE_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, itemName);
            statement.setString(3, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String processVote(String itemName) throws SQLException {
        String itemId = fetchItemId(itemName);
        if (itemId == null) {
            return "Item not found.";
        }

        String insertVoteQuery = Constants.INSERT_VOTE_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(insertVoteQuery)) {
            statement.setString(1, itemId);
            statement.setString(2, username);
            int rowsAffected = statement.executeUpdate();
            updateVoteCount(itemId);

            return rowsAffected > 0 ? "Vote recorded successfully." : "Failed to record vote.";
        }
    }

    private String fetchItemId(String itemName) throws SQLException {
        String query = Constants.FETCH_ITEM_ID_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("menu_item_id") : null;
            }
        }
    }

    private void updateVoteCount(String itemId) throws SQLException {
        String query = Constants.UPDATE_VOTE_COUNT_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemId);
            statement.executeUpdate();
        }
    }

    private void processFeedback() throws IOException, SQLException, MenuItemNotFoundException {
        String clientInput = in.readLine();
        JSONObject request = new JSONObject(clientInput);

        String action = request.getString("action");
        if ("feedback".equals(action)) {
            submitFeedback(request);
        }
    }

    private void submitFeedback(JSONObject request) throws SQLException, MenuItemNotFoundException {
        String name = request.getString("name");
        String rating = request.getString("rating");
        String comment = request.getString("comment");

        if (hasGivenFeedbackToday(name)) {
            out.println("You have already given feedback for this meal today.");
        } else {
            saveFeedback(name, rating, comment);
            out.println("Feedback submitted successfully.");
        }
    }

    private boolean hasGivenFeedbackToday(String itemName) throws SQLException {
        String query = Constants.CHECK_FEEDBACK_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, itemName);
            statement.setString(3, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void saveFeedback(String itemName, String rating, String comment) throws SQLException, MenuItemNotFoundException {
        String itemId = fetchItemId(itemName);
        if (itemId == null) {
            throw new MenuItemNotFoundException("Menu item not found.");
        }

        Map<Integer, Set<String>> sentimentData = SentimentAnalyzer.calculateSentimentScore(comment);
        String insertFeedbackQuery = Constants.INSERT_FEEDBACK_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(insertFeedbackQuery)) {
            statement.setString(1, itemId);
            statement.setString(2, rating);
            statement.setString(3, comment);
            statement.setString(4, username);

            for (Map.Entry<Integer, Set<String>> entry : sentimentData.entrySet()) {
                Integer score = entry.getKey();
                Set<String> sentiments = entry.getValue();
                String sentimentString = String.join(", ", sentiments);
                statement.setInt(5, score);
                statement.setString(6, sentimentString);
                statement.executeUpdate();
            }
        }
    }

    private void showMenu() throws IOException, SQLException {
        String query = Constants.SHOW_MENU_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append(String.format("%-20s%-20s%-15s%-40s\n", "Meal Item", "Meal Type", "Rating", "Sentiments"));

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String mealType = resultSet.getString("type_name");
                double rating = resultSet.getDouble("rating");
                String sentiments = resultSet.getString("sentiments");

                resultBuilder.append(String.format("%-20s%-20s%-15.2f%-40s\n", name, mealType, rating, sentiments));
            }

            out.println(resultBuilder.toString());
            out.println("END_OF_MENU");
        }
    }

    private void updateProfile() throws IOException, SQLException, ProfileUpdateException {
        String foodCategory = getSelectionFromUser("foodcategory");
        String spiceLevel = getSelectionFromUser("SpiceLevel");
        String foodType = getSelectionFromUser("foodtypes");
        String sweetTooth = getSweetToothPreference();

        String updateProfileQuery = Constants.UPDATE_PROFILE_QUERY;
        try (PreparedStatement statement = connection.prepareStatement(updateProfileQuery)) {
            statement.setString(1, foodCategory);
            statement.setString(2, spiceLevel);
            statement.setString(3, foodType);
            statement.setString(4, sweetTooth);
            statement.setString(5, username);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Profile updated successfully.");
            } else {
                out.println("Failed to update profile.");
            }
        } catch (SQLException e) {
            throw new ProfileUpdateException("Error updating profile.", e);
        }
    }

    private String getSelectionFromUser(String tableName) throws SQLException, IOException {
        displayOptions(tableName);
        String clientInput = in.readLine();
        JSONObject request = new JSONObject(clientInput);
        return request.getString("selection");
    }

    private String getSweetToothPreference() throws IOException {
        String clientInput = in.readLine();
        JSONObject request = new JSONObject(clientInput);
        return request.getString("selection");
    }

    private void displayOptions(String tableName) throws SQLException, IOException {
        StringBuilder resultBuilder = new StringBuilder();
        String query = String.format(Constants.SELECT_OPTIONS_QUERY, tableName);

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String typeName = resultSet.getString("type_name");
                resultBuilder.append(String.format("\n%s. %s", id, typeName));
            }
            out.println(resultBuilder.toString());
            out.println("END_OF_OPTIONS");
        }
    }
}
