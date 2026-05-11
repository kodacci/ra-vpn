package pro.ra_tech.ra_vpn.common.tun;

import info.skyblond.jna.wintun.WintunAdapter;
import info.skyblond.jna.wintun.WintunSession;
import lombok.Getter;
import lombok.val;
import pro.ra_tech.ra_vpn.common.exceptions.WintunReadException;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

import static com.sun.jna.platform.win32.IPHlpAPI.AF_INET;
import static pro.ra_tech.ra_vpn.common.Constants.MAX_VPN_PACKET_SIZE;

public class WinVpnTunDevice implements VpnTunDevice {
    private final byte[] noPacket = new byte[0];

    private final WintunAdapter tun;
    private WintunSession session;

    @Getter
    private InetAddress virtualIp;
    private InetAddress gatewayIp;

    public WinVpnTunDevice(String name) {
        tun = new WintunAdapter(name, "Wintun", UUID.randomUUID().toString());
    }

    @Override
    public boolean isInitialized() {
        return session != null;
    }

    @Override
    public void setVirtualIp(InetAddress virtualIp, InetAddress gatewayIp) {
        if (virtualIp.equals(this.virtualIp) && session != null) {
            return;
        }

        if (session != null) {
            session.close();
        }

        session = tun.newSession(0x800000);
        tun.associateIp(virtualIp, 24);
        tun.setMTU(AF_INET, MAX_VPN_PACKET_SIZE);
        tun.setDefaultAdapter();

        this.virtualIp = virtualIp;
        this.gatewayIp = gatewayIp;
    }

    @Override
    public String getName() {
        return tun.getName();
    }

    @Override
    public byte[] read() {
        if (session == null) {
            throw new IllegalStateException("Win tun device is not configured properly");
        }

        try {
            val packet =  session.readPacket();

            return packet != null ? packet : noPacket;
        } catch (Exception ex) {
            throw new WintunReadException("Error reading packet from wintun " + getName(), ex);
        }
    }

    @Override
    public void write(ByteBuffer packet) {
        if (session == null) {
            throw new IllegalStateException("Win tun device is not configured properly");
        }

        try {
            session.writePacket(packet.array(), packet.arrayOffset(), packet.limit());
        } catch (Exception ex) {
            throw new WintunReadException("Error writing packet to wintun " + getName(), ex);
        }
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
        if (session != null) {
            session.close();
        }

        if (virtualIp != null) {
            tun.dissociateIp(virtualIp);
            virtualIp = null;
        }
    }
}
