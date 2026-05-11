package pro.ra_tech.ra_vpn.server.base;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.proto.DataTransferPacket;
import pro.ra_tech.ra_vpn.common.tun.BaseTunReader;
import pro.ra_tech.ra_vpn.common.tun.ChannelSupplier;
import pro.ra_tech.ra_vpn.common.tun.VpnTunDevice;
import pro.ra_tech.ra_vpn.server.client.ClientManager;
import pro.ra_tech.ra_vpn.server.event.SendDataTransferEvent;

@Slf4j
public class ServerTunReader extends BaseTunReader {
    private final ClientManager clientManager;

    public ServerTunReader(
            VpnTunDevice tunDevice,
            ChannelSupplier channelSupplier,
            ClientManager clientManager
    ) {
        super(tunDevice, channelSupplier);
        this.clientManager = clientManager;
    }

    @Override
    protected void sendPacket(Channel channel, DataTransferPacket packet) {
        val client = clientManager.getClient(packet.getPayload().dstAddress());
        if (client == null) {
            log.warn("No client with virtual ip {}", packet.getPayload().dstAddress());

            return;
        }

        channel.pipeline().fireUserEventTriggered(
                new SendDataTransferEvent(
                        packet,
                        client
                )
        );
    }
}
