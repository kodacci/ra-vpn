package pro.ra_tech.ra_vpn.client.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.client.base.ClientContext;
import pro.ra_tech.ra_vpn.client.base.VpnPacketHandler;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;

import java.net.InetSocketAddress;

@Slf4j
public class UdpInChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final PacketEncryptor encryptor;
    private final VpnPacketHandler handler;
    private final InetSocketAddress server;

    public UdpInChannelHandler(ClientContext ctx) {
        encryptor = ctx.encryptorSupplier().get();
        handler = new VpnPacketHandler(ctx);
        server = ctx.server();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        log.info("Received packet from {}", msg.sender());

        try {
            val packet = encryptor.decrypt(msg);
            handler.handle(
                    packet,
                    msg.sender(),
                    res -> ctx.writeAndFlush(new DatagramPacket(encryptor.encrypt(res), server))
            );
        } catch (Exception ex) {
            log.error("Error handling packet from {}:", msg.sender(), ex);
        }
    }
}
