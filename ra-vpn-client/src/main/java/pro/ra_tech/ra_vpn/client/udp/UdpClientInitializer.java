package pro.ra_tech.ra_vpn.client.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.client.base.ClientContext;

@RequiredArgsConstructor
public class UdpClientInitializer extends ChannelInitializer<DatagramChannel> {
    private final ClientContext context;

    @Override
    protected void initChannel(DatagramChannel ch) {
        ch.pipeline().addLast(new UdpOutChannelHandler(
                context.server(), context.encryptorSupplier().get(), context.connector()
        ));
        ch.pipeline().addLast(new UdpInChannelHandler(context));
    }
}
