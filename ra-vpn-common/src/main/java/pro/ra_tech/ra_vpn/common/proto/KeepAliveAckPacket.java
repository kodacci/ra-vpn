package pro.ra_tech.ra_vpn.common.proto;

import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectPayload;
import java.net.InetAddress;

@RequiredArgsConstructor
public class KeepAliveAckPacket implements VpnPacket {
    private final ConnectPayload payload;

    public static KeepAliveAckPacket fromBytes(byte[] bytes, int offset) {
        return new KeepAliveAckPacket(ConnectPayload.fromBytes(bytes, offset));
    }

    public KeepAliveAckPacket(String clientId, InetAddress srcAddress, InetAddress dstAddress) {
        payload = new ConnectPayload(clientId, srcAddress, dstAddress);
    }

    @Override
    public VpnPacketType getType() {
        return VpnPacketType.KEEP_ALIVE_ACK;
    }

    @Override
    public VpnPacketPayload getPayload() {
        return payload;
    }
}
