package clientServer;

import java.util.*;

public class FeedbackData {
	private double avgRating;
	private double avgSentimentScore;
	private Set<String> sentimentComments;

	public FeedbackData() {
		sentimentComments = new HashSet<>();
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
		this.sentimentComments = new HashSet<>(sentimentComments); // defensive copying
	}
}