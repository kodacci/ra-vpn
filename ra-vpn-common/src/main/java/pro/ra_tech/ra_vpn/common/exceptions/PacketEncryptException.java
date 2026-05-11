package pro.ra_tech.ra_vpn.common.exceptions;

public class PacketEncryptException extends RuntimeException {
    public PacketEncryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
