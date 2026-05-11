package pro.ra_tech.ra_vpn.common.ip;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Protocol {
    ICMP(1),
    IGMP(2),
    GGP(3),
    TCP(6),
    UDP(17),
    NARP(54),
    OTHER(255);

    private final int value;

    public static Protocol of(int id) {
        return switch (id) {
            case 1 -> ICMP;
            case 2 -> IGMP;
            case 3 -> GGP;
            case 6 -> TCP;
            case 17 -> UDP;
            case 54 -> NARP;
            default -> OTHER;
        };
    }
}
