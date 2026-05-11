package pro.ra_tech.ra_vpn.server.event;

import pro.ra_tech.ra_vpn.common.proto.KeepAlivePacket;
import pro.ra_tech.ra_vpn.server.client.Client;

import static pro.ra_tech.ra_vpn.server.event.ServerEventType.SEND_KEEP_ALIVE;

public record SendKeepAliveEvent(
    KeepAlivePacket packet,
    Client client
) implements ServerEvent {
    @Override
    public ServerEventType type() {
        return SEND_KEEP_ALIVE;
    }
}
