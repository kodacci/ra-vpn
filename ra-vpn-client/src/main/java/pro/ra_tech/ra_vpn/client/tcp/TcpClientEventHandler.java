package pro.ra_tech.ra_vpn.client.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.client.event.ConnectionState;
import pro.ra_tech.ra_vpn.client.event.Connector;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

import static pro.ra_tech.ra_vpn.common.proto.VpnPacketType.CONNECT;

@Slf4j
@RequiredArgsConstructor
public class TcpClientEventHandler extends ChannelInboundHandlerAdapter {
    private final Connector connector;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        val packet = (VpnPacket) event;

        if (connector.getState() != ConnectionState.CONNECTED && packet.getType() != CONNECT) {
            log.info("Not connected, skipping packet");

            return;
        }

        ctx.writeAndFlush(packet);
    }
}
