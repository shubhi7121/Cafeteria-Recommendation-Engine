package clientServer;
import java.util.*;
import java.util.stream.*;

public class SentimentWords {
	final static Set<String> a1 = new HashSet<>(Arrays.asList("Awful", "Horrible", "Terrible", "Disgusting", "Hate", "Unbearable", 
                                      "Repulsive", "Atrocious", "Dreadful", "Appalling", "Abysmal", "Nauseating", 
                                      "Revolting", "Loathsome", "Deplorable", "Abominable", "Vile", "Detestable", 
                                      "Unpleasant", "Inferior", "Miserable", "Horrendous", "Distasteful", "Grotesque", 
                                      "Horrific", "Foul", "Putrid", "Vicious", "Heinous", "Diabolical")).stream().map(String::toLowerCase).collect(Collectors.toSet());
    final static Set<String> a2 = new HashSet<>(Arrays.asList("Bad", "Poor", "Disliked", "Unsatisfactory", "Subpar", "Mediocre", "Unpleasant", 
                                      "Disappointing", "Inferior", "Lacking", "Unimpressive", "Deficient", "Lousy", 
                                      "Substandard", "Unacceptable", "Faulty", "Flawed", "Inadequate", "Defective", 
                                      "Unappealing", "Lamentable", "Unfortunate", "Second-rate", "Shoddy", "Mediocre", 
                                      "Substandard", "Unfulfilling", "Regrettable", "Lackluster", "Passable")).stream().map(String::toLowerCase).collect(Collectors.toSet());
    
    final static Set<String> a3 = new HashSet<>(Arrays.asList("Average", "Okay", "Fine", "Satisfactory", "Indifferent", "Moderate", "Fair", 
            "Unremarkable", "Tolerable", "Middling", "Passable", "Standard", "Acceptable", 
            "Usual", "Ordinary", "Plain", "Commonplace", "Middling", "Routine", "Regular", 
            "So-so", "Workable", "Decent", "Moderate", "Reasonable", "Mediocre", "Average", 
            "All right", "Standard", "Adequate")).stream().map(String::toLowerCase).collect(Collectors.toSet());
    
    final static Set<String> a4 = new HashSet<>(Arrays.asList("Good", "Enjoyable", "Pleasant", "Satisfying", "Nice", "Liked", "Delightful", 
            "Pleasing", "Admirable", "Commendable", "Worthy", "Gratifying", "Pleasurable", 
            "Appealing", "Lovely", "Congenial", "Agreeable", "Charming", "Delightful", 
            "Rewarding", "Pleasurable", "Favorable", "Admirable", "Superior", "Nice", 
            "Praiseworthy", "Positive", "Gratifying", "Encouraging", "Pleasant")).stream().map(String::toLowerCase).collect(Collectors.toSet());
    
    final static Set<String> a5 = new HashSet<>(Arrays.asList("Excellent", "Fantastic", "Amazing", "Wonderful", "Outstanding", "Superb", 
            "Love", "Exceptional", "Marvelous", "Brilliant", "Terrific", "Remarkable", 
            "Phenomenal", "Extraordinary", "Magnificent", "Perfect", "Splendid", 
            "Glorious", "Stellar", "Exquisite", "Superb", "Unmatched", "Unbeatable", 
            "Impressive", "Stunning", "Sensational", "Divine", "Awesome", "Superior", 
            "Top-notch")).stream().map(String::toLowerCase).collect(Collectors.toSet());   

	 public static Map<Integer, Set<String>>  sentimentann(String feedback) {
		 System.out.println("feedback: "+feedback);
		 Map<Integer, Set<String>> mapping2 = new HashMap<>();
        int a1Count = 0;
        int a2Count = 0;
        int a3Count = 0;
        int a4Count = 0;
        int a5Count = 0;
        int score = 0;
        String[] words = feedback.toLowerCase().split("\\s+");
        for (String word : words) {
            if (a1.contains(word)) {
            	a1Count++;
            	System.out.println("a1Count" + a1Count);
            } 
            else if (a2.contains(word)) {
            	a2Count++;
            	System.out.println("a2Count" + a2Count);
            }
            else if (a3.contains(word)) {
            	a3Count++;
            	System.out.println("a3Count" + a3Count);
            } 
            else if (a4.contains(word)) {
            	a4Count++;
            	System.out.println("a4Count" + a4Count);
            } 
            else if (a5.contains(word)) {
            	a5Count++;
            	System.out.println("a5Count" + a5Count);
            }
        } 
        if (a1Count > a2Count && a1Count > a3Count && a1Count > a4Count && a1Count > a5Count) {
        	score = 1;
        } else if (a2Count > a1Count && a2Count > a3Count && a2Count > a4Count && a2Count > a5Count) {
        	score = 2;
        } else if (a3Count > a1Count && a3Count > a2Count && a3Count > a4Count && a3Count > a5Count) {
        	score = 3;
        } else if (a4Count > a1Count && a4Count > a2Count && a4Count > a3Count && a4Count > a5Count) {
        	score = 4;
        } else if (a5Count > a1Count && a5Count > a2Count && a5Count > a3Count && a5Count > a4Count) {
        	score = 5;
        } else {
        	score = 3;
        }
        System.out.println("score: "+score);
        mapping2 = extractSentimentWords( feedback,  score);
        System.out.println("mapping2: "+mapping2);
        
        return mapping2;
    }

	 public static Map<Integer, Set<String>> extractSentimentWords(String feedback, int score) {
		 Set<String> sentimentWords = new HashSet<>();
	     String[] words = feedback.toLowerCase().split("\\s+");
	     System.out.println("words: "+words);
	     Map<Integer, Set<String>> mapping = new HashMap<>();
    	 if(score == 1) {
    		 for (String word : words) {
    			 if (a1.contains(word)){
    				 sentimentWords.add(word);
    	         }
    	      }
    	 }
    	  else if(score == 2){
    	      for (String word : words) {
        	      if (a2.contains(word)){
        	        sentimentWords.add(word);
        	      }
        	  }
    	  }
		  else if(score == 3){
						for (String word : words) {
	    	        		if (a3.contains(word)){
	    	        			sentimentWords.add(word);
	    	        		}
	    	        	}        		
		  }
					else if(score == 4){
						for (String word : words) {
	    	        		if (a4.contains(word)){
	    	        			sentimentWords.add(word);
	    	        		}
	    	        	}
					}
					else if(score == 5){
						System.out.println("score in words: "+score);
						for (String word : words) {
							System.out.println("mapping: "+mapping);System.out.println("word: "+word);
	    	        		if (a5.contains(word)){
	    	        			sentimentWords.add(word);
	    	        		}
	    	        	}
					}
    	 mapping.put(score, sentimentWords);
    	 System.out.println("mapping: "+mapping);
    	        return mapping;
    	 }
}