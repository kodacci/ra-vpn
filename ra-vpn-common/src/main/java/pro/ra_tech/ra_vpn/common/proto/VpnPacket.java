package pro.ra_tech.ra_vpn.common.proto;

public interface VpnPacket {
    VpnPacketType getType();
    VpnPacketPayload getPayload();
}
