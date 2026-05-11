package pro.ra_tech.ra_vpn.common.proto.payload;

import lombok.val;
import pro.ra_tech.ra_vpn.common.bin.BinHelper;
import pro.ra_tech.ra_vpn.common.proto.VpnPacketPayload;

import java.net.InetAddress;

public record ConnectAckPayload (
        InetAddress srcAddress,
        InetAddress dstAddress
) implements VpnPacketPayload {
    public static ConnectAckPayload fromBytes(byte[] bytes, int offset) {
        try {
            return new ConnectAckPayload(
                    BinHelper.toInetAddress(bytes, offset),
                    BinHelper.toInetAddress(bytes, offset + 4)
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("CONNECT_ACK: Invalid addresses", ex);
        }
    }

    @Override
    public byte[] toBytes() {
        val data = new byte[8];
        System.arraycopy(srcAddress.getAddress(), 0, data, 0, 4);
        System.arraycopy(dstAddress.getAddress(), 0, data, 4, 4);

        return data;
    }
}
