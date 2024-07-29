package server;

import java.util.*;
import constants.Constants;
import exceptions.SentimentAnalysisException;

public class SentimentAnalyzer {

    private static final Map<Integer, Set<String>> SENTIMENT_WORDS_MAP = Map.of(
        Constants.SCORE_NEGATIVE_STRONG, Constants.NEGATIVE_STRONG,
        Constants.SCORE_NEGATIVE_WEAK, Constants.NEGATIVE_WEAK,
        Constants.SCORE_NEUTRAL, Constants.NEUTRAL,
        Constants.SCORE_POSITIVE_WEAK, Constants.POSITIVE_WEAK,
        Constants.SCORE_POSITIVE_STRONG, Constants.POSITIVE_STRONG
    );

    public static Map<Integer, Set<String>> calculateSentimentScore(String feedback) {
        validateFeedback(feedback);
        
        Map<Integer, Integer> sentimentCounts = countSentiments(feedback);
        int score = determineDominantSentimentScore(sentimentCounts);

        return extractSentimentWords(feedback, score);
    }

    private static void validateFeedback(String feedback) {
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new SentimentAnalysisException("Feedback cannot be null or empty.");
        }
    }

    private static Map<Integer, Integer> countSentiments(String feedback) {
        Map<Integer, Integer> sentimentCounts = new HashMap<>();
        String[] words = feedback.toLowerCase().split("\\s+");

        for (String word : words) {
            for (Map.Entry<Integer, Set<String>> entry : SENTIMENT_WORDS_MAP.entrySet()) {
                if (entry.getValue().contains(word)) {
                    sentimentCounts.put(entry.getKey(), sentimentCounts.getOrDefault(entry.getKey(), 0) + 1);
                }
            }
        }
        return sentimentCounts;
    }

    private static int determineDominantSentimentScore(Map<Integer, Integer> sentimentCounts) {
        if (sentimentCounts.isEmpty()) {
            throw new SentimentAnalysisException("No sentiments found in the feedback.");
        }
        
        int maxCount = Collections.max(sentimentCounts.values());
        return sentimentCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new SentimentAnalysisException("Unable to determine sentiment score."));
    }

    private static Map<Integer, Set<String>> extractSentimentWords(String feedback, int score) {
        Set<String> sentimentWords = new HashSet<>();
        String[] words = feedback.toLowerCase().split("\\s+");

        if (SENTIMENT_WORDS_MAP.containsKey(score)) {
            for (String word : words) {
                if (SENTIMENT_WORDS_MAP.get(score).contains(word)) {
                    sentimentWords.add(word);
                }
            }
        } else {
            throw new SentimentAnalysisException("Invalid sentiment score.");
        }

        return Map.of(score, sentimentWords);
    }
}
