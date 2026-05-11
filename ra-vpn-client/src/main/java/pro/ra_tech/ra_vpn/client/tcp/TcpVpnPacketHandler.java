package pro.ra_tech.ra_vpn.client.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.ra_vpn.client.base.ClientContext;
import pro.ra_tech.ra_vpn.client.base.VpnPacketHandler;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

import java.net.InetSocketAddress;

@Slf4j
public class TcpVpnPacketHandler extends ChannelInboundHandlerAdapter {
    private final VpnPacketHandler handler;
    private final ClientContext clientContext;

    public TcpVpnPacketHandler(ClientContext ctx) {
        handler = new VpnPacketHandler(ctx);
        clientContext = ctx;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Connected to server");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        handler.handle(
                (VpnPacket) msg,
                (InetSocketAddress) ctx.channel().remoteAddress(),
                ctx::writeAndFlush
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Server disconnected, reconnecting...");
        clientContext.reconnector().reconnect();
        super.channelInactive(ctx);
    }
}
