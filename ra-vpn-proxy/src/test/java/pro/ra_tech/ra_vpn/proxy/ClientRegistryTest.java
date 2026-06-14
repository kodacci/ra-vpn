package pro.ra_tech.ra_vpn.proxy;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientRegistryTest {
    private static final InetAddress VIRTUAL_IP = InetAddress.ofLiteral("10.0.0.2");
    private static final InetSocketAddress CLIENT = new InetSocketAddress("198.51.100.7", 5000);

    @Test
    void tracksNewClientAndResolvesItsRealAddress() {
        val registry = new ClientRegistry();

        assertTrue(registry.track(VIRTUAL_IP, CLIENT), "first association should be reported as new");
        assertEquals(CLIENT, registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void reportsRepeatedSameAssociationAsNotNew() {
        val registry = new ClientRegistry();

        registry.track(VIRTUAL_IP, CLIENT);
        assertFalse(registry.track(VIRTUAL_IP, CLIENT), "unchanged mapping should not be reported as new");
    }

    @Test
    void reportsChangedRealAddressAsNew() {
        val registry = new ClientRegistry();
        val roamedClient = new InetSocketAddress("203.0.113.9", 6000);

        registry.track(VIRTUAL_IP, CLIENT);

        assertTrue(registry.track(VIRTUAL_IP, roamedClient), "changed address should be reported as new");
        assertEquals(roamedClient, registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void returnsNullForUnknownClient() {
        val registry = new ClientRegistry();

        assertNull(registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void forgetsTrackedClient() {
        val registry = new ClientRegistry();
        registry.track(VIRTUAL_IP, CLIENT);

        registry.forget(VIRTUAL_IP);

        assertNull(registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void forgettingUnknownClientIsNoOp() {
        val registry = new ClientRegistry();

        registry.forget(VIRTUAL_IP);

        assertNull(registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void keepsRecentlySeenClientOnExpirySweep() {
        val registry = new ClientRegistry();
        registry.track(VIRTUAL_IP, CLIENT);

        registry.forgetExpired(Duration.ofMinutes(1));

        assertEquals(CLIENT, registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void forgetsStaleClientOnExpirySweep() throws InterruptedException {
        val registry = new ClientRegistry();
        registry.track(VIRTUAL_IP, CLIENT);

        // Let the last-seen timestamp age past the (tiny) timeout below.
        Thread.sleep(10);
        registry.forgetExpired(Duration.ofMillis(1));

        assertNull(registry.findRealAddress(VIRTUAL_IP));
    }

    @Test
    void expirySweepRefreshedByTracking() throws InterruptedException {
        val registry = new ClientRegistry();
        registry.track(VIRTUAL_IP, CLIENT);

        Thread.sleep(10);
        // A fresh packet refreshes last-seen, so the client survives the sweep.
        registry.track(VIRTUAL_IP, CLIENT);
        registry.forgetExpired(Duration.ofMillis(5));

        assertEquals(CLIENT, registry.findRealAddress(VIRTUAL_IP));
    }
}
