package pro.ra_tech.ra_vpn.server.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.proto.KeepAlivePacket;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectPayload;
import pro.ra_tech.ra_vpn.server.base.ServerContext;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class ClientChecker implements Runnable {
    private static final Duration FORGET_TIMEOUT = Duration.ofSeconds(60);

    private final ServerContext ctx;

    @Override
    public void run() {
        val clients = ctx.clientManager().getClients();
        log.debug("Checking {} clients", clients.size());

        if (clients.isEmpty()) {
            return;
        }

        val now = Instant.now();
        clients.forEach(client -> {
            val lastSeen = client.lastSeen();
            if (lastSeen.plus(FORGET_TIMEOUT).isBefore(now)) {
                log.info("Forgetting client {}", client);
                ctx.clientManager().forget(client);

                return;
            }

            log.info("Sending keep alive packet to {}", client);
            client.channel().pipeline().fireUserEventTriggered(
                    new SendKeepAliveEvent(
                            new KeepAlivePacket(
                                new ConnectPayload(
                                        client.id(),
                                        ctx.serverVirtualIp(),
                                        client.virtualIp()
                                )
                            ),
                            client
                    )
            );
        });
    }
}
