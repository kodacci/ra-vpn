package pro.ra_tech.ra_vpn.common.tun;

import pro.ra_tech.ra_vpn.common.ip.IpHeader;

public record TunPacket(
        IpHeader header,
        byte[] packet
) {
}
