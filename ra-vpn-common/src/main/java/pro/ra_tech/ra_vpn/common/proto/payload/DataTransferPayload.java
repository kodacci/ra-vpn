package pro.ra_tech.ra_vpn.common.proto.payload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.ip.IpHeaderParser;
import pro.ra_tech.ra_vpn.common.proto.VpnPacketPayload;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public record DataTransferPayload (
        byte[] data,
        InetAddress srcAddress,
        InetAddress dstAddress
) implements VpnPacketPayload {
    private static final IpHeaderParser parser = new IpHeaderParser();

    public static DataTransferPayload fromBytes(byte[] bytes, int offset, int length) {
        val payload = new byte[length];
        System.arraycopy(bytes, offset, payload, 0, length);
        val header = parser.parse(payload);
        try {
            return new DataTransferPayload(
                    payload,
                    header.srcAddress().toInetAddress(),
                    header.dstAddress().toInetAddress()
            );
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Bad DATA_TRANSFER payload", ex);
        }
    }

    @Override
    public byte[] toBytes() {
        return data;
    }
}
