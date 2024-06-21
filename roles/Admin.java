package roles;

import java.io.*;
import java.sql.*;

public class Admin implements Role {
    private Connection connection;
    private BufferedReader in;
    private PrintWriter out;

    public Admin(Connection connection, BufferedReader in, PrintWriter out) {
        this.connection = connection;
        this.in = in;
        this.out = out;
    }

    @Override
    public void showOptions() throws IOException {
        out.println("Admin actions: 1. Add Menu Item 2. Update Menu Item 3. Delete Menu Item 4. Show Menu Type exit to logout");
    }

    @Override
    public void handleAction(String action) throws IOException, SQLException {
    	System.out.println("in handle action");
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
            case "exit":
                out.println("Exiting...");
                break;
            default:
                out.println("Invalid action.");
        }
    }

    private void addMenuItem() throws IOException, SQLException {
    	String name = in.readLine();
        System.out.println("Received name: " + name);

        String priceStr = in.readLine();
        double price = Double.parseDouble(priceStr);
        System.out.println("Received price: " + price);
         
        String mealType = in.readLine();
	    int meal_type = Integer.parseInt(mealType);
	    System.out.println("Received type: " + mealType);
	        
	    String AvailabilityStatus = in.readLine();
	    boolean Availability_Status = Boolean.parseBoolean(AvailabilityStatus);
	    System.out.println("Received status: " + Availability_Status);                           

        String insertQuery = "INSERT INTO meal_item (name, price, type_id, availability_Status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, meal_type);
            statement.setBoolean(4, Availability_Status);
            statement.executeUpdate();
            out.println("Menu item added successfully.");
        }
    }

    private void updateMenuItem() throws IOException, SQLException {
    	String id = in.readLine();
	    int Id = Integer.parseInt(id);
	    System.out.println("Received type: " + Id);

        String priceStr = in.readLine();
        double price = Double.parseDouble(priceStr);
        System.out.println("Received price: " + price);
	        
	    String AvailabilityStatus = in.readLine();
	    boolean Availability_Status = Boolean.parseBoolean(AvailabilityStatus);
	    System.out.println("Received status: " + Availability_Status);                           

        String insertQuery = "UPDATE meal_item SET price = ?, availability_status = ? WHERE menu_item_id = ?"; //"UPDATE meal_item SET price = " + price + ", availability_Status = " + Availability_Status + " where menu_item_id = " + Id;
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setDouble(1, price);
            statement.setBoolean(2, Availability_Status);
            statement.setInt(3, Id);
            statement.executeUpdate();
            out.println("Menu item updated successfully.");
        }
    }

    private void deleteMenuItem() throws IOException, SQLException {
         
        String id = in.readLine();
	    int Id = Integer.parseInt(id);
	    System.out.println("Received type: " + Id);
	                                 

        String deleteQuery = "DELETE FROM meal_item WHERE menu_item_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, Id);
            statement.executeUpdate();
            out.println("Menu item deleted successfully.");
        }
    }

    private void showMenu() throws IOException, SQLException {
    	StringBuilder resultBuilder = new StringBuilder();
    	resultBuilder.append(String.format("%-10s%-20s%-15s%-10s%-15s\n", 
				"ID", "Meal Item", "Meal Type", "Price", "Availaibility Status"));
    	String query = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price, mi.availability_status " +
    	        "FROM meal_item mi " +
    	        "JOIN meal_type mt ON mi.type_id = mt.type_id ";
    	try (PreparedStatement statement = connection.prepareStatement(query);
   	         ResultSet resultSet = statement.executeQuery()) {
   	
   	        while (resultSet.next()) {
   	            int id = resultSet.getInt("menu_item_id");
   		        String name = resultSet.getString("name");
   		        String mealType = resultSet.getString("mt.type_name");
   		        Double price = resultSet.getDouble("price");
   		        Boolean availaibility_status = resultSet.getBoolean("availability_status");
   		        resultBuilder.append(String.format("%-10d%-20s%-15s%-10.2f%-10b%n", 
   		                                                   id, name, mealType, price, availaibility_status));
   	        }
   	    }
    	out.println(resultBuilder.toString());
    }
}
