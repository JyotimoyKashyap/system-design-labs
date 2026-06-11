import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


record StorageNode(
        String name,
        String ip
    ) {
        
    }

public class ConsistentHashing {
    private List<StorageNode> nodes;
    private List<Integer> keys;
    private final int totalSlots;

    /**
     * Let's create a hash space of 16 slots
     * for this consistent hashing algorithm
     * 
     * And then we will place the {@StorageNode} in those
     */
    public ConsistentHashing(List<StorageNode> initialNodes, int totalSlots) {
        this.keys = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.totalSlots = totalSlots;

        for (StorageNode node : initialNodes) {
            addNode(node);
        }
    }

    public void addNode(StorageNode node) {
        if (nodes.size() >= totalSlots) {
            throw new IllegalStateException("The hash ring is completely full");
        }

        int hash = HashUtils.md5Hash(node.ip(), totalSlots);

        while (keys.contains(hash)) {
            hash = (hash + 1) % totalSlots;
        }

        int index = Collections.binarySearch(keys, hash);
        int insertionPoint = Math.abs(index + 1);

        keys.add(insertionPoint, hash);
        nodes.add(insertionPoint, node);
    }

    public void removeNode (StorageNode node) {
        int index = nodes.indexOf(node);
        if (index >= 0) {
            keys.remove(index);
            nodes.remove(index);
        }
    }

    public StorageNode getTargetNode(String dataKey) {
        if (keys.isEmpty()) {
            return null;
        }

        int keyHash = HashUtils.md5Hash(dataKey, totalSlots);

        int index = Collections.binarySearch(keys, keyHash);

        if (index < 0) {
            index = Math.abs(index + 1);
        }

        if (index == keys.size()) {
            index = 0;
        }

        return nodes.get(index);
    }


    public static void main(String[] args) {
        // Initialize a tiny ring with exactly 16 slots (0 to 15)
        int ringSize = 16;

        List<StorageNode> defaultNodes = new ArrayList<>();
        defaultNodes.add(new StorageNode("A", "235.191.161.1"));
        defaultNodes.add(new StorageNode("B", "123.666.999.2"));
        defaultNodes.add(new StorageNode("C", "987.444.234.3"));
        defaultNodes.add(new StorageNode("D", "367.765.456.4"));
        defaultNodes.add(new StorageNode("E", "342.873.637.5"));

        List<StorageNode> scaledNodes = new ArrayList<>(defaultNodes);
        scaledNodes.add(new StorageNode("G", "153.628.986.7"));
        scaledNodes.add(new StorageNode("H", "624.562.168.8"));

        System.out.println("--- Building the 16-Slot Ring ---");
        ConsistentHashing ring = new ConsistentHashing(defaultNodes, ringSize);

        System.out.println("\n--- Routing Data ---");
        String[] sampleKeys = {"alice_data", "bob_data", "charlie_data", "diana_data"};
        
        for (String key : sampleKeys) {
            StorageNode target = ring.getTargetNode(key);
            System.out.println("Routed to " + target);
        }
    }
}
