package pro.ra_tech.ra_vpn.common.ip;

import lombok.val;

public class IpHeaderParser {
    public IpHeader parse(byte[] raw) {
        return parse(raw, 0);
    }

    public IpHeader parse(byte[] raw, int offset) {
        if (raw.length - offset < 23) {
            throw new IllegalArgumentException("Ip packet array too small - invalid or damaged ip packet");
        }

        val proto = Protocol.of(raw[offset + 9] & 0xFF);
        val src = Ip4Address.of(raw, offset + 12);
        val dst = Ip4Address.of(raw, offset + 16);

        return new IpHeader(proto, src, dst);
    }
}
