package clientServer;

import java.util.*;
import java.util.stream.Collectors;

public class SentimentWords {
	public static final Set<String> NEGATIVE_STRONG = new HashSet<>(
			Arrays.asList("Awful", "Horrible", "Terrible", "Disgusting", "Hate", "Unbearable", "Repulsive", "Atrocious",
					"Dreadful", "Appalling", "Abysmal", "Nauseating", "Revolting", "Loathsome", "Deplorable",
					"Abominable", "Vile", "Detestable", "Unpleasant", "Inferior", "Miserable", "Horrendous",
					"Distasteful", "Grotesque", "Horrific", "Foul", "Putrid", "Vicious", "Heinous", "Diabolical"))
			.stream().map(String::toLowerCase).collect(Collectors.toSet());

	public static final Set<String> NEGATIVE_WEAK = new HashSet<>(Arrays.asList("Bad", "Poor", "Disliked",
			"Unsatisfactory", "Subpar", "Mediocre", "Unpleasant", "Disappointing", "Inferior", "Lacking",
			"Unimpressive", "Deficient", "Lousy", "Substandard", "Unacceptable", "Faulty", "Flawed", "Inadequate",
			"Defective", "Unappealing", "Lamentable", "Unfortunate", "Second-rate", "Shoddy", "Mediocre", "Substandard",
			"Unfulfilling", "Regrettable", "Lackluster", "Passable")).stream().map(String::toLowerCase)
			.collect(Collectors.toSet());

	public static final Set<String> NEUTRAL = new HashSet<>(Arrays.asList("Average", "Okay", "Fine", "Satisfactory",
			"Indifferent", "Moderate", "Fair", "Unremarkable", "Tolerable", "Middling", "Passable", "Standard",
			"Acceptable", "Usual", "Ordinary", "Plain", "Commonplace", "Middling", "Routine", "Regular", "So-so",
			"Workable", "Decent", "Moderate", "Reasonable", "Mediocre", "Average", "All right", "Standard", "Adequate"))
			.stream().map(String::toLowerCase).collect(Collectors.toSet());

	public static final Set<String> POSITIVE_WEAK = new HashSet<>(Arrays.asList("Good", "Enjoyable", "Pleasant",
			"Satisfying", "Nice", "Liked", "Delightful", "Pleasing", "Admirable", "Commendable", "Worthy", "Gratifying",
			"Pleasurable", "Appealing", "Lovely", "Congenial", "Agreeable", "Charming", "Delightful", "Rewarding",
			"Pleasurable", "Favorable", "Admirable", "Superior", "Nice", "Praiseworthy", "Positive", "Gratifying",
			"Encouraging", "Pleasant")).stream().map(String::toLowerCase).collect(Collectors.toSet());

	public static final Set<String> POSITIVE_STRONG = new HashSet<>(Arrays.asList("Excellent", "Fantastic", "Amazing",
			"Wonderful", "Outstanding", "Superb", "Love", "Exceptional", "Marvelous", "Brilliant", "Terrific",
			"Remarkable", "Phenomenal", "Extraordinary", "Magnificent", "Perfect", "Splendid", "Glorious", "Stellar",
			"Exquisite", "Superb", "Unmatched", "Unbeatable", "Impressive", "Stunning", "Sensational", "Divine",
			"Awesome", "Superior", "Tasty", "Sweet", "Top-notch")).stream().map(String::toLowerCase)
			.collect(Collectors.toSet());

	private static final Map<Integer, Set<String>> SENTIMENT_WORDS_MAP = Map.of(1, Constants.NEGATIVE_STRONG, 2,
			Constants.NEGATIVE_WEAK, 3, Constants.NEUTRAL, 4, Constants.POSITIVE_WEAK, 5, Constants.POSITIVE_STRONG);

	public static Map<Integer, Set<String>> calculateSentimentScore(String feedback) {
		Map<Integer, Integer> sentimentCounts = new HashMap<>();
		String[] words = feedback.toLowerCase().split("\\s+");

		for (String word : words) {
			for (Map.Entry<Integer, Set<String>> entry : SENTIMENT_WORDS_MAP.entrySet()) {
				if (entry.getValue().contains(word)) {
					sentimentCounts.put(entry.getKey(), sentimentCounts.getOrDefault(entry.getKey(), 0) + 1);
				}
			}
		}

		int maxCount = Collections.max(sentimentCounts.values());
		int score = sentimentCounts.entrySet().stream().filter(entry -> entry.getValue() == maxCount)
				.map(Map.Entry::getKey).findFirst().orElse(3);

		return extractSentimentWords(feedback, score);
	}

	private static Map<Integer, Set<String>> extractSentimentWords(String feedback, int score) {
		Set<String> sentimentWords = new HashSet<>();
		String[] words = feedback.toLowerCase().split("\\s+");

		for (String word : words) {
			if (SENTIMENT_WORDS_MAP.get(score).contains(word)) {
				sentimentWords.add(word);
			}
		}

		return Map.of(score, sentimentWords);
	}
}