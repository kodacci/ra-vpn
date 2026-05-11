package pro.ra_tech.ra_vpn.common.proto;

import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectAckPayload;

@RequiredArgsConstructor
public class ConnectAckPacket implements VpnPacket {
    private final ConnectAckPayload payload;

    public static ConnectAckPacket fromBytes(byte[] bytes, int offset) {
        return new ConnectAckPacket(ConnectAckPayload.fromBytes(bytes, offset));
    }

    @Override
    public VpnPacketType getType() {
        return VpnPacketType.CONNECT_ACK;
    }

    @Override
    public VpnPacketPayload getPayload() {
        return payload;
    }
}
