package pro.ra_tech.ra_vpn.client.base;

import io.netty.channel.Channel;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.client.event.Connector;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorFactory;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.common.tun.TunReaderHandler;
import pro.ra_tech.ra_vpn.common.tun.UnixVpnTunDevice;
import pro.ra_tech.ra_vpn.common.tun.VpnTunDevice;
import pro.ra_tech.ra_vpn.common.tun.WinVpnTunDevice;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static lombok.AccessLevel.PROTECTED;

@Slf4j
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public abstract class BaseClient implements Client {
    protected static final int CONNECTION_INTERVAL_SEC = 3;

    private final InetSocketAddress server;
    private final int tunNumber;
    private final String virtualIp;
    private final String clientId;
    private final EncryptorType encryptorType;
    @Nullable
    private final String encryptorKeyFilePath;

    @RequiredArgsConstructor
    protected enum ClientType {
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

    protected VpnTunDevice buildTunDevice(int number) {
        val os = System.getProperty("os.name");
        log.info("OS name: {}", os);

        if (os.contains("Windows")) {
            log.info("Building windows TUN device");

            return new WinVpnTunDevice("RA-VPN");
        }

        log.info("Building Unix tun device utun{}", number);

        return new UnixVpnTunDevice(number);
    }

    protected abstract void bootstrap(IoEventLoopGroup masterGroup, ClientContext ctx) throws InterruptedException;
    protected abstract ClientType getType();
    protected abstract Supplier<Channel> getChannelSupplier();
    protected abstract Reconnector getReconnector();

    @Override
    public void start() {
        val masterGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try (
                val tun = buildTunDevice(getTunNumber());
                val connectExecutor = Executors.newSingleThreadScheduledExecutor()
        ) {
            val connector = new Connector(getClientId(), InetAddress.ofLiteral(getVirtualIp()), getServer().getAddress());


            val ctx = new ClientContext(
                    getServer(), tun, this::buildEncryptor, connector, getClientId(), getReconnector()
            );
            bootstrap(masterGroup, ctx);
            connector.setChannelSupplier(getChannelSupplier());
            connectExecutor.scheduleAtFixedRate(connector, 0, CONNECTION_INTERVAL_SEC, java.util.concurrent.TimeUnit.SECONDS);

            log.info("{} client started on {}", getType(), getChannelSupplier().get().localAddress());

            val tunReader = new ClientTunReader(tun, _ -> getChannelSupplier().get());
            TunReaderHandler.start(tunReader).join();
        } catch (InterruptedException _) {
            log.warn("{} client interrupted", getType());
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("Error opening tun device: ", ex);
        } finally {
            masterGroup.shutdownGracefully();
        }
    }
}
