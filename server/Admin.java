package server;

import java.io.*;
import java.sql.*;

import org.json.JSONObject;

import constants.Constants;
import exceptions.MenuItemNotFoundException;
import exceptions.NotificationException;
import exceptions.SQLCustomException;
import interfaces.Role;

public class Admin implements Role {
    private final Connection connection;
    private final BufferedReader inputReader;
    private final PrintWriter outputWriter;
    private final String username;

    public Admin(Connection connection, BufferedReader inputReader, PrintWriter outputWriter, String username) {
        this.connection = connection;
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
        this.username = username;
    }

    @Override
    public void handleAction(String action) throws IOException, SQLException, NotificationException, MenuItemNotFoundException {
        switch (action) {
            case "1":
                addMenuItem();
                break;
            case "2":
                updateMenuItem();
                break;
            case "3":
                deleteMenuItem();
                break;
            case "4":
                showMenu();
                break;
            case "5":
                displayLoginActivity();
                break;
            case "6":
                discardMenuList();
                break;
            case "exit":
                break;
            default:
                outputWriter.println("Invalid action.");
        }
    }

    private void addMenuItem() throws IOException, SQLCustomException {
        JSONObject receivedJson = new JSONObject(inputReader.readLine());
        String name = receivedJson.getString("name");
        double price = receivedJson.getDouble("price");
        int mealType = receivedJson.getInt("mealType");
        int availabilityStatus = receivedJson.getInt("availabilityStatus");
        int sweetTooth = receivedJson.getInt("sweetTooth");
        int foodType = receivedJson.getInt("foodType");
        int spiceLevel = receivedJson.getInt("spiceLevel");
        int foodCategory = receivedJson.getInt("foodCategory");

        try {
            insertMenuItem(name, price, mealType, availabilityStatus, foodCategory, spiceLevel, foodType, sweetTooth);
            int itemId = getMenuItemIdByName(name);
            sendNotification(name, itemId);
        } catch (SQLException | NotificationException | MenuItemNotFoundException e) {
            throw new SQLCustomException("Error adding menu item", e);
        }
    }

    private void insertMenuItem(String name, double price, int mealType, int availabilityStatus, int foodCategory, int spiceLevel, int foodType, int sweetTooth) throws SQLException {
        String query = Constants.INSERT_MENU_ITEM;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, mealType);
            statement.setInt(4, availabilityStatus);
            statement.setInt(5, foodCategory);
            statement.setInt(6, spiceLevel);
            statement.setInt(7, foodType);
            statement.setInt(8, sweetTooth);
            statement.executeUpdate();
            outputWriter.println("Menu item added successfully.");
        }
    }

    private int getMenuItemIdByName(String name) throws SQLException, MenuItemNotFoundException {
        String query = Constants.SELECT_MENU_ITEM_ID_BY_NAME;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("menu_item_id");
            } else {
                throw new MenuItemNotFoundException("Menu item not found for name: " + name);
            }
        }
    }

    private void sendNotification(String name, int itemId) throws SQLException, NotificationException {
        String query = Constants.INSERT_NOTIFICATION;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "A new item added - " + name);
            statement.setString(2, username);
            statement.setInt(3, itemId);
            statement.executeUpdate();
            outputWriter.println("Notification sent.");
        } catch (SQLException e) {
            throw new NotificationException("Failed to send notification", e);
        }
    }

    private void updateMenuItem() throws IOException, SQLCustomException {
        JSONObject receivedJson = new JSONObject(inputReader.readLine());
        String name = receivedJson.getString("name");
        double price = receivedJson.getDouble("price");
        int availabilityStatus = receivedJson.getInt("availabilityStatus");

        try {
            updateMenuItemInDatabase(name, price, availabilityStatus);
            int itemId = getMenuItemIdByName(name);
            sendNotification(name + " has something new", itemId);
        } catch (SQLException | NotificationException | MenuItemNotFoundException e) {
            throw new SQLCustomException("Error updating menu item", e);
        }
    }

    private void updateMenuItemInDatabase(String name, double price, int availabilityStatus) throws SQLException {
        String query = Constants.UPDATE_MENU_ITEM;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, price);
            statement.setInt(2, availabilityStatus);
            statement.setString(3, name);
            statement.executeUpdate();
            outputWriter.println("Menu item updated successfully.");
        }
    }

    private void deleteMenuItem() throws IOException, SQLCustomException {
        JSONObject receivedJson = new JSONObject(inputReader.readLine());
        String name = receivedJson.getString("name");

        try {
            deleteMenuItemFromDatabase(name);
        } catch (SQLException e) {
            throw new SQLCustomException("Error deleting menu item", e);
        }
    }

    private void deleteMenuItemFromDatabase(String name) throws SQLException {
        String query = Constants.DELETE_MENU_ITEM;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.executeUpdate();
            outputWriter.println("Menu item deleted successfully.");
        }
    }

    private void showMenu() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(String.format("%-10s%-20s%-15s%-10s%-15s\n", "ID", "Meal Item", "Meal Type", "Price", "Availability"));
        String query = Constants.SHOW_MENU;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("menu_item_id");
                String name = resultSet.getString("name");
                String mealType = resultSet.getString("type_name");
                double price = resultSet.getDouble("price");
                int availabilityStatus = resultSet.getInt("availability_status");
                resultBuilder.append(String.format("%-10d%-20s%-15s%-10.2f%-10b%n", id, name, mealType, price, availabilityStatus));
            }
        }
        outputWriter.println(resultBuilder.toString());
        outputWriter.println("END_OF_MENU");
    }

    private void displayLoginActivity() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(String.format("%-30s%-25s%-25s\n", "Email", "LogIn Time", "LogOut Time"));
        String query = Constants.LOGIN_ACTIVITY;
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String emailId = resultSet.getString("email_id");
                String loginTime = resultSet.getString("login_time");
                String logoutTime = resultSet.getString("logout_time");
                resultBuilder.append(String.format("%-30s%-25s%-25s\n", emailId, loginTime, logoutTime));
            }
        }
        outputWriter.println(resultBuilder.toString());
        outputWriter.println("END_OF_MENU");
    }

    private void discardMenuList() throws SQLException, IOException, NotificationException, MenuItemNotFoundException {
        DiscardMenu discardMenu = new DiscardMenu(connection, inputReader, outputWriter, username);
        discardMenu.discardMenuList();
    }
}
