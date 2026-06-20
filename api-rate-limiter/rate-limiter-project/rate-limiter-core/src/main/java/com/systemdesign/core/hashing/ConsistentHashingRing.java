package com.systemdesign.core.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashingRing {

    private final int numOfReplicas;
    private final SortedMap<Long, String> circle = new TreeMap<>();

    public ConsistentHashingRing(int numOfReplicas, Collection<String> nodes) {
        this.numOfReplicas = numOfReplicas;
        for (String node : nodes) {
            addNode(node);
        }
    }

    public void addNode(String node) {
        for (int i=0; i<numOfReplicas; i++) {
            circle.put(hash(node + "VIRTUAL_NODE_" + i), node);
        }
    }

    public void removeNode(String node) {
        for (int i=0; i<numOfReplicas; i++) {
            circle.remove(hash(node + "VIRTUAL_NODE_" + i));
        }
    }

    public String getNode(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        long hash = hash(key);

        if (!circle.containsKey(hash)) {
            SortedMap<Long, String> tailMap = circle.tailMap(hash);

            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    private long hash(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(key.getBytes());
            return ((long) (digest[3] & 0xFF) << 24) |
                   ((long) (digest[2] & 0xFF) << 16) |
                   ((long) (digest[1] & 0xFF) << 8) |
                   ((long) (digest[0] & 0xFF));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

}
