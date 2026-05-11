package pro.ra_tech.ra_vpn.common.proto.payload;

import lombok.val;
import pro.ra_tech.ra_vpn.common.bin.BinHelper;
import pro.ra_tech.ra_vpn.common.proto.VpnPacketPayload;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_CLIENT_ID_LENGTH;

public record ConnectPayload(
        String clientId,
        InetAddress srcAddress,
        InetAddress dstAddress
) implements VpnPacketPayload {
    private static final int PAYLOAD_LENGTH = MAX_CLIENT_ID_LENGTH + 8;

    public static ConnectPayload fromBytes(byte[] bytes, int offset) {
        if (bytes.length < PAYLOAD_LENGTH + offset) {
            throw new IllegalArgumentException("CONNECT: Insufficient bytes length " + bytes.length);
        }

        try {
            val id = BinHelper.toClientId(bytes, offset);
            val srcAddress = BinHelper.toInetAddress(bytes, MAX_CLIENT_ID_LENGTH + offset);
            val dstAddress = BinHelper.toInetAddress(bytes, MAX_CLIENT_ID_LENGTH + offset + 4);

            return new ConnectPayload(id, srcAddress, dstAddress);
        } catch (Exception ex) {
            throw new IllegalArgumentException("CONNECT: Invalid payload", ex);
        }
    }

    @Override
    public byte[] toBytes() {
        val source = clientId.getBytes(StandardCharsets.UTF_8);
        val bytes = new byte[PAYLOAD_LENGTH];
        System.arraycopy(source, 0, bytes, 0, Math.min(source.length, MAX_CLIENT_ID_LENGTH));
        System.arraycopy(srcAddress.getAddress(), 0, bytes, MAX_CLIENT_ID_LENGTH, 4);
        System.arraycopy(dstAddress.getAddress(), 0, bytes, MAX_CLIENT_ID_LENGTH + 4, 4);

        return bytes;
    }
}
