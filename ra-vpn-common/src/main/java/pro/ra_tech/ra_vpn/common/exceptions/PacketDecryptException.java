package pro.ra_tech.ra_vpn.common.exceptions;

public class PacketDecryptException extends RuntimeException {
    public PacketDecryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
