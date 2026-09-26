package search.auto.complete.service;

import search.auto.complete.autocompleteengine.AutoCompleteIndex;
import search.auto.complete.queryingestionengine.QueryIngestionBuffer;

import java.util.List;

public class SearchAutoCompleteService {
    final private AutoCompleteIndex autoCompleteIndex;
    final private QueryIngestionBuffer ingestionBuffer;

    public SearchAutoCompleteService(AutoCompleteIndex autoCompleteIndex, QueryIngestionBuffer ingestionBuffer) {
        this.autoCompleteIndex = autoCompleteIndex;
        this.ingestionBuffer = ingestionBuffer;
    }
    
    public List<String> getSuggestions(String prefix) {
        validateQuery(prefix);
        return autoCompleteIndex.search(prefix);
        
    }

    public void recordQuery(String query) {
        validateQuery(query);
        ingestionBuffer.queue(query);
    }

    private void validateQuery(String query) {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("Query cannot be Blank or empty");
        if (query.length() > 20) throw new IllegalArgumentException("Suggestions unavailable for query length > 20 characters");

        for (char k : query.toCharArray()) {
            if (k != ' ' && (k < 'a' || k > 'z')) {
                throw new IllegalArgumentException("Query contains invalid characters");
            }
        }
    }
}
