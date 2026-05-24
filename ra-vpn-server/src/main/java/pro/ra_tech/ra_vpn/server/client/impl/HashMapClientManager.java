package pro.ra_tech.ra_vpn.server.client.impl;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.net.util.SubnetUtils;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.server.client.Client;
import pro.ra_tech.ra_vpn.server.client.ClientManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HashMapClientManager implements ClientManager {
    private final Map<InetAddress, Client> virtualMap = new HashMap<>();
    private final Map<InetSocketAddress, Client> realMap = new HashMap<>();
    private final Deque<InetAddress> reusableAddrs = new ArrayDeque<>();

    private SubnetUtils subnet;

    public HashMapClientManager(SubnetUtils subnetStart) {
        subnet = subnetStart;
    }

    private InetAddress getNextClientAddress() {
        if (reusableAddrs.isEmpty()) {
            subnet = subnet.getNext();

            return InetAddress.ofLiteral(subnet.getInfo().getAddress());
        }

        return reusableAddrs.removeFirst();
    }

    @Override
    public synchronized Client registerClient(String id, InetSocketAddress realAddress, Channel channel) {
        val client = realMap.get(realAddress);
        if (client != null) {
            return client;
        }

        val virtualIp = getNextClientAddress();
        val newClient = new VpnClient(
                id, virtualIp, realAddress, channel
        );

        virtualMap.put(newClient.virtualIp(), newClient);
        realMap.put(realAddress, newClient);
        log.info("Clients list updated: {}", virtualMap);

        return newClient;
    }

    @Override
    public synchronized @Nullable Client getClient(InetAddress virtualIp) {
        return virtualMap.get(virtualIp);
    }

    @Override
    public synchronized Collection<Client> getClients() {
        return new ArrayList<>(virtualMap.values());
    }

    @Override
    public synchronized void updateLastSeen(InetAddress virtualIp) {
        val client = virtualMap.get(virtualIp);
        if (client != null) {
            client.lastSeen(Instant.now());
        }
    }

    @Override
    public synchronized void forget(Client client) {
        if (virtualMap.remove(client.virtualIp()) != null) {
            reusableAddrs.addLast(client.virtualIp());
            realMap.remove(client.realAddress());
            log.info("Forgot client {}, clients: {}", client, virtualMap);
        }
    }
}
