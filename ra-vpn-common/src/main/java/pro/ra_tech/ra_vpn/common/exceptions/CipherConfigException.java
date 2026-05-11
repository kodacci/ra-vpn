package pro.ra_tech.ra_vpn.common.exceptions;

public class CipherConfigException extends RuntimeException {
    public CipherConfigException(String message) {
        super(message);
    }

    public CipherConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
