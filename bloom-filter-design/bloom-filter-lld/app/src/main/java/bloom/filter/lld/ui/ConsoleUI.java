package bloom.filter.lld.ui;

import bloom.filter.lld.core.DefaultBloomFilter;
import java.util.Scanner;
import java.util.BitSet;

public class ConsoleUI {
    private final DefaultBloomFilter<String> bloomFilter;
    private final Scanner scanner;

    public ConsoleUI(DefaultBloomFilter<String> bloomFilter) {
        this.bloomFilter = bloomFilter;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            renderFrame();
            System.out.print("\nEnter option [1] Add 1 Key [2] Add 10 Keys [3] Reset [4] Exit: ");
            String input = scanner.nextLine();
            
            switch (input.trim()) {
                case "1":
                    bloomFilter.addKey("key_" + System.nanoTime());
                    break;
                case "2":
                    for (int i = 0; i < 10; i++) {
                        bloomFilter.addKey("key_" + System.nanoTime() + "_" + i);
                    }
                    break;
                case "3":
                    bloomFilter.reset();
                    break;
                case "4":
                    System.out.print("\033[H\033[2J"); // Clear screen on exit
                    System.out.flush();
                    System.out.println("Exiting Bloom Filter Visualizer...");
                    return;
                default:
                    // Ignored
                    break;
            }
        }
    }

    private void renderFrame() {
        // ANSI escape codes to clear screen and move cursor to top-left (0,0)
        System.out.print("\033[H\033[2J");
        System.out.flush();

        StringBuilder sb = new StringBuilder();
        sb.append("==============================================================\n");
        sb.append("                 BLOOM FILTER VISUALIZER                      \n");
        sb.append("==============================================================\n");
        sb.append(String.format("Bit Array Size (m) : %d\n", bloomFilter.getBitArraySize()));
        sb.append(String.format("Hash Functions (k) : %d\n", bloomFilter.getKHashFunctions()));
        sb.append(String.format("Total Keys Added   : %d\n", bloomFilter.getKeysAdded()));
        
        // Calculate current Theoretical FPR: (1 - e^(-k * keys / m))^k
        double currentFpr = 0.0;
        if (bloomFilter.getKeysAdded() > 0) {
            double exponent = -1.0 * bloomFilter.getKHashFunctions() * bloomFilter.getKeysAdded() / bloomFilter.getBitArraySize();
            currentFpr = Math.pow(1 - Math.exp(exponent), bloomFilter.getKHashFunctions());
        }
        double targetFprLimit = 0.05; // 5% limit as configured in Main
        if (currentFpr > targetFprLimit) {
            // ANSI Red color for crossing limit
            sb.append(String.format("\033[31mCurrent FPR        : %.4f%%\033[0m\n", currentFpr * 100));
        } else {
            sb.append(String.format("Current FPR        : %.4f%%\n", currentFpr * 100));
        }
        sb.append("==============================================================\n\n");
        
        // Bit Array Visualizer
        sb.append("Bit Array State:\n");
        BitSet bits = bloomFilter.getBitSet();
        int m = bloomFilter.getBitArraySize();
        
        for (int i = 0; i < m; i++) {
            if (bits.get(i)) {
                // ANSI Green without brackets
                sb.append("\033[32m█\033[0m");
            } else {
                // Unset bit
                sb.append("░");
            }
            // Wrap text every 80 elements so it fits nicely
            if ((i + 1) % 80 == 0) {
                sb.append("\n");
            }
        }
        sb.append("\n");
        
        System.out.print(sb.toString());
    }
}
