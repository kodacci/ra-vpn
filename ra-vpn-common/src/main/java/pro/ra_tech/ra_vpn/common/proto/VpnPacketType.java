package pro.ra_tech.ra_vpn.common.proto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum VpnPacketType {
    CONNECT((byte) 0),
    CONNECT_ACK((byte) 1),
    DISCONNECT((byte) 2),
    KEEP_ALIVE((byte) 3),
    KEEP_ALIVE_ACK((byte) 4),
    DATA_TRANSFER((byte) 5),
    UNKNOWN((byte) 255);

    @Getter
    private final byte code;

    public static VpnPacketType of(byte code) {
        return switch (code) {
            case 0 -> CONNECT;
            case 1 -> CONNECT_ACK;
            case 2 -> DISCONNECT;
            case 3 -> KEEP_ALIVE;
            case 4 -> KEEP_ALIVE_ACK;
            case 5 -> DATA_TRANSFER;
            default -> UNKNOWN;
        };
    }
}
