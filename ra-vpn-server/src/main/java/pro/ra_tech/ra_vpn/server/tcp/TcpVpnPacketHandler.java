package pro.ra_tech.ra_vpn.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;
import pro.ra_tech.ra_vpn.server.base.ServerContext;
import pro.ra_tech.ra_vpn.server.base.VpnPacketHandler;

import java.net.InetSocketAddress;

@Slf4j
public class TcpVpnPacketHandler extends ChannelInboundHandlerAdapter {
    private final VpnPacketHandler handler;

    public TcpVpnPacketHandler(ServerContext ctx) {
        handler = new VpnPacketHandler(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            handler.handle(
                    (VpnPacket) msg,
                    (InetSocketAddress) ctx.channel().remoteAddress(),
                    ctx.channel(),
                    ctx::writeAndFlush,
                    (data, client) -> client.channel().pipeline().fireUserEventTriggered(data)
            );
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
