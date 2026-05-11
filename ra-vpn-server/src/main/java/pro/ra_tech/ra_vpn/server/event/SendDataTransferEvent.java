package pro.ra_tech.ra_vpn.server.event;

import pro.ra_tech.ra_vpn.common.proto.DataTransferPacket;
import pro.ra_tech.ra_vpn.server.client.Client;

import static pro.ra_tech.ra_vpn.server.event.ServerEventType.SEND_DATA_TRANSFER;

public record SendDataTransferEvent(
        DataTransferPacket packet,
        Client client
) implements ServerEvent {
    @Override
    public ServerEventType type() {
        return SEND_DATA_TRANSFER;
    }
}
