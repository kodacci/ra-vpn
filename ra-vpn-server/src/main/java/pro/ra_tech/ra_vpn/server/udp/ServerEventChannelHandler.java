package pro.ra_tech.ra_vpn.server.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.server.event.ServerEvent;

@Slf4j
@RequiredArgsConstructor
public class ServerEventChannelHandler extends SimpleChannelInboundHandler<ServerEvent> {
    private final PacketEncryptor encryptor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerEvent msg) {
        log.debug("Handling server event {} for {}", msg, msg.client());

        ctx.writeAndFlush(new DatagramPacket(
            encryptor.encrypt(msg.packet()),
            msg.client().realAddress()
        ));
    }
}
