package pro.ra_tech.ra_vpn.client.base;

import pro.ra_tech.ra_vpn.client.event.Connector;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.common.tun.VpnTunDevice;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public record ClientContext(
        InetSocketAddress server,
        VpnTunDevice tun,
        Supplier<PacketEncryptor> encryptorSupplier,
        Connector connector,
        String clientId,
        Reconnector reconnector
) {
}
