package pro.ra_tech.ra_vpn.common.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public record Ip4Address(
        byte[] address
) {
    public static Ip4Address of(byte[] raw, int offset) {
        return new Ip4Address(Arrays.copyOfRange(raw, offset, offset + 4));
    }

    public InetAddress toInetAddress() throws UnknownHostException {
        return InetAddress.getByAddress(address);
    }

    @Override
    public String toString() {
        return (address[0] & 0xff) + "." + (address[1] & 0xff) + "." + (address[2] & 0xff) + "." + (address[3] & 0xff);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (other instanceof Ip4Address(byte[] right)) {
            return Arrays.equals(address, right);
        }

        return false;
    }
}
