package pro.ra_tech.ra_vpn.server.event;

import pro.ra_tech.ra_vpn.common.proto.VpnPacket;
import pro.ra_tech.ra_vpn.server.client.Client;

public interface ServerEvent {
    ServerEventType type();
    VpnPacket packet();
    Client client();
}
