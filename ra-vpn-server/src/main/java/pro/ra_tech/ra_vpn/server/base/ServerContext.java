package pro.ra_tech.ra_vpn.server.base;

import org.apache.commons.net.util.SubnetUtils;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.common.tun.VpnTunDevice;
import pro.ra_tech.ra_vpn.server.client.ClientManager;

import java.net.InetAddress;
import java.util.function.Supplier;

public record ServerContext(
        VpnTunDevice tun,
        ClientManager clientManager,
        Supplier<PacketEncryptor> encryptorSupplier,
        InetAddress serverVirtualIp,
        SubnetUtils.SubnetInfo subnetInfo
) {
}
