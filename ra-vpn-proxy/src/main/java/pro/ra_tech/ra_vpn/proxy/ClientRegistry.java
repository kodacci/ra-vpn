package pro.ra_tech.ra_vpn.proxy;

import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks the real (transport) address of each connected client keyed by its VPN virtual IP.
 * <p>
 * Packets travelling from a client to the server carry the client virtual IP as the inner IP
 * source address, so observing a client packet lets the proxy learn where to deliver the
 * server's responses, which carry the same virtual IP as their inner destination address.
 */
public class ClientRegistry {
    private final ConcurrentMap<InetAddress, InetSocketAddress> clients = new ConcurrentHashMap<>();

    /**
     * Associates a client virtual IP with the real address its packets came from.
     *
     * @return {@code true} when this is a new association (or the address changed), {@code false}
     *         when the same mapping was already present.
     */
    public boolean track(InetAddress virtualIp, InetSocketAddress realAddress) {
        return !realAddress.equals(clients.put(virtualIp, realAddress));
    }

    @Nullable
    public InetSocketAddress findRealAddress(InetAddress virtualIp) {
        return clients.get(virtualIp);
    }
}
