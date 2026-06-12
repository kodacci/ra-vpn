package pro.ra_tech.ra_vpn.client.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.client.base.BaseClient;
import pro.ra_tech.ra_vpn.client.base.ClientContext;
import pro.ra_tech.ra_vpn.client.base.Reconnector;
import pro.ra_tech.ra_vpn.client.event.ConnectionWatchdog;
import pro.ra_tech.ra_vpn.client.event.Connector;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class UdpClient extends BaseClient {
    private Channel channel;

    public UdpClient(
            InetSocketAddress address,
            int tunNumber,
            String virtualIp,
            String clientId,
            EncryptorType encryptorType,
            @Nullable String encryptionKeyPath
    ) {
        super(address, tunNumber, virtualIp, clientId, encryptorType, encryptionKeyPath);
    }

    @Override
    protected void bootstrap(IoEventLoopGroup masterGroup, ClientContext ctx) throws InterruptedException {
        val boot = new Bootstrap();
        boot.group(masterGroup)
                .channel(NioDatagramChannel.class)
                .handler(new UdpClientInitializer(ctx));

        channel = boot.bind(0).sync().channel();
    }

    @Override
    protected ClientType getType() {
        return ClientType.UDP;
    }

    @Override
    protected Supplier<Channel> getChannelSupplier() {
        return () -> channel;
    }

    @Override
    protected Reconnector getReconnector() {
        return () -> {};
    }

    @Override
    protected void scheduleWatchdog(ScheduledExecutorService executor, Connector connector) {
        executor.scheduleAtFixedRate(
                new ConnectionWatchdog(connector, KEEP_ALIVE_TIMEOUT),
                CONNECTION_INTERVAL_SEC, CONNECTION_INTERVAL_SEC, TimeUnit.SECONDS
        );
    }
}
