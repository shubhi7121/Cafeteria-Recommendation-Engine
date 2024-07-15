package dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FeedbackData {
    private double avgRating;
    private double avgSentimentScore;
    private final Set<String> sentimentComments;

    public FeedbackData() {
        this.sentimentComments = new HashSet<>();
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
        return Collections.unmodifiableSet(sentimentComments);
    }

    public void addSentimentComment(String comment) {
        sentimentComments.add(comment);
    }

    public void setSentimentComments(Set<String> sentimentComments) {
        this.sentimentComments.clear();
        if (sentimentComments != null) {
            this.sentimentComments.addAll(sentimentComments);
        }
    }
}
