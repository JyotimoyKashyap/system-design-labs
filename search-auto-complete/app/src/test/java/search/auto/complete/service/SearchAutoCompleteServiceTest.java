package search.auto.complete.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import search.auto.complete.autocompleteengine.trieautocomplete.TrieAutoCompleteIndex;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchAutoCompleteServiceTest {

    private SearchAutoCompleteService service;

    @BeforeEach
    void setUp() {
        TrieAutoCompleteIndex.resetForTesting();
        TrieAutoCompleteIndex index = TrieAutoCompleteIndex.init(5);
        service = new SearchAutoCompleteService(index);
    }

    @Test
    @DisplayName("Should return empty list when no queries match the prefix")
    void testEmptyTrieReturnsEmptyList() {
        List<String> suggestions = service.getSuggestions("tech");
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    @DisplayName("Should return single matching query for valid prefix")
    void testSingleWordSuggestion() {
        service.recordQuery("apple");

        List<String> suggestions = service.getSuggestions("app");
        assertEquals(List.of("apple"), suggestions);
    }

    @Test
    @DisplayName("Should return all matching completions within prefix subtree")
    void testMultipleWordsPrefixMatch() {
        service.recordQuery("app");
        service.recordQuery("apple");
        service.recordQuery("application");

        List<String> suggestions = service.getSuggestions("app");
        assertEquals(3, suggestions.size());
        assertTrue(suggestions.contains("app"));
        assertTrue(suggestions.contains("apple"));
        assertTrue(suggestions.contains("application"));
    }

    @Test
    @DisplayName("Should rank completions strictly by search frequency (highest rank first)")
    void testRankingByFrequency() {
        // "apple" searched 1 time
        service.recordQuery("apple");

        // "application" searched 3 times
        service.recordQuery("application");
        service.recordQuery("application");
        service.recordQuery("application");

        // "app" searched 2 times
        service.recordQuery("app");
        service.recordQuery("app");

        List<String> suggestions = service.getSuggestions("ap");
        assertEquals(List.of("application", "app", "apple"), suggestions);
    }

    @Test
    @DisplayName("Should truncate results to Top-K limits")
    void testTopKTruncation() {
        // Re-initialize with K = 2
        TrieAutoCompleteIndex.resetForTesting();
        TrieAutoCompleteIndex index = TrieAutoCompleteIndex.init(2);
        SearchAutoCompleteService boundedService = new SearchAutoCompleteService(index);

        boundedService.recordQuery("car");
        boundedService.recordQuery("cart");
        boundedService.recordQuery("card");

        List<String> suggestions = boundedService.getSuggestions("ca");
        assertEquals(2, suggestions.size(), "Should only return top-K=2 results");
    }

    @Test
    @DisplayName("Should break ties lexicographically when frequencies are identical")
    void testDeterministicTieBreaking() {
        // Both have frequency 1
        service.recordQuery("apply");
        service.recordQuery("apple");

        List<String> suggestions = service.getSuggestions("app");
        assertEquals(List.of("apple", "apply"), suggestions, "'apple' must precede 'apply' lexicographically");
    }

    @Test
    @DisplayName("Should properly support phrases containing whitespace")
    void testPhrasesWithSpaces() {
        service.recordQuery("system design");
        service.recordQuery("system architecture");
        service.recordQuery("system design"); // 2 searches

        List<String> suggestions = service.getSuggestions("system ");
        assertEquals(List.of("system design", "system architecture"), suggestions);
    }

    @Test
    @DisplayName("Dynamic ranking: updated searches should promote queries to higher positions")
    void testDynamicRankPromotion() {
        service.recordQuery("banana"); // 1 search
        service.recordQuery("band");   // 2 searches
        service.recordQuery("band");

        List<String> initial = service.getSuggestions("ban");
        assertEquals(List.of("band", "banana"), initial);

        // Record "banana" twice more -> now 3 searches
        service.recordQuery("banana");
        service.recordQuery("banana");

        List<String> updated = service.getSuggestions("ban");
        assertEquals(List.of("banana", "band"), updated, "banana should now rank #1");
    }

    @Test
    @DisplayName("Validation: Invalid queries should throw IllegalArgumentException")
    void testValidationEdgeCases() {
        // Null query
        assertThrows(IllegalArgumentException.class, () -> service.getSuggestions(null));
        assertThrows(IllegalArgumentException.class, () -> service.recordQuery(null));

        // Blank query
        assertThrows(IllegalArgumentException.class, () -> service.getSuggestions("   "));
        assertThrows(IllegalArgumentException.class, () -> service.recordQuery(""));

        // Query length > 20
        assertThrows(IllegalArgumentException.class, () -> service.getSuggestions("thisstringiswaytoolongtofit"));

        // Uppercase or special characters
        assertThrows(IllegalArgumentException.class, () -> service.getSuggestions("Apple"));
        assertThrows(IllegalArgumentException.class, () -> service.recordQuery("hello123"));
        assertThrows(IllegalArgumentException.class, () -> service.recordQuery("fast-track"));
    }
}
