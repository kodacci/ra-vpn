package pro.ra_tech.ra_vpn.client.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.client.base.BaseClient;
import pro.ra_tech.ra_vpn.client.base.ClientContext;
import pro.ra_tech.ra_vpn.client.base.Reconnector;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class TcpClient extends BaseClient implements ChannelFutureListener {
    private Channel channel;
    private Bootstrap boot;

    public TcpClient(
            InetSocketAddress server,
            int tunNumber,
            String virtualIp,
            String clientId,
            EncryptorType encryptorType,
            @Nullable String encryptorKeyFilePath
    ) {
        super(server, tunNumber, virtualIp, clientId, encryptorType, encryptorKeyFilePath);
    }

    private void connect() {
        try {
            channel = boot.connect(getServer()).addListener(this).sync().channel();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void bootstrap(IoEventLoopGroup masterGroup, ClientContext ctx) throws InterruptedException {
        boot = new Bootstrap();
        boot.group(masterGroup);
        boot.channel(NioSocketChannel.class);
        boot.option(ChannelOption.SO_KEEPALIVE, true);
        boot.option(ChannelOption.TCP_NODELAY, true);
        boot.handler(new TcpClientInitializer(ctx));

        connect();
    }

    @Override
    protected ClientType getType() {
        return ClientType.TCP;
    }

    @Override
    protected Supplier<Channel> getChannelSupplier() {
        return () -> channel;
    }

    @Override
    protected Reconnector getReconnector() {
        return this::connect;
    }

    @Override
    public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            log.error("Error connecting to server, retrying...");
            future.channel().eventLoop().schedule(this::connect, 1, TimeUnit.SECONDS);
        }
    }
}
