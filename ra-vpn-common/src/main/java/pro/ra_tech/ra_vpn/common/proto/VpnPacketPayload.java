package pro.ra_tech.ra_vpn.common.proto;

import java.net.InetAddress;

public interface VpnPacketPayload {
    byte[] toBytes();
    InetAddress srcAddress();
    InetAddress dstAddress();
}
