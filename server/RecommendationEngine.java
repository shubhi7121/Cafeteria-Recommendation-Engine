package server;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import constants.Constants;
import dto.FeedbackData;
import dto.FoodItem;
import exceptions.RecommendationException;
import exceptions.DataAccessException;

public class RecommendationEngine {
    private final Connection connection;
    private final PrintWriter out;

    public RecommendationEngine(Connection connection, PrintWriter out) {
        this.connection = connection;
        this.out = out;
    }

    public void processRecommendations() {
        try {
            List<FoodItem> foodItems = fetchAllFoodItems();
            updateFoodItemMetrics(foodItems);
            cleanOldData();
        } catch (SQLException e) {
            throw new RecommendationException("Error processing recommendations.", e);
        }
    }

    private void cleanOldData() {
        try {
            deleteOldFeedback();
            deleteOldMenu();
        } catch (SQLException e) {
            throw new RecommendationException("Error cleaning old data.", e);
        }
    }

    private void deleteOldFeedback() throws SQLException {
        executeUpdate(Constants.DELETE_OLD_FEEDBACK_SQL);
    }

    private void deleteOldMenu() throws SQLException {
        executeUpdate(Constants.DELETE_OLD_MENU_SQL);
    }

    private void executeUpdate(String query) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
            out.println("Old data cleaned successfully.");
        }
    }

    private void updateFoodItemMetrics(List<FoodItem> foodItems) throws SQLException {
        for (FoodItem foodItem : foodItems) {
            FeedbackData feedbackData = fetchFeedbackDataForFoodItem(foodItem);
            updateFoodItemWithMetrics(foodItem, feedbackData);
        }
        batchUpdateFoodItems(foodItems);
    }

    private void batchUpdateFoodItems(List<FoodItem> foodItems) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Constants.UPDATE_FOOD_ITEM_SQL)) {
            for (FoodItem foodItem : foodItems) {
                setFoodItemParameters(statement, foodItem);
                statement.addBatch();
            }
            statement.executeBatch();
            out.println("Food items updated successfully.");
        } catch (SQLException e) {
            throw new DataAccessException("Error updating food items.", e);
        }
    }

    private void setFoodItemParameters(PreparedStatement statement, FoodItem foodItem) throws SQLException {
        statement.setInt(1, (int) Math.ceil(foodItem.getAvgRating()));
        statement.setDouble(2, foodItem.getAvgSentimentScore());
        statement.setString(3, String.join(", ", foodItem.getSentimentComments()));
        statement.setInt(4, foodItem.getId());
    }

    private void updateFoodItemWithMetrics(FoodItem foodItem, FeedbackData feedbackData) {
        double averageRating = calculateAverageRating(feedbackData.getAvgRating(), foodItem.getRating());
        double averageScore = calculateAverageScore(feedbackData.getAvgSentimentScore(), foodItem.getSentimentScore());
        Set<String> aggregatedComments = aggregateComments(feedbackData.getSentimentComments(), foodItem.getSentimentComments());

        foodItem.setAvgRating(averageRating);
        foodItem.setAvgSentimentScore(averageScore);
        foodItem.setSentimentComments(aggregatedComments);
    }

    private FeedbackData fetchFeedbackDataForFoodItem(FoodItem foodItem) {
        try (PreparedStatement statement = connection.prepareStatement(Constants.SELECT_FEEDBACK_DATA_SQL)) {
            statement.setInt(1, foodItem.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToFeedbackData(resultSet);
            } else {
                return new FeedbackData();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching feedback data.", e);
        }
    }

    private FeedbackData mapResultSetToFeedbackData(ResultSet resultSet) throws SQLException {
        FeedbackData feedbackData = new FeedbackData();
        feedbackData.setAvgRating(resultSet.getDouble("avg_rating"));
        feedbackData.setAvgSentimentScore(resultSet.getDouble("avg_sentiment_score"));
        String sentimentComments = resultSet.getString("all_sentiments");
        feedbackData.setSentimentComments(parseSentimentComments(sentimentComments));
        return feedbackData;
    }

    private Set<String> parseSentimentComments(String comments) {
        return comments == null ? new HashSet<>() : new HashSet<>(Arrays.asList(comments.split(", ")));
    }

    private List<FoodItem> fetchAllFoodItems() {
        try (PreparedStatement statement = connection.prepareStatement(Constants.SELECT_ALL_FOOD_ITEMS_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<FoodItem> foodItems = new ArrayList<>();
            while (resultSet.next()) {
                foodItems.add(mapResultSetToFoodItem(resultSet));
            }
            return foodItems;
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching food items.", e);
        }
    }

    private FoodItem mapResultSetToFoodItem(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("menu_item_id");
        String name = resultSet.getString("name");
        String mealType = resultSet.getString("type_name");
        int rating = resultSet.getInt("rating");
        double sentimentScore = resultSet.getDouble("sentiment_score");
        String sentiments = resultSet.getString("sentiments");
        Set<String> sentimentSet = parseSentimentComments(sentiments);
        return new FoodItem(id, name, mealType, rating, sentimentScore, sentimentSet);
    }

    private double calculateAverageRating(double feedbackRating, double itemRating) {
        return (feedbackRating + itemRating) / 2.0;
    }

    private double calculateAverageScore(double feedbackScore, double itemScore) {
        return (feedbackScore + itemScore) / 2.0;
    }

    private Set<String> aggregateComments(Set<String> feedbackComments, Set<String> itemComments) {
        Set<String> aggregatedComments = new HashSet<>(feedbackComments);
        aggregatedComments.addAll(itemComments);
        return aggregatedComments;
    }
}
