package pro.ra_tech.ra_vpn.server.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;
import pro.ra_tech.ra_vpn.common.ip.IpHeader;
import pro.ra_tech.ra_vpn.server.base.BaseServer;
import pro.ra_tech.ra_vpn.server.base.ServerContext;

@Slf4j
public class UdpServer extends BaseServer {
    private Channel channel;

    public UdpServer(
            String host,
            int port,
            int tunNumber,
            String sidr,
            EncryptorType encryptorType,
            @Nullable String encryptionKeyPath
    ) {
        super(
                host,
                port,
                tunNumber,
                sidr,
                encryptorType,
                encryptionKeyPath
        );
    }

    @Override
    protected void bootstrap(ServerContext serverCtx, IoEventLoopGroup masterGroup) throws InterruptedException {
        val boot = new Bootstrap();
        boot.group(masterGroup)
                .channel(NioDatagramChannel.class)
                .handler(new UdpServerChannelInitializer(serverCtx));

        channel = boot.bind(getHost(), getPort()).sync().channel();

    }

    @Override
    protected Channel findChannel(IpHeader header) {
        return channel;
    }

    @Override
    protected ServerType getType() {
        return ServerType.UDP;
    }
}
