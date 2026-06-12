package pro.ra_tech.ra_vpn.client.base;

import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.client.event.ConnectionState;
import pro.ra_tech.ra_vpn.common.proto.ConnectAckPacket;
import pro.ra_tech.ra_vpn.common.proto.KeepAliveAckPacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class VpnPacketHandler {
    private final ClientContext clientContext;

    private void onConnectAck(ConnectAckPacket packet) {
        log.info("Got CONNECT_ACK from server: {}", packet.getPayload());

        try {
            clientContext.tun().setVirtualIp(packet.getPayload().dstAddress(), packet.getPayload().srcAddress());
            clientContext.connector().markAlive();
            clientContext.connector().setState(ConnectionState.CONNECTED);

            log.info("Successfully connected to server with virtual IP set to {}", packet.getPayload().dstAddress());
        } catch (Exception ex) {
            log.error("Error setting virtual IP given by server", ex);
        }
    }

    private void onKeepAlive(VpnPacket packet, Consumer<VpnPacket> consumer) {
        log.debug("Got KEEP_ALIVE from server: {}", packet.getPayload());
        clientContext.connector().markAlive();
        val virtualIp = clientContext.tun().getVirtualIp();
        if (virtualIp == null) {
            log.warn("Keep alive when tun not configured, skipping");

            return;
        }

        consumer.accept(new KeepAliveAckPacket(
                clientContext.clientId(),
                virtualIp,
                clientContext.tun().getGatewayIp()
        ));
    }

    private void onDataTransfer(VpnPacket packet) {
        try {
            clientContext.tun().write(Unpooled.wrappedBuffer(packet.getPayload().toBytes()).nioBuffer());
        } catch (Exception ex) {
            log.error("Error writing to tun device", ex);
        }
    }

    public void handle(
            VpnPacket packet,
            InetSocketAddress sender,
            Consumer<VpnPacket> responseHandler
    ) {
        log.debug("Received packet {} from {}", packet.getType(), sender);

        try {
            switch (packet.getType()) {
                case CONNECT_ACK:
                    onConnectAck((ConnectAckPacket) packet);
                    return;

                case KEEP_ALIVE:
                    onKeepAlive(packet, responseHandler);
                    return;

                case DISCONNECT:
                    log.warn("Got DISCONNECT from server");
                    clientContext.connector().setState(ConnectionState.DISCONNECTED);
                    return;

                case DATA_TRANSFER:
                    onDataTransfer(packet);
                    return;

                default:
                    log.warn("Unexpected packet type from server");
            }
        } catch (Exception ex) {
            log.error("Error handling packet from {}:", sender, ex);
        }
    }
}
