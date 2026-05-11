package pro.ra_tech.ra_vpn.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.val;
import pro.ra_tech.ra_vpn.server.event.ServerEvent;

public class TcpServerEventHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        val serverEvent = (ServerEvent) event;

        serverEvent.client().channel().writeAndFlush(serverEvent.packet());
    }
}
