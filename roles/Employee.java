package roles;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Employee implements Role {
    private Connection connection;
    private BufferedReader in;
    private PrintWriter out;

    public Employee(Connection connection, BufferedReader in, PrintWriter out) {
        this.connection = connection;
        this.in = in;
        this.out = out;
    }

    @Override
    public void showOptions() throws IOException {
        out.println("Employee actions: 1. Give Feedback 2. Vote for Tomorrow Menu 3. Show menu  Type exit to logout");
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
            case "exit":
                out.println("Exiting...");
                break;
            default:
                out.println("Invalid action.");
        }
    }

    private void voteForTommorrowMenu() throws IOException, SQLException {
    	StringBuilder resultBuilder = new StringBuilder();
    	resultBuilder.append(String.format("%-10s%-20s%-15s%-10s%-15s\n", 
				"ID", "Meal Item", "Meal Type", "Rating", "Sentiments"));
    	String query = "SELECT Id, r.meal_item_id, mi.name AS meal_item_name, mt.type_name AS meal_type, r.rating, r.sentiments, r.date "
    			+ "FROM rolloutmenu r JOIN meal_item mi ON r.meal_item_id = mi.menu_item_id JOIN meal_type mt ON mi.type_id = mt.type_id"; 

    	try (PreparedStatement statement = connection.prepareStatement(query);
   	         ResultSet resultSet = statement.executeQuery()) {
   	        while (resultSet.next()) {
   	            int id = resultSet.getInt("Id");
   		        String name = resultSet.getString("meal_item_name");
   		        String mealType = resultSet.getString("meal_type");
   		        int rating = resultSet.getInt("rating");
   		        String sentiments = resultSet.getString("sentiments");
   		        resultBuilder.append(String.format("%-10d%-20s%-15s%-10d%-10s%n", id, name, mealType, rating, sentiments));
   	        }
   	    }
    	out.println(resultBuilder.toString());
    	
    }
    
    private void giveFeedback() throws IOException, SQLException {
    	String id = in.readLine();
	    int Id = Integer.parseInt(id);
	    System.out.println("Received menu id: " + Id);
	    
    	String comment = in.readLine();
        System.out.println("Received comment: " + comment);
         
        String rating = in.readLine();
	    int rate = Integer.parseInt(rating);
	    System.out.println("Received rate: " + rating);
	                   
	    LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        String insertQuery = "INSERT INTO feedback (menu_item_id, rating, comment, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setInt(1, Id);
            statement.setInt(2, rate);
            statement.setString(3, comment);
            statement.setString(4, formattedDate);
            statement.executeUpdate();
            out.println("feedback given");
        }
    }

    private void showMenu() throws IOException, SQLException {
    	StringBuilder resultBuilder = new StringBuilder();
    	resultBuilder.append(String.format("%-10s%-20s%-15s%-10s%-15s\n", 
				"ID", "Meal Item", "Meal Type", "Price", "Availaibility Status"));
    	String query = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price, mi.availability_status " +
    	        "FROM meal_item mi " +
    	        "JOIN meal_type mt ON mi.type_id = mt.type_id Order by mi.menu_item_id";
    	try (PreparedStatement statement = connection.prepareStatement(query);
   	         ResultSet resultSet = statement.executeQuery()) {
   	        while (resultSet.next()) {
   	            int id = resultSet.getInt("menu_item_id");
   		        String name = resultSet.getString("name");
   		        String mealType = resultSet.getString("mt.type_name");
   		        Double price = resultSet.getDouble("price");
   		        Boolean availaibility_status = resultSet.getBoolean("availability_status");
   		        resultBuilder.append(String.format("%-10d%-20s%-15s%-10.2f%-10b%n", id, name, mealType, price, availaibility_status));
   	        }
   	    }
    	out.println(resultBuilder.toString());
    }
}
