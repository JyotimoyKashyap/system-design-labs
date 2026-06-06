package bloom.filter.lld.hashing;

import java.nio.charset.StandardCharsets;

public class StringFunnelStrategy implements FunnelStrategy<String>{

    @Override
    public byte[] funnel(String key) {
        if (key == null) {
            return new byte[0];
        }

        return key.getBytes(StandardCharsets.UTF_8);
    }


}
