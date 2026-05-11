package pro.ra_tech.ra_vpn.common.ip;

public record IpHeader(
        Protocol proto,
        Ip4Address srcAddress,
        Ip4Address dstAddress
) {
}
