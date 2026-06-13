package pro.ra_tech.ra_vpn.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorFactory;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;

import java.net.InetSocketAddress;

/**
 * UDP mediator that sits between VPN clients and the real VPN server. A single datagram socket
 * receives client traffic and forwards it to the server, and relays the server's responses back to
 * the originating client (see {@link ProxyChannelHandler}).
 */
@Slf4j
@RequiredArgsConstructor
public class ProxyServer {
    private final String host;
    private final int port;
    private final InetSocketAddress serverAddress;
    private final EncryptorType encryptorType;
    @Nullable
    private final String encryptorKeyFilePath;

    public void start() {
        val group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        val clients = new ClientRegistry();

        try {
            val boot = new Bootstrap();
            boot.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) {
                            ch.pipeline().addLast(new ProxyChannelHandler(
                                    EncryptorFactory.ofType(encryptorType, encryptorKeyFilePath),
                                    serverAddress,
                                    clients
                            ));
                        }
                    });

            val channel = boot.bind(host, port).sync().channel();
            log.info(
                    "UDP proxy started on {}:{}, forwarding to server {}",
                    host,
                    port,
                    serverAddress
            );

            channel.closeFuture().sync();
        } catch (InterruptedException _) {
            log.warn("UDP proxy interrupted");
            Thread.currentThread().interrupt();
        } finally {
            group.shutdownGracefully();
        }
    }
}
