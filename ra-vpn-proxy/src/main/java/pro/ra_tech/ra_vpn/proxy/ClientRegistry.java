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
 * The server, not the client, owns the virtual IP: it assigns one from its CIDR pool and returns
 * it in the CONNECT_ACK. The proxy therefore cannot learn the virtual IP from the CONNECT (whose
 * inner source address is only the client's requested IP). Instead it records the connecting
 * client's real address against its {@code clientId} as {@linkplain #addPending pending}, and
 * {@linkplain #bind binds} it to the server-assigned virtual IP when the matching CONNECT_ACK
 * (which echoes the same {@code clientId}) comes back.
 * <p>
 * Once bound, packets from a client carry that virtual IP as the inner source address and the
 * server's responses carry it as the inner destination address, so each association records the
 * time its last client packet was seen and idle clients can be {@linkplain #forgetExpired expired};
 * clients are also dropped explicitly when they {@linkplain #forget(InetAddress) disconnect}.
 */
@Slf4j
public class ClientRegistry {
    private record Client(InetSocketAddress realAddress, Instant lastSeen) {}

    private final ConcurrentMap<InetAddress, Client> clients = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, InetSocketAddress> pending = new ConcurrentHashMap<>();

    /**
     * Records a connecting client's real address against its {@code clientId} while it awaits a
     * CONNECT_ACK. A client retries CONNECT until acknowledged, so this overwrites any prior entry.
     */
    public void addPending(String clientId, InetSocketAddress realAddress) {
        pending.put(clientId, realAddress);
    }

    /**
     * Binds the server-assigned virtual IP from a CONNECT_ACK to the client that originated the
     * matching CONNECT, and returns that client's real address so the ack can be delivered.
     * <p>
     * Falls back to the existing mapping when no pending entry is found, so a re-sent CONNECT_ACK
     * (the server re-acks a client that retried CONNECT) is still routed.
     *
     * @return the client's real address, or {@code null} if the client is neither pending nor known.
     */
    @Nullable
    public InetSocketAddress bind(String clientId, InetAddress virtualIp) {
        val realAddress = pending.remove(clientId);
        if (realAddress != null) {
            clients.put(virtualIp, new Client(realAddress, Instant.now()));
            log.info("Bound client {} to virtual IP {} at {}", clientId, virtualIp.getHostAddress(), realAddress);
            return realAddress;
        }

        return findRealAddress(virtualIp);
    }

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
