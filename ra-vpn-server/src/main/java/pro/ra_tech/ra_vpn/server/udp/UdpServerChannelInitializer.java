package pro.ra_tech.ra_vpn.server.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.server.base.ServerContext;

@RequiredArgsConstructor
public class UdpServerChannelInitializer extends ChannelInitializer<DatagramChannel> {
    private final ServerContext ctx;

    @Override
    protected void initChannel(DatagramChannel ch) {
        ch.pipeline().addLast(new UdpInChannelHandler(ctx));
        ch.pipeline().addLast(new UdpOutChannelHandler(ctx.encryptorSupplier().get()));
//        ch.pipeline().addLast(new ServerEventChannelHandler(ctx.encryptorSupplier().get()));
    }
}
