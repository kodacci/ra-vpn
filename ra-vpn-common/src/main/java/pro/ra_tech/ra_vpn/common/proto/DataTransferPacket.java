package pro.ra_tech.ra_vpn.common.proto;

import lombok.RequiredArgsConstructor;
import pro.ra_tech.ra_vpn.common.proto.payload.DataTransferPayload;

import java.net.InetAddress;

@RequiredArgsConstructor
public class DataTransferPacket implements VpnPacket {
    private final DataTransferPayload payload;

    public static DataTransferPacket fromBytes(byte[] bytes, int offset, int length) {
        return new DataTransferPacket(DataTransferPayload.fromBytes(bytes, offset, length));
    }

    public DataTransferPacket(byte[] data, InetAddress src, InetAddress dst) {
        payload = new DataTransferPayload(data, src, dst);
    }

    @Override
    public VpnPacketType getType() {
        return VpnPacketType.DATA_TRANSFER;
    }

    @Override
    public DataTransferPayload getPayload() {
        return payload;
    }
}
