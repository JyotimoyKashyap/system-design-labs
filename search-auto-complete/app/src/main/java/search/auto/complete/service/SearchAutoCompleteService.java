package search.auto.complete.service;

import search.auto.complete.autocompleteengine.AutoCompleteIndex;

import java.util.List;

public class SearchAutoCompleteService {
    final private AutoCompleteIndex autoCompleteIndex;

    public SearchAutoCompleteService(AutoCompleteIndex autoCompleteIndex) {
        this.autoCompleteIndex = autoCompleteIndex;
    }
    
    public List<String> getSuggestions(String prefix) {
        validateQuery(prefix);
        return autoCompleteIndex.search(prefix);
        
    }

    public void recordQuery(String query) {
        validateQuery(query);
        autoCompleteIndex.insert(query);
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
