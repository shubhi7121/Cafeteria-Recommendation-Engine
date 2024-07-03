package server;

import java.io.PrintWriter;
import java.sql.*;
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
		List<FoodItem> foodItems = getAllFoodItems();

		for (FoodItem foodItem : foodItems) {
			FeedbackData feedbackData = getFeedbackDataForFoodItem(foodItem);
			double rating = (feedbackData.getAvgRating() + foodItem.getRating()) / 2;
			double score = (feedbackData.getAvgSentimentScore() + foodItem.getSentimentScore()) / 2;
			Set<String> comments = new HashSet<>();
			comments.addAll(feedbackData.getSentimentComments());
			comments.addAll(foodItem.getSentimentComments());
			foodItem.setAvgRating(rating);
			foodItem.setAvgSentimentScore(score);
			foodItem.setSentimentComments(comments);
		}

		updateFoodItems(foodItems);
	}

	void updateFoodItems(List<FoodItem> foodItems) {
		String updateSQL = "UPDATE meal_item SET rating = ?, sentiment_score = ?, sentiments = ? WHERE menu_item_id = ?";
		try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {

			for (FoodItem foodItem : foodItems) {
				int rate = (int) Math.ceil(foodItem.getAvgRating()) > 0 ? (int) Math.ceil(foodItem.getAvgRating()) : 1;
				statement.setInt(1, rate);
				statement.setDouble(2, foodItem.getAvgSentimentScore());
				Set<String> sentimentsSet = foodItem.getSentimentComments();
				StringJoiner joiner = new StringJoiner(",");
				for (String sentiment : sentimentsSet) {
					joiner.add(sentiment.trim());
				}
				String sentiments = joiner.toString();
				statement.setString(3, sentiments);
				statement.setInt(4, foodItem.getId());
				statement.addBatch();
			}
			statement.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	FeedbackData getFeedbackDataForFoodItem(FoodItem foodItem) {
		double rating, sentimentScore;
		String sentimentComment;
		FeedbackData feedbackData = new FeedbackData();
		String query = "SELECT rating, sentiment_score, sentiments FROM feedback WHERE menu_item_id = ?";

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, foodItem.getId());
			ResultSet resultSet = statement.executeQuery();
			int count = 0;
			double totalRating = 0, totalSentimentScore = 0;
			Set<String> comments = new HashSet<>();

			while (resultSet.next()) {
				rating = resultSet.getDouble("rating");
				sentimentScore = resultSet.getDouble("sentiment_score");
				sentimentComment = resultSet.getString("sentiments");
				totalRating += rating;
				totalSentimentScore += sentimentScore;
				String[] sentimentArray = sentimentComment.split(",\\s*");
				comments.addAll(Arrays.asList(sentimentArray));
				count++;
			}

			if (count > 0) {
				feedbackData.setAvgRating(totalRating / count);
				feedbackData.setAvgSentimentScore(totalSentimentScore / count);
			}
			feedbackData.setSentimentComments(comments);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackData;
	}

	List<FoodItem> getAllFoodItems() {
		List<FoodItem> foodItems = new ArrayList<>();
		String query = "SELECT mi.menu_item_id, mi.name, mi.rating, mi.sentiment_score, mt.type_name, mi.sentiments FROM meal_item mi JOIN meal_type mt ON mi.type_id = mt.type_id ORDER BY mi.menu_item_id";

		try (PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				int id = resultSet.getInt("menu_item_id");
				String name = resultSet.getString("name");
				String mealType = resultSet.getString("mt.type_name");
				int rating = resultSet.getInt("rating");
				int sentimentScore = resultSet.getInt("sentiment_score");
				String sentiments = resultSet.getString("sentiments");
				Set<String> sentimentSet = new HashSet<>();
				String[] elements = sentiments.split(",");
				for (String element : elements) {
					sentimentSet.add(element.trim());
				}
				FoodItem foodItem = new FoodItem(id, name, mealType, rating, sentimentScore, sentimentSet);
				foodItems.add(foodItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return foodItems;
	}
}