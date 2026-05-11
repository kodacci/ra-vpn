package pro.ra_tech.ra_vpn.client.tcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.client.base.ClientContext;
import pro.ra_tech.ra_vpn.common.converters.VpnPacketDecoder;
import pro.ra_tech.ra_vpn.common.converters.VpnPacketEncoder;

import static pro.ra_tech.ra_vpn.common.Constants.SIGNATURE;

@RequiredArgsConstructor
public class TcpClientInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientContext ctx;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new VpnPacketEncoder(ctx.encryptorSupplier().get()),
                new TcpClientEventHandler(ctx.connector()),
                new DelimiterBasedFrameDecoder(1500, true, Unpooled.wrappedBuffer(SIGNATURE)),
                new VpnPacketDecoder(ctx.encryptorSupplier().get()),
                new TcpVpnPacketHandler(ctx)
        );
    }
}
