package pro.ra_tech.ra_vpn.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.ra_vpn.common.proto.DataTransferPacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;
import pro.ra_tech.ra_vpn.server.base.ServerContext;
import pro.ra_tech.ra_vpn.server.base.VpnPacketHandler;
import pro.ra_tech.ra_vpn.server.client.Client;
import pro.ra_tech.ra_vpn.server.event.SendDataTransferEvent;

import java.net.InetSocketAddress;

@Slf4j
public class TcpVpnPacketHandler extends ChannelInboundHandlerAdapter {
    private final VpnPacketHandler handler;

    public TcpVpnPacketHandler(ServerContext ctx) {
        handler = new VpnPacketHandler(ctx);
    }

    private void handleInternalTraffic(VpnPacket data, Client client) {
        if (!(data instanceof DataTransferPacket dataPacket)) {
            return;
        }

        client.channel().pipeline().fireUserEventTriggered(
                new SendDataTransferEvent(dataPacket, client)
        );
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            handler.handle(
                    (VpnPacket) msg,
                    (InetSocketAddress) ctx.channel().remoteAddress(),
                    ctx.channel(),
                    ctx::writeAndFlush,
                    this::handleInternalTraffic
            );
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
