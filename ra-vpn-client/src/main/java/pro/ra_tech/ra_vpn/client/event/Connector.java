package pro.ra_tech.ra_vpn.client.event;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.proto.ConnectPacket;

import java.net.InetAddress;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class Connector implements Runnable {
    private final String clientId;
    private final InetAddress virtualIp;
    private final InetAddress serverHost;

    @Setter
    @Nullable
    private Supplier<Channel> channelSupplier;
    @Getter
    @Setter
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;

    @Override
    public void run() {
        if (state == ConnectionState.DISCONNECTED && channelSupplier != null) {
            log.info("Sending CONNECT packet to server");
            channelSupplier.get().pipeline().fireUserEventTriggered(
                    new ConnectPacket(
                            clientId,
                            virtualIp,
                            serverHost
                    )
            );
        }
    }
}
