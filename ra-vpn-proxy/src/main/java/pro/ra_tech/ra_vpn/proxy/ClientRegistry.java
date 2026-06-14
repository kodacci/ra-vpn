package pro.ra_tech.ra_vpn.proxy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks the real (transport) address of each connected client keyed by its VPN virtual IP.
 * <p>
 * Packets travelling from a client to the server carry the client virtual IP as the inner IP
 * source address, so observing a client packet lets the proxy learn where to deliver the
 * server's responses, which carry the same virtual IP as their inner destination address.
 * <p>
 * Each association records the time its last client packet was seen so idle clients can be
 * {@linkplain #forgetExpired(Duration) expired}; clients are also dropped explicitly when they
 * {@linkplain #forget(InetAddress) disconnect}.
 */
@Slf4j
public class ClientRegistry {
    private record Client(InetSocketAddress realAddress, Instant lastSeen) {}

    private final ConcurrentMap<InetAddress, Client> clients = new ConcurrentHashMap<>();

    /**
     * Associates a client virtual IP with the real address its packets came from and refreshes the
     * client's last-seen timestamp.
     *
     * @return {@code true} when this is a new association (or the address changed), {@code false}
     *         when the same mapping was already present.
     */
    public boolean track(InetAddress virtualIp, InetSocketAddress realAddress) {
        val previous = clients.put(virtualIp, new Client(realAddress, Instant.now()));
        return previous == null || !realAddress.equals(previous.realAddress());
    }

    @Nullable
    public InetSocketAddress findRealAddress(InetAddress virtualIp) {
        val client = clients.get(virtualIp);
        return client == null ? null : client.realAddress();
    }

    /**
     * Drops the association for the given client virtual IP, e.g. after a DISCONNECT.
     */
    public void forget(InetAddress virtualIp) {
        if (clients.remove(virtualIp) != null) {
            log.info("Forgot client {}", virtualIp.getHostAddress());
        }
    }

    /**
     * Drops every client whose last packet was seen longer ago than {@code timeout}.
     */
    public void forgetExpired(Duration timeout) {
        val deadline = Instant.now().minus(timeout);
        clients.entrySet().removeIf(entry -> {
            val expired = entry.getValue().lastSeen().isBefore(deadline);
            if (expired) {
                log.info("Forgetting stale client {} at {}", entry.getKey().getHostAddress(), entry.getValue().realAddress());
            }
            return expired;
        });
    }
}
