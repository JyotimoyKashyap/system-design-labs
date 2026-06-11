import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static int md5Hash(String key, int ringSize) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));

            // We take the first 4 bytes of the 16-byte MD5 hash to create a 32-bit integer.
            // Using bitwise operations ensures we handle the bytes purely as unsigned values.
            int fullHash = ((digest[3] & 0xFF) << 24)
                 | ((digest[2] & 0xFF) << 16)
                 | ((digest[1] & 0xFF) << 8)
                 | (digest[0] & 0xFF);

            return (fullHash & 0x7FFFFFFF) % ringSize;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found in JDK", e);
        }
    }
}
