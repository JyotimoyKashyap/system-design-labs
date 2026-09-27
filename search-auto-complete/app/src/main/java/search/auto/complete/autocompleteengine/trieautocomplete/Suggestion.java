package search.auto.complete.autocompleteengine.trieautocomplete;

public class Suggestion implements Comparable<Suggestion>{
    private double rank;
    private String autoSuggest;

    public Suggestion(double rank, String autoSuggest) {
        this.rank = rank;
        this.autoSuggest = autoSuggest;
    }

    public String getAutoSuggest() {
        return autoSuggest;
    }

    @Override
    public int compareTo(Suggestion o) {
        int compare = Double.compare(rank, o.rank);
        if (compare == 0) {
            return o.autoSuggest.compareTo(autoSuggest);
        }
        return compare;
    }
}
