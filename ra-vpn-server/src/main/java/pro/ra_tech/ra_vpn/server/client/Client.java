package pro.ra_tech.ra_vpn.server.client;

import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;

public interface Client {
    String id();
    InetAddress virtualIp();
    InetSocketAddress realAddress();
    Channel channel();
    void lastSeen(Instant lastSeen);
    Instant lastSeen();
}
