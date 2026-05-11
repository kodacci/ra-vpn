package pro.ra_tech.ra_vpn.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final byte[] SIGNATURE = "RA-V".getBytes(StandardCharsets.US_ASCII);
    public static final int MAX_PACKET_SIZE = 1472;
    public static final int MAX_CLIENT_ID_LENGTH = 200;
    public static final int AES_BITS = 128;
    public static final int IV_SIZE = 12;
    public static final int MAX_PADDING_SIZE = 16;
    public static final int MAX_VPN_PACKET_SIZE = MAX_PACKET_SIZE - SIGNATURE.length - 2 - IV_SIZE - MAX_PADDING_SIZE;
}
