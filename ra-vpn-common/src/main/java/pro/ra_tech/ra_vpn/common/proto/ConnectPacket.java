package pro.ra_tech.ra_vpn.common.proto;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectPayload;

import java.net.InetAddress;

@ToString
@RequiredArgsConstructor
public class ConnectPacket implements VpnPacket {
    private final ConnectPayload payload;

    public static ConnectPacket fromBytes(byte[] bytes, int offset) {
        return new ConnectPacket(ConnectPayload.fromBytes(bytes, offset));
    }

    public ConnectPacket(String clientId, InetAddress src, InetAddress dst) {
        payload = new ConnectPayload(clientId, src, dst);
    }

    @Override
    public VpnPacketType getType() {
        return VpnPacketType.CONNECT;
    }

    @Override
    public ConnectPayload getPayload() {
        return payload;
    }
}
