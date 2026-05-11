package pro.ra_tech.ra_vpn.server.tcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.common.converters.VpnPacketDecoder;
import pro.ra_tech.ra_vpn.common.converters.VpnPacketEncoder;
import pro.ra_tech.ra_vpn.server.base.ServerContext;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_PACKET_SIZE;
import static pro.ra_tech.ra_vpn.common.Constants.SIGNATURE;

@RequiredArgsConstructor
public class TcpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ServerContext ctx;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new VpnPacketEncoder(ctx.encryptorSupplier().get()),
                new DelimiterBasedFrameDecoder(MAX_PACKET_SIZE, true, Unpooled.wrappedBuffer(SIGNATURE)),
                new VpnPacketDecoder(ctx.encryptorSupplier().get()),
                new TcpVpnPacketHandler(ctx),
                new TcpServerEventHandler()
        );
    }
}
