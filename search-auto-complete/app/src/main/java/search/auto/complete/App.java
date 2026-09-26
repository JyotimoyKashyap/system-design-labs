package search.auto.complete;

import search.auto.complete.autocompleteengine.trieautocomplete.TrieAutoCompleteIndex;
import search.auto.complete.queryingestionengine.FlushPolicy;
import search.auto.complete.queryingestionengine.QueryIngestionBuffer;
import search.auto.complete.queryingestionengine.TimeIntervalFlushPolicy;
import search.auto.complete.service.SearchAutoCompleteService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    public String getGreeting() {
        return "Search Autocomplete Engine Initialized.";
    }

    public static void main(String[] args) {
        // 1. Initialize Engine Singleton with K = 5
        TrieAutoCompleteIndex.resetForTesting();
        TrieAutoCompleteIndex index = TrieAutoCompleteIndex.init(5);

        // 2. Initialize Asynchronous Ingestion Buffer with 5-second interval
        FlushPolicy flushPolicy = new TimeIntervalFlushPolicy(5, TimeUnit.SECONDS);
        QueryIngestionBuffer ingestionBuffer = new QueryIngestionBuffer(index, flushPolicy);
        ingestionBuffer.start();

        // 3. Initialize Gateway Service
        SearchAutoCompleteService service = new SearchAutoCompleteService(index, ingestionBuffer);

        // 4. Pre-seed initial corpus and flush immediately into the Trie
        seedData(service);
        ingestionBuffer.flush();

        // 5. Clean shutdown hook to stop policy and flush buffer on exit
        Runtime.getRuntime().addShutdownHook(new Thread(ingestionBuffer::stop));

        // 3. Launch Real-time Interactive Console UI
        try {
            runRealtimeConsole(service);
        } catch (Exception e) {
            System.err.println("Console UI encountered an error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void seedData(SearchAutoCompleteService service) {
        // Tech & Apple Ecosystem (varying frequencies)
        seedQuery(service, "apple", 12);
        seedQuery(service, "apple watch", 8);
        seedQuery(service, "apple macbook", 6);
        seedQuery(service, "application", 4);
        seedQuery(service, "apply", 2);
        seedQuery(service, "algorithm", 7);

        // Distributed Systems & System Design
        seedQuery(service, "distributed system", 15);
        seedQuery(service, "system design", 11);
        seedQuery(service, "system architecture", 7);
        seedQuery(service, "software engineer", 9);
        seedQuery(service, "software engineering", 5);
        seedQuery(service, "search engine", 6);

        // Databases & Data Science
        seedQuery(service, "database system", 8);
        seedQuery(service, "data structure", 7);
        seedQuery(service, "data science", 4);
    }

    private static void seedQuery(SearchAutoCompleteService service, String query, int count) {
        for (int i = 0; i < count; i++) {
            service.recordQuery(query);
        }
    }

    private static void setTerminalRawMode(boolean raw) {
        try {
            String cmd = raw ? "stty -icanon -echo min 1 < /dev/tty" : "stty sane < /dev/tty";
            new ProcessBuilder("/bin/sh", "-c", cmd).inheritIO().start().waitFor();
        } catch (Exception ignored) {
        }
    }

    public static void runRealtimeConsole(SearchAutoCompleteService service) {
        // Direct TTY streams bypass any sub-process line-buffering
        InputStream in;
        PrintStream out;
        try {
            in = new FileInputStream("/dev/tty");
        } catch (Exception e) {
            in = System.in;
        }

        try {
            out = new PrintStream(new FileOutputStream("/dev/tty"));
        } catch (Exception e) {
            out = System.out;
        }

        // Put terminal in non-canonical (raw), no-echo mode
        setTerminalRawMode(true);

        // Ensure terminal is cleanly restored on JVM shutdown or Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            setTerminalRawMode(false);
        }));

        StringBuilder currentQuery = new StringBuilder();
        String statusMessage = "Engine initialized with sample queries. Start typing!";

        render(out, currentQuery.toString(), Collections.emptyList(), statusMessage, -1);

        try {
            while (true) {
                int ch = in.read();

                if (ch == -1 || ch == 3 || ch == 4) { // EOF, Ctrl+C, Ctrl+D
                    break;
                }

                if (ch == 27) { // Escape key or escape sequence (e.g. arrow keys)
                    // If more bytes are pending, it's an arrow key / sequence (e.g. \033[A)
                    if (in.available() > 0) {
                        while (in.available() > 0) {
                            in.read(); // consume and ignore arrow navigation
                        }
                        continue;
                    } else {
                        // Standalone ESC pressed
                        break;
                    }
                }

                statusMessage = "";

                if (ch == 127 || ch == 8) { // Backspace / Delete
                    if (currentQuery.length() > 0) {
                        currentQuery.deleteCharAt(currentQuery.length() - 1);
                    }
                } else if (ch == '\r' || ch == '\n') { // Enter -> Commit search query
                    if (currentQuery.length() > 0) {
                        String queryToCommit = currentQuery.toString();
                        try {
                            service.recordQuery(queryToCommit);
                            statusMessage = "[+] Query committed & frequency incremented: \"" + queryToCommit + "\"";
                        } catch (IllegalArgumentException e) {
                            statusMessage = "[!] Cannot record: " + e.getMessage();
                        }
                        currentQuery.setLength(0);
                    }
                } else {
                    char c = (char) ch;
                    if (Character.isUpperCase(c)) {
                        c = Character.toLowerCase(c);
                    }

                    if ((c == ' ' || (c >= 'a' && c <= 'z')) && currentQuery.length() < 20) {
                        currentQuery.append(c);
                    }
                }

                List<String> suggestions = Collections.emptyList();
                long latencyNanos = -1;
                if (currentQuery.length() > 0) {
                    try {
                        long start = System.nanoTime();
                        suggestions = service.getSuggestions(currentQuery.toString());
                        latencyNanos = System.nanoTime() - start;
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                render(out, currentQuery.toString(), suggestions, statusMessage, latencyNanos);
            }
        } catch (Exception e) {
            // Handled gracefully on exit
        } finally {
            setTerminalRawMode(false);
            clearAndExit(out);
        }
    }

    // ANSI Color and Styling Tokens
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String COLOR_WHITE = "\033[38;5;255m";
    private static final String COLOR_PREFIX = "\033[38;5;221m";   // Warm Amber for matched prefix
    private static final String COLOR_TAIL = "\033[38;5;51m";      // Electric Cyan for auto-completed tail
    private static final String COLOR_MUTED = "\033[38;5;244m";    // Slate Gray for index and borders
    private static final String COLOR_SUCCESS = "\033[38;5;82m";   // Bright Emerald for committed status
    private static final String COLOR_BORDER = "\033[38;5;239m";   // Dark Border Gray
    private static final String COLOR_TITLE = "\033[38;5;39m";     // Vivid Sky Blue for Title
    private static final String BG_INPUT = "\033[48;5;237m";       // Sleek Dark Charcoal Grey for Input Box

    private static void render(PrintStream out, String query, List<String> suggestions, String statusMessage, long latencyNanos) {
        StringBuilder sb = new StringBuilder();
        // Clear screen and position cursor at row 1, col 1
        sb.append("\033[H\033[2J");
        sb.append(COLOR_BORDER).append("================================================================================").append(RESET).append("\n");
        sb.append("   ").append(BOLD).append(COLOR_TITLE).append("SEARCH AUTOCOMPLETE ENGINE").append(RESET).append(COLOR_MUTED).append(" — Real-Time Typeahead").append(RESET).append("\n");
        sb.append(COLOR_BORDER).append("================================================================================").append(RESET).append("\n");
        sb.append(COLOR_MUTED).append("  * Type characters -> Completions refresh INSTANTLY on each keystroke.\n");
        sb.append("  * Press [ENTER]   -> Commit & record query (boosts popularity ranking).\n");
        sb.append("  * Press [BACKSPACE] to delete  |  Press [ESC] or [Ctrl+C] to exit.").append(RESET).append("\n");
        sb.append(COLOR_BORDER).append("--------------------------------------------------------------------------------").append(RESET).append("\n\n");

        // Rectangular Greyish Input Box
        int boxInnerWidth = 64;
        String promptContent = "  > Type Here: " + query + "█";
        int paddingLen = Math.max(0, boxInnerWidth - promptContent.length());
        String fullPaddedRow = promptContent + " ".repeat(paddingLen);

        sb.append("  ").append(COLOR_BORDER).append("┌").append("─".repeat(boxInnerWidth)).append("┐").append(RESET).append("\n");
        sb.append("  ").append(COLOR_BORDER).append("│").append(RESET)
          .append(BG_INPUT).append(BOLD).append(COLOR_WHITE).append(fullPaddedRow).append(RESET)
          .append(COLOR_BORDER).append("│").append(RESET).append("\n");
        sb.append("  ").append(COLOR_BORDER).append("└").append("─".repeat(boxInnerWidth)).append("┘").append(RESET).append("\n\n");

        if (latencyNanos >= 0) {
            double ms = latencyNanos / 1_000_000.0;
            long micros = latencyNanos / 1_000;
            String latencyText = String.format("%.3f ms (%d µs)", ms, micros);
            String latencyColor = (ms < 1.0) ? COLOR_SUCCESS : (ms < 5.0 ? "\033[38;5;220m" : "\033[38;5;203m");

            sb.append(BOLD).append(COLOR_WHITE).append("  TOP SUGGESTIONS ").append(RESET)
              .append(COLOR_MUTED).append("[")
              .append(BOLD).append(latencyColor).append("⚡ ").append(latencyText).append(RESET)
              .append(COLOR_MUTED).append(" | ").append(suggestions.size()).append(" results]")
              .append(RESET).append(":\n");
        } else {
            sb.append(BOLD).append(COLOR_WHITE).append("  TOP SUGGESTIONS:").append(RESET).append("\n");
        }

        if (query.isEmpty()) {
            sb.append(COLOR_MUTED).append("    (Start typing a prefix to see suggestions...)").append(RESET).append("\n");
            for (int i = 0; i < 4; i++) {
                sb.append("\n");
            }
        } else if (suggestions.isEmpty()) {
            sb.append(COLOR_MUTED).append("    (No matching suggestions for \"").append(query).append("\")").append(RESET).append("\n");
            for (int i = 0; i < 4; i++) {
                sb.append("\n");
            }
        } else {
            for (int i = 0; i < 5; i++) {
                if (i < suggestions.size()) {
                    String item = suggestions.get(i);
                    String matchedPart = "";
                    String tailPart = item;

                    if (item.startsWith(query)) {
                        matchedPart = item.substring(0, query.length());
                        tailPart = item.substring(query.length());
                    }

                    sb.append("    ").append(COLOR_MUTED).append("[").append(i + 1).append("] ").append(RESET)
                      .append(BOLD).append(COLOR_PREFIX).append(matchedPart).append(RESET)
                      .append(BOLD).append(COLOR_TAIL).append(tailPart).append(RESET)
                      .append("\n");
                } else {
                    sb.append("\n");
                }
            }
        }

        sb.append("\n").append(COLOR_BORDER).append("================================================================================").append(RESET).append("\n");
        if (statusMessage != null && !statusMessage.isEmpty()) {
            sb.append("  ").append(BOLD).append(COLOR_SUCCESS).append(statusMessage).append(RESET).append("\n");
        } else {
            sb.append("\n");
        }

        out.print(sb.toString());
        out.flush();
    }

    private static void clearAndExit(PrintStream out) {
        out.print("\033[H\033[2J");
        out.println(BOLD + COLOR_TITLE + "\nExiting Search Autocomplete Engine. Goodbye!\n" + RESET);
        out.flush();
    }
}
