import java.util.*;

/**
 * BS18_06 Yuloskov Artyom
 */
public class SpellAutocorrection {
    /**
     * I did not come up with this algorithm by myself, but Levenshtein did. And I was lucky to know about it:)
     * Here is the resources where I investigated the problem and found the pseudocode:
     * <p>
     * https://goo.gl/J23MHU
     * https://www.youtube.com/watch?v=MiqoA-yF-0M
     * <p>
     * I used a dynamic programming approach to solve this task. I create a 2d array of sub problems.
     * Each sub problem is actually the question: how can I get with the minimal cost
     * from one substring to another substring? How many moves I need to do?
     * In this array I remember the amount of moves I need to do to get needed substring. For example:
     * <p>
     *  clot
     * c0123
     * a1123
     * t2232
     * <p>
     * The data structure I used is simple 2d array, because it has fast random access speed.
     *
     * @param w1 - first word to compare
     * @param w2 -  second word to compare
     * @param insertCost - cost of insertion of the letter
     * @param deleteCost - cost of deletion of the letter
     * @param swapCost - cost of swapping two adjacent letters
     * @param replaceCost - cost of replacing of the letter
     * @return - the distance between the words
     */
    private int flexibleEstimation(String w1, String w2, int insertCost, int deleteCost, int swapCost, int replaceCost) {
        //2d array of minimal cost to get from one substring to another
        int[][] dp = new int[w1.length()+1][w2.length()+1];
        dp[0][0] = 0;
        //fill up the dynamic basis
        for (int i = 1; i < w1.length()+1; i++) {
            dp[i][0] = dp[i-1][0]+deleteCost;
        }

        for (int j = 1; j < w2.length()+1; j++) {
            dp[0][j] = dp[0][j-1] + insertCost;
        }

        //actual algorithm
        for (int i = 0; i < w1.length(); i++) {
            for (int j = 0; j < w2.length(); j++) {

                if (w1.charAt(i) != w2.charAt(j)) {
                    //chose either deletion ot insertion
                    dp[i+1][j+1] = Math.min(dp[i + 1][j] + insertCost,
                            Math.min(dp[i][j + 1] + deleteCost, dp[i][j] + replaceCost));
                } else {
                    //choose replacement if letter in one word equal to letter in another word
                    dp[i+1][j+1] = dp[i][j];
                }

                //choose swap
                if (i > 0 && j > 0) {
                    if (w1.charAt(i) == w2.charAt(j-1) && w1.charAt(i-1) == w2.charAt(j)) {
                        dp[i+1][j+1] = Math.min(dp[i - 1][j - 1] + swapCost, dp[i+1][j+1]);
                    }
                }
            }
        }

        return dp[w1.length()][w2.length()];
    }

    /**
     * !!! THE ANSWER TO THE FIRST BONUS QUESTION IS HERE !!!
     *
     * Actually, the answer to the bonus question is adding the arguments to specify the cost of
     * changing specified misspelling. So, in the algorithm from the assignment the cost of each operation is 1.
     *
     * @param w1 - first word to compare
     * @param w2 - second word to compare
     * @return - the distance between the words
     */
    private int estimate(String w1, String w2) {
        return flexibleEstimation(w1, w2, 1, 1, 1, 1);
    }

    /**
     * The algorithm uses method estimate(String word1, String word2) to find the closest words to given word.
     * I use an array to store estimations for each word in dictionary.
     * In the end I use Arrays.sort() to sort words in lexicographic order.
     *
     * @param dictionary - the set of words that considered as right
     * @param word       - word to check whether it is right, if not then suggest the closest correct words from dictionary
     * @return - returns an array of closest suggestions
     */
    private String[] correctionSuggestions(String[] dictionary, String word) {
        int[] estimations = new int[dictionary.length];
        int min = Integer.MAX_VALUE;
        int numberOfSuggestions = 0;

        for (int i = 0; i < dictionary.length; i++) {

            int currentEstimation = estimate(dictionary[i], word);
            estimations[i] = currentEstimation;

            //find the minimum number of estimations and count how many words with this number in the dictionary
            if (currentEstimation < min) {
                min = currentEstimation;
                numberOfSuggestions = 1;
            } else if (currentEstimation == min) {
                numberOfSuggestions++;
            }
        }

        String[] suggestions = new String[numberOfSuggestions];
        int k = 0;

        //find the words with the minimum number of estimations in the dictionary
        for (int i = 0; i < dictionary.length; i++) {
            if (estimations[i] == min) {
                suggestions[k++] = dictionary[i];
            }
        }
        //sort the words in lexicographic order
        Arrays.sort(suggestions);
        return suggestions;
    }

    /**
     * Method uses HashMap to store the frequency of the corresponding word. I chose this data structure because
     * it can conveniently store the pairs of objects. Also, it allows the access to this objects in constant time.
     *
     * @param dictionary - string that contains dictionary words
     * @param toCorrect  - string that I need to correct
     * @return - return the corrected string
     */
    private String textAutocorrection(String dictionary, String toCorrect) {
        //get the array of words from the dictionary string
        String[] dict = dictionary.split("[^a-z]+");

        //get the array of words from toCorrect string
        String[] wordsToCorrect = toCorrect.split("[^a-z]+");

        //get the array of symbols between the words, spaces and multiple punctuations are included
        String[] betweenWords = toCorrect.split("\\w+");

        String[] correctWords = new String[wordsToCorrect.length]; //to store corrected words

        //Calculate the length of a final array. If there were no spaces or punctuation marks add 1 to length
        //to store the empty string
        int n = correctWords.length + (betweenWords.length == 0 ? 1 : betweenWords.length);
        String[] output = new String[n];

        HashMap<String, Integer> frequencies = countFrequencies(dict);

        //set of unique dictionary words to compare with
        String[] uniqueWords = frequencies.keySet().toArray(new String[0]);

        //find the most suitable word in the dictionary and put in the array of corrected words
        for (int i = 0; i < wordsToCorrect.length; i++) {
            String[] possibleSuggestions = correctionSuggestions(uniqueWords, wordsToCorrect[i]);
            String maxFrequencyString = "";
            int maxFrequency = Integer.MIN_VALUE;

            for (String possibleSuggestion : possibleSuggestions) {
                if (frequencies.get(possibleSuggestion) > maxFrequency) {
                    maxFrequency = frequencies.get(possibleSuggestion);
                    maxFrequencyString = possibleSuggestion;
                }
            }

            correctWords[i] = maxFrequencyString;
        }

        int k = 0;
        int j = 0;

        //merge the array of punctuation marks and spaces with the array of corrected words
        for (int i = 0; i < n; i++) {
            output[i] = betweenWords.length == 0 ? "" : betweenWords[k++];
            output[++i] = correctWords[j++];
        }

        //put it all in one string
        StringBuilder sb = new StringBuilder();
        for (String s : output) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * The method counts the occurrences of each word in the dictionary.
     *
     * @param dict - multiset of strings
     * @return - HashMap of unique words and corresponding frequencies
     */
    private HashMap<String, Integer> countFrequencies(String[] dict) {
        List<String> dictList = Arrays.asList(dict);
        HashMap<String, Integer> frequencies = new HashMap<>();

        for (String s : dict) {
            frequencies.put(s, Collections.frequency(dictList, s));
        }
        return frequencies;
    }

    public static void main(String[] args) {
        SpellAutocorrection sa = new SpellAutocorrection();
        Scanner sc = new Scanner(System.in);
        String s1 = sc.nextLine();
        String s2 = sc.nextLine();
        System.out.println(sa.textAutocorrection(s1,s2));
    }
}
