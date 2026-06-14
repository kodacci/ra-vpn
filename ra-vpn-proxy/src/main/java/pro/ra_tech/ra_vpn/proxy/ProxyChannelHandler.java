package pro.ra_tech.ra_vpn.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacketType;

import java.net.InetSocketAddress;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_PACKET_SIZE;

/**
 * Relays VPN datagrams between clients and the real VPN server over a single UDP socket.
 * <p>
 * Datagrams whose sender is the configured server are routed back to the client identified by the
 * inner IP destination address; everything else is treated as client traffic, the sender is
 * recorded against the inner IP source address, and the packet is forwarded to the server.
 * <p>
 * The proxy only inspects the inner addresses to make routing decisions: the original (encrypted)
 * bytes are forwarded verbatim so the client/server keep doing end-to-end crypto. Decryption is
 * performed against a copy because some encryptors (e.g. XOR) decrypt in place.
 */
@Slf4j
@RequiredArgsConstructor
public class ProxyChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final PacketEncryptor encryptor;
    private final InetSocketAddress serverAddress;
    private final ClientRegistry clients;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        val sender = msg.sender();
        val content = msg.content();
        val size = content.readableBytes();

        if (size > MAX_PACKET_SIZE) {
            log.warn("Received UDP packet from {} with too large size: {}", sender, size);
            return;
        }

        val raw = new byte[size];
        content.getBytes(content.readerIndex(), raw);

        final VpnPacket packet;
        try {
            // Decrypt a copy: forward the untouched original and avoid in-place mutation side effects.
            packet = encryptor.decrypt(raw.clone(), size);
        } catch (Exception ex) {
            log.warn("Failed to decode packet from {}, dropping", sender, ex);
            return;
        }

        if (sender.equals(serverAddress)) {
            routeToClient(ctx, packet, raw);
        } else {
            routeToServer(ctx, packet, sender, raw);
        }
    }

    private void routeToServer(ChannelHandlerContext ctx, VpnPacket packet, InetSocketAddress sender, byte[] raw) {
        val virtualIp = packet.getPayload().srcAddress();

        if (packet.getType() == VpnPacketType.DISCONNECT) {
            log.debug("Forwarding DISCONNECT from client {} to server", virtualIp.getHostAddress());
            forward(ctx, raw, serverAddress);
            clients.forget(virtualIp);
            return;
        }

        if (clients.track(virtualIp, sender)) {
            log.info("Tracking client {} at {}", virtualIp.getHostAddress(), sender);
        }

        log.debug("Forwarding {} from client {} to server", packet.getType(), virtualIp.getHostAddress());
        forward(ctx, raw, serverAddress);
    }

    private void routeToClient(ChannelHandlerContext ctx, VpnPacket packet, byte[] raw) {
        val virtualIp = packet.getPayload().dstAddress();
        val client = clients.findRealAddress(virtualIp);
        if (client == null) {
            log.warn("Dropping {} from server for unknown client {}", packet.getType(), virtualIp.getHostAddress());
            return;
        }

        log.debug("Forwarding {} from server to client {} at {}", packet.getType(), virtualIp.getHostAddress(), client);
        forward(ctx, raw, client);
    }

    private void forward(ChannelHandlerContext ctx, byte[] raw, InetSocketAddress target) {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(raw), target));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception caught while proxying packets: ", cause);
        ctx.flush();
    }
}
