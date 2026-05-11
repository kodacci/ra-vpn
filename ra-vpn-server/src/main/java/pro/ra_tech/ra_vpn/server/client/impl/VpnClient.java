package pro.ra_tech.ra_vpn.server.client.impl;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import pro.ra_tech.ra_vpn.server.client.Client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;

@ToString
@RequiredArgsConstructor
public class VpnClient implements Client {
    @Getter
    @Accessors(fluent = true)
    private final String id;
    @Getter
    @Accessors(fluent = true)
    private final InetAddress virtualIp;
    @Getter
    @Accessors(fluent = true)
    private final InetSocketAddress realAddress;
    @Getter
    @Accessors(fluent = true)
    private final Channel channel;

    private Instant lastSeen = Instant.now();

    @Override
    public void lastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public Instant lastSeen() {
        return lastSeen;
    }
}
