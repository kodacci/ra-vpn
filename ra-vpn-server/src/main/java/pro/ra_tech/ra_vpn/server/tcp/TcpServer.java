package pro.ra_tech.ra_vpn.server.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;
import pro.ra_tech.ra_vpn.common.ip.IpHeader;
import pro.ra_tech.ra_vpn.server.base.BaseServer;
import pro.ra_tech.ra_vpn.server.base.ServerContext;

@Slf4j
public class TcpServer extends BaseServer {

    public TcpServer(
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
        val boot = new ServerBootstrap();
        boot.group(masterGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new TcpServerChannelInitializer(serverCtx))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        boot.bind(getPort()).sync().channel();
    }

    @Override
    @Nullable
    protected Channel findChannel(IpHeader header) {
        try {
            val client = getClientManager().getClient(header.dstAddress().toInetAddress());
            if (client == null) {
                return null;
            }

            return client.channel();
        } catch (Exception _) {
            return null;
        }
    }

    @Override
    protected ServerType getType() {
        return ServerType.TCP;
    }
}
