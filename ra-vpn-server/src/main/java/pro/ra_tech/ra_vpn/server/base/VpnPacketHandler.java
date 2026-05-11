package pro.ra_tech.ra_vpn.server.base;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.proto.ConnectAckPacket;
import pro.ra_tech.ra_vpn.common.proto.ConnectPacket;
import pro.ra_tech.ra_vpn.common.proto.KeepAliveAckPacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectAckPayload;
import pro.ra_tech.ra_vpn.server.client.Client;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class VpnPacketHandler {
    private final ServerContext serverContext;

    private void onClientConnect(
            Consumer<VpnPacket> responseConsumer,
            ConnectPacket packet,
            InetSocketAddress sender,
            Channel channel
    ) {
        val id = packet.getPayload().clientId();
        val client = serverContext.clientManager().registerClient(id, sender, channel);

        log.info(
                "Client {}:{} connected with id {} virtual IP {}",
                sender.getHostString(),
                sender.getPort(),
                client.id(),
                client.virtualIp()
        );

        responseConsumer.accept(new ConnectAckPacket(
                new ConnectAckPayload(
                        serverContext.serverVirtualIp(),
                        client.virtualIp()
                )
        ));
    }

    private void onClientKeepAliveAck(KeepAliveAckPacket packet) {
        log.debug("Got keep alive ack from {}", packet.getPayload().srcAddress());
        val client = serverContext.clientManager().getClient(packet.getPayload().srcAddress());
        if (client == null) {
            log.warn("Keep alive ack from unknown client: {}", packet);
            return;
        }

        log.debug("Keep alive from client {}", client);
        client.lastSeen(Instant.now());
    }

    private void onDataTransfer(VpnPacket packet, BiConsumer<VpnPacket, Client> internalPacketConsumer) {
        try {
            val dstAddr = packet.getPayload().dstAddress();
            if (!serverContext.serverVirtualIp().equals(dstAddr) && serverContext.subnetInfo().isInRange(dstAddr.getHostAddress())) {
                val client = serverContext.clientManager().getClient(dstAddr);
                if (client != null) {
                    internalPacketConsumer.accept(packet, client);
                    return;
                }
            }

            serverContext.tun().write(Unpooled.wrappedBuffer(packet.getPayload().toBytes()).nioBuffer());
        } catch (Exception ex) {
            log.error("Error writing to tun device", ex);
        }
    }

    public void handle(
            VpnPacket packet,
            InetSocketAddress sender,
            Channel channel,
            Consumer<VpnPacket> responseHandler,
            BiConsumer<VpnPacket, Client> internalPacketHandler
    ) {
        val clientHost = sender.getHostString();
        val clientPort = sender.getPort();

        switch (packet.getType()) {
            case CONNECT:
                onClientConnect(responseHandler, (ConnectPacket) packet, sender, channel);
                return;
            case KEEP_ALIVE_ACK:
                onClientKeepAliveAck((KeepAliveAckPacket) packet);
                return;
            case DISCONNECT:
                log.info("Client {}:{} disconnected", clientHost, clientPort);
                return;
            case DATA_TRANSFER:
                onDataTransfer(packet, internalPacketHandler);
                return;
            default:
                log.warn("Unexpected packet from {}:{}", clientHost, clientPort);
        }
    }
}
