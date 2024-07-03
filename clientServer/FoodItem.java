package clientServer;

import java.util.*;

public class FoodItem {
	private int id;
	private String name;
	private String mealType;
	private double rating;
	private double sentimentScore;
	private double avgRating;
	private double avgSentimentScore;
	private Set<String> sentimentComments;

	public FoodItem(int id, String name, String mealType, double rating, double sentimentScore,
			Set<String> sentimentComments) {
		this.id = id;
		this.name = name;
		this.mealType = mealType;
		this.rating = rating;
		this.sentimentScore = sentimentScore;
		this.sentimentComments = new HashSet<>(sentimentComments); // Defensive copying
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMealType() {
		return mealType;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getSentimentScore() {
		return sentimentScore;
	}

	public void setSentimentScore(double sentimentScore) {
		this.sentimentScore = sentimentScore;
	}

	public double getAvgRating() {
		return avgRating;
	}

	public void setAvgRating(double avgRating) {
		this.avgRating = avgRating;
	}

	public double getAvgSentimentScore() {
		return avgSentimentScore;
	}

	public void setAvgSentimentScore(double avgSentimentScore) {
		this.avgSentimentScore = avgSentimentScore;
	}

	public Set<String> getSentimentComments() {
		return new HashSet<>(sentimentComments);
	}

	public void setSentimentComments(Set<String> sentimentComments) {
		this.sentimentComments = new HashSet<>(sentimentComments); 
	}
}