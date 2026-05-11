package pro.ra_tech.ra_vpn.server.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.server.base.ServerContext;
import pro.ra_tech.ra_vpn.server.base.VpnPacketHandler;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_PACKET_SIZE;

@Slf4j
@RequiredArgsConstructor
public class UdpInChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final PacketEncryptor encryptor;
    private final VpnPacketHandler handler;

    public UdpInChannelHandler(ServerContext ctx) {
        encryptor = ctx.encryptorSupplier().get();
        handler = new VpnPacketHandler(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        val size = msg.content().readableBytes();

        if (size > MAX_PACKET_SIZE) {
            log.warn(
                    "Received UDP packet from {} with too large size: {}",
                    msg.sender(),
                    size
            );

            return;
        }

        try {
            val packet = encryptor.decrypt(msg);
            handler.handle(
                    packet,
                    msg.sender(),
                    ctx.channel(),
                    res -> ctx.writeAndFlush(new DatagramPacket(encryptor.encrypt(res), msg.sender())),
                    (data, client) -> ctx.writeAndFlush(new DatagramPacket(encryptor.encrypt(data), client.realAddress()))
            );
        } catch (Exception ex) {
            log.error("Error handling packet from client:", ex);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception caught while handling inbound packets: ", cause);
        ctx.flush();
    }
}
