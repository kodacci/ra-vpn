package pro.ra_tech.ra_vpn.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import pro.ra_tech.ra_vpn.server.event.ServerEvent;

public class TcpServerEventHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (!(event instanceof ServerEvent serverEvent)) {
            return;
        }

        serverEvent.client().channel().writeAndFlush(serverEvent.packet());
    }
}
