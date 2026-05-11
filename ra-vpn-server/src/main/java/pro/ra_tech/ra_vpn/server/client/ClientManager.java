package pro.ra_tech.ra_vpn.server.client;

import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

public interface ClientManager {
    Client registerClient(String id, InetSocketAddress realAddress, Channel channel);
    Client getClient(InetAddress virtualIp);
    Collection<Client> getClients();
    void updateLastSeen(InetAddress virtualIp);
    void forget(Client client);
}
