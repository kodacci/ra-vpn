package pro.ra_tech.ra_vpn.common.proto;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectPayload;

import java.net.InetAddress;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DisconnectPacket implements VpnPacket {
    private final ConnectPayload payload;

    public static DisconnectPacket fromBytes(byte[] data, int offset) {
        return new DisconnectPacket(ConnectPayload.fromBytes(data, offset));
    }

    public DisconnectPacket(String clientId, InetAddress src, InetAddress dst) {
        payload = new ConnectPayload(clientId, src, dst);
    }

    @Override
    public VpnPacketType getType() {
        return VpnPacketType.DISCONNECT;
    }

    @Override
    public VpnPacketPayload getPayload() {
        return payload;
    }
}
