package pro.ra_tech.ra_vpn.server.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.crypto.PacketEncryptor;
import pro.ra_tech.ra_vpn.server.event.ServerEvent;

@Slf4j
@RequiredArgsConstructor
public class UdpOutChannelHandler extends ChannelInboundHandlerAdapter {
    private final PacketEncryptor encryptor;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        val serverEvent = (ServerEvent) event;

        ctx.writeAndFlush(new DatagramPacket(
                encryptor.encrypt(serverEvent.packet()),
                serverEvent.client().realAddress()
        ));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception caught while handling outbound vpn packets: ", cause);
        ctx.flush();
    }
}
