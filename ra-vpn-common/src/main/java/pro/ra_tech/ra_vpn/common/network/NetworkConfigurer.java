package pro.ra_tech.ra_vpn.common.network;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_VPN_PACKET_SIZE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkConfigurer {
    public static void configureIface(String cidr, String iface) throws IOException, InterruptedException {
        // Remove all addresses
        new ProcessBuilder("ip", "addr", "flush", "dev", iface).start().waitFor();

        // Adds interface address
        new ProcessBuilder("ip", "addr", "add", cidr, "dev", iface).start().waitFor();

        // MTU
        new ProcessBuilder("ip", "link", "set", "dev", iface, "mtu", Integer.toString(MAX_VPN_PACKET_SIZE)).start().waitFor();

        // Interface up
        new ProcessBuilder("ip", "link", "set", "dev", iface, "up").start().waitFor();
    }
}
