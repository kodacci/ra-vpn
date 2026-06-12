package pro.ra_tech.ra_vpn.common.tun;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.ip.IpHeaderParser;
import pro.ra_tech.ra_vpn.common.proto.DataTransferPacket;

import java.io.IOException;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_VPN_PACKET_SIZE;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseTunReader implements TunReader {
    private final VpnTunDevice tun;
    private final ChannelSupplier channelSupplier;

    private final IpHeaderParser parser = new IpHeaderParser();

    @Override
    public void read() throws InterruptedException {
        try {
            if (!tun.isInitialized()) {
                Thread.sleep(1000);
                return;
            }

            val rawPacket = tun.read();
            if (rawPacket.length == 0) {
                return;
            }
            if (rawPacket.length > MAX_VPN_PACKET_SIZE) {
                log.warn("Too big packet size from internet: {}, dropping", rawPacket.length);
                return;
            }

            val header = parser.parse(rawPacket);
            val channel = channelSupplier.get(header);
            if (channel == null) {
                return;
            }

            log.debug("Sending packet with header {}", header);

            sendPacket(channel, new DataTransferPacket(
                    rawPacket,
                    header.srcAddress().toInetAddress(),
                    header.dstAddress().toInetAddress()
            ));
        } catch (IOException ex) {
            log.error("Error reading from tun device", ex);
            Thread.sleep(1000);
        }
    }

    @Override
    public String getTunName() {
        return tun.getName();
    }

    protected abstract void sendPacket(Channel channel, DataTransferPacket packet);
}
