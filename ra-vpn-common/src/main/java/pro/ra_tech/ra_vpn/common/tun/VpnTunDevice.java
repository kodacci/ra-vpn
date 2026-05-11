package pro.ra_tech.ra_vpn.common.tun;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public interface VpnTunDevice extends AutoCloseable {
    boolean isInitialized();
    void setVirtualIp(InetAddress virtualIp, InetAddress gatewayIp) throws IOException;
    @Nullable InetAddress getVirtualIp();
    String getName();
    byte[] read() throws IOException;
    void write(ByteBuffer packet) throws IOException;
    InetAddress getGatewayIp();
}
