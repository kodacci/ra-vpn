package pro.ra_tech.ra_vpn.server.base;

import io.netty.channel.Channel;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.AutoScalingEventExecutorChooserFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.net.util.SubnetUtils;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorFactory;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.common.ip.IpHeader;
import pro.ra_tech.ra_vpn.common.network.NetworkConfigurer;
import pro.ra_tech.ra_vpn.common.tun.TunReaderHandler;
import pro.ra_tech.ra_vpn.common.tun.UnixVpnTunDevice;
import pro.ra_tech.ra_vpn.server.Server;
import pro.ra_tech.ra_vpn.server.client.ClientManager;
import pro.ra_tech.ra_vpn.server.client.impl.HashMapClientManager;
import pro.ra_tech.ra_vpn.server.event.ClientChecker;
import pro.ra_tech.ra_vpn.server.exceptions.ServerStartException;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class BaseServer implements Server {
    private static final int MAX_THREADS = 4;
    private static final int CLIENT_CHECK_INTERVAL_SEC = 10;

    private final String host;
    private final int port;
    private final int tunNumber;
    private final String cidr;
    private final EncryptorType encryptorType;
    @Nullable
    private final String encryptorKeyFilePath;

    @Getter
    private ClientManager clientManager;

    @RequiredArgsConstructor
    protected enum ServerType {
        TCP("TCP"),
        UDP("UDP");

        private final String type;

        @Override
        public String toString() {
            return type;
        }
    }

    protected PacketEncryptor buildEncryptor() {
        return EncryptorFactory.ofType(encryptorType, encryptorKeyFilePath);
    }

    protected abstract void bootstrap(ServerContext serverCtx, IoEventLoopGroup masterGroup) throws InterruptedException;
    @Nullable
    protected abstract Channel findChannel(IpHeader header);
    protected abstract ServerType getType();

    @Override
    public void start() {
        val factory = new AutoScalingEventExecutorChooserFactory(
                1,
                MAX_THREADS,
                500,
                TimeUnit.MILLISECONDS,
                0.25,
                0.8,
                1,
                1,
                2
        );

        val masterGroup = new MultiThreadIoEventLoopGroup(MAX_THREADS, null, factory, NioIoHandler.newFactory());

        try (
                val checkerExecutor = Executors.newSingleThreadScheduledExecutor();
                val tun = new UnixVpnTunDevice(tunNumber)
        ) {
            val subnet = new SubnetUtils(cidr);
            val serverVirtualIp = InetAddress.ofLiteral(subnet.getInfo().getLowAddress());

            tun.setVirtualIp(serverVirtualIp, serverVirtualIp);
            clientManager = new HashMapClientManager(subnet);
            val serverCtx = new ServerContext(
                    tun,
                    clientManager,
                    this::buildEncryptor,
                    serverVirtualIp,
                    subnet.getInfo()
            );

            log.info("Setting server virtual ip to {}", serverVirtualIp);

            NetworkConfigurer.configureIface(cidr, tun.getName());

            bootstrap(serverCtx, masterGroup);

            checkerExecutor.scheduleAtFixedRate(
                    new ClientChecker(serverCtx),
                    CLIENT_CHECK_INTERVAL_SEC,
                    CLIENT_CHECK_INTERVAL_SEC,
                    TimeUnit.SECONDS
            );

            log.info("{} server started on {}:{}", getType(), host, port);

            val tunReader = new ServerTunReader(tun, this::findChannel, clientManager);
            TunReaderHandler.start(tunReader).join();
        } catch (InterruptedException _) {
            log.warn("UDP server interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            throw new ServerStartException("Error configuring UDP VPN server:", ex);
        } finally {
            masterGroup.shutdownGracefully();
        }
    }
}
