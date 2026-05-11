package pro.ra_tech.ra_vpn.server.exceptions;

public class ServerStartException extends RuntimeException {
    public ServerStartException(String message, Throwable cause) {
        super("Error starting vpn server: " + message, cause);
    }
}
