package clientServer;

import java.io.PrintWriter;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class RecommendationEngine {
	
    private Connection connection;
    private PrintWriter out;
    private Map<Integer, Integer> sentimentScores;
    private Map<Integer, Set<String>> sentimentWordsMap;
    private Map<Integer, Double> averageRatings;

    public RecommendationEngine(Connection connection, PrintWriter out) {
        this.connection = connection;
        this.out = out;
        sentimentScores = new HashMap<>();
        sentimentWordsMap = new HashMap<>();
        averageRatings = new HashMap<>();
    }

	public void viewRecommendations() throws SQLException {
    String query = "SELECT menu_item_id, GROUP_CONCAT(comment SEPARATOR ' ') as feedbacks, AVG(rating) as avg_rating " +
                   "FROM feedback GROUP BY menu_item_id";
    try (PreparedStatement statement = connection.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
            int mealItemId = resultSet.getInt("menu_item_id");
            System.out.println("mealItemId: "+mealItemId);
            String feedbacks = resultSet.getString("feedbacks");
            System.out.println("feedbacks: "+feedbacks);
            double avgRating = resultSet.getDouble("avg_rating");

            Map<Integer, Set<String>> mappingmeal = new HashMap<>();
            mappingmeal = SentimentWords.sentimentann(feedbacks);
            
            for (Map.Entry<Integer, Set<String>> entry : mappingmeal.entrySet()) {
                Integer id = entry.getKey();
                Set<String> stringSet = entry.getValue();

                sentimentScores.put(mealItemId, id); // Store the id in the idMap
                System.out.println("sentimentScores: "+sentimentScores);
                sentimentWordsMap.put(mealItemId, stringSet); // Store the id and its set of strings in stringSetMap
                System.out.println("sentimentWordsMap: "+sentimentWordsMap);
            }
            averageRatings.put(mealItemId, avgRating);
        }
    }
    out.println("ID  | Meal Item         | Meal Type | Price  | Rating | Sentiment Words");
    out.println("----|-------------------|-----------|--------|--------|----------------");

    String bfQuery = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price FROM meal_item mi " +
            "JOIN meal_type mt ON mi.type_id = mt.type_id " +
        "WHERE mt.type_name = 'Breakfast' " +
        "ORDER BY mi.rating DESC"; //+
        //"LIMIT 3";
	    
	String lunchQuery = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price FROM meal_item mi " +
                "JOIN meal_type mt ON mi.type_id = mt.type_id " +
	        "WHERE mt.type_name = 'Lunch' " +
	        "ORDER BY mi.rating DESC";//+
	        //"LIMIT ";
	
	String dinnerQuery = "SELECT mi.menu_item_id, mi.name, mt.type_name, mi.price FROM meal_item mi " +
                "JOIN meal_type mt ON mi.type_id = mt.type_id " +
	        "WHERE mt.type_name = 'Dinner' " +
	        "ORDER BY mi.rating DESC"; //+
	        //"LIMIT 3";
    
	    method(bfQuery);
	    method(lunchQuery);
	    method(dinnerQuery);
}
	private void method( String mealQuery) throws SQLException {
		try (PreparedStatement mealStatement = connection.prepareStatement(mealQuery);
		ResultSet mealResultSet = mealStatement.executeQuery()) {
		
			while (mealResultSet.next()) {
			 int id = mealResultSet.getInt("menu_item_id");
			 String name = mealResultSet.getString("name");
			 String mealType = mealResultSet.getString("mt.type_name");
			 double price = mealResultSet.getDouble("price");
			
			 int sentimentScore = sentimentScores.getOrDefault(id, 3);
			 System.out.println("sentimentWordsMap: "+sentimentWordsMap);
			 Set<String> sentimentWords = sentimentWordsMap.getOrDefault(id, Collections.emptySet());
			 double avgRating = averageRatings.getOrDefault(id, 3.0);
			
			 List<String> sortedWords = new ArrayList<>(sentimentWords);
			 Collections.sort(sortedWords);
			 if (sortedWords.size() > 3) {
			     sortedWords = sortedWords.subList(0, 3);
			 }
		
		    out.printf("%-4d| %-18s| %-10s| %-6.2f| %-6.2f| %-16s%n", id, name, mealType, price, avgRating, String.join(", ", sortedWords));
			}
		}
}
	
	
}