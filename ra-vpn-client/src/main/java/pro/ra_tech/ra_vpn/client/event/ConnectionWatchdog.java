package pro.ra_tech.ra_vpn.client.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.time.Instant;

/**
 * Detects a lost connection to the server. The server sends keep-alive packets on a fixed
 * interval; if none arrive within {@link #keepAliveTimeout} while the client believes it is
 * connected, the connection is considered dead and the state is flipped to
 * {@link ConnectionState#DISCONNECTED}, which makes the scheduled {@link Connector} re-send a
 * CONNECT packet and thus reconnect.
 */
@Slf4j
@RequiredArgsConstructor
public class ConnectionWatchdog implements Runnable {
    private final Connector connector;
    private final Duration keepAliveTimeout;

    @Override
    public void run() {
        if (connector.getState() != ConnectionState.CONNECTED) {
            return;
        }

        val now = Instant.now();
        if (connector.getLastAlive().plus(keepAliveTimeout).isBefore(now)) {
            log.warn(
                    "No keep alive from server for more than {}, marking connection as lost and reconnecting",
                    keepAliveTimeout
            );
            connector.setState(ConnectionState.DISCONNECTED);
        }
    }
}
