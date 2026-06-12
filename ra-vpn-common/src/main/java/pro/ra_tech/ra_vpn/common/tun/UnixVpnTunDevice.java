package pro.ra_tech.ra_vpn.common.tun;

import io.github.isotes.net.tun.io.TunDevice;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.network.NetworkConfigurer;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class UnixVpnTunDevice implements VpnTunDevice {
    private final int tunNumber;

    private TunDevice tun;
    @Getter
    private InetAddress virtualIp;
    private InetAddress gatewayIp;

    @Override
    public boolean isInitialized() {
        return tun != null;
    }

    @Override
    public void setVirtualIp(InetAddress virtualIp, InetAddress gatewayIp) throws IOException {
        if (virtualIp.equals(this.virtualIp) && tun != null) {
            return;
        }

        // The interface is reused across reconnections; reopening the same tun
        // number while the previous fd is still being torn down causes EBUSY.
        // Keep the device open and just reconfigure the interface address.
        if (tun == null) {
            tun = TunDevice.open(tunNumber);
        }

        try {
            val cidr = virtualIp.getHostAddress() + "/24";
            log.info("Setting cidr {} to tun device {}", cidr, tun.getName());
            NetworkConfigurer.configureIface(cidr, tun.getName());
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } finally {
            this.virtualIp = virtualIp;
            this.gatewayIp = gatewayIp;
        }
    }

    @Override
    public String getName() {
        if (tun == null) {
            return "";
        }

        return tun.getName();
    }

    @Override
    public byte[] read() throws IOException {
        if (tun == null) {
            throw new IllegalStateException("Tun device not initialized");
        }

        return tun.readIPv4Packet().bytes();
    }

    @Override
    public void write(ByteBuffer packet) throws IOException {
        if (tun == null) {
            throw new IllegalStateException("Tun device not initialized");
        }

        tun.write(packet);
    }

    @Override
    public InetAddress getGatewayIp() {
        if (gatewayIp == null) {
            return InetAddress.ofLiteral("0.0.0.0");
        }

        return gatewayIp;
    }

    @Override
    public void close() throws Exception {
        if (tun != null) {
            tun.close();
            tun = null;
            virtualIp = null;
        }
    }
}
