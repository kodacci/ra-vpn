package pro.ra_tech.ra_vpn.common.crypto;

import io.netty.channel.socket.DatagramPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.proto.ConnectAckPacket;
import pro.ra_tech.ra_vpn.common.proto.ConnectPacket;
import pro.ra_tech.ra_vpn.common.proto.DataTransferPacket;
import pro.ra_tech.ra_vpn.common.proto.DisconnectPacket;
import pro.ra_tech.ra_vpn.common.proto.KeepAliveAckPacket;
import pro.ra_tech.ra_vpn.common.proto.KeepAlivePacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;
import pro.ra_tech.ra_vpn.common.proto.VpnPacketType;

import java.util.Arrays;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_PACKET_SIZE;
import static pro.ra_tech.ra_vpn.common.Constants.SIGNATURE;

@Slf4j
public abstract class BaseEncryptor implements PacketEncryptor {
    protected static final int TYPE_OFFSET = SIGNATURE.length;

    @Getter(AccessLevel.PROTECTED)
    private final byte[] buffer = new byte[MAX_PACKET_SIZE];

    protected  VpnPacket parseRawPacket(byte[] raw) {
        return parseRawPacket(raw, raw.length);
    }

    private VpnPacket parsePayload(VpnPacketType type, byte[] raw, int payloadOffset, int length) {
        return switch (type) {
            case CONNECT -> ConnectPacket.fromBytes(raw, payloadOffset);
            case CONNECT_ACK -> ConnectAckPacket.fromBytes(raw, payloadOffset);
            case KEEP_ALIVE -> KeepAlivePacket.fromBytes(raw, payloadOffset);
            case KEEP_ALIVE_ACK -> KeepAliveAckPacket.fromBytes(raw, payloadOffset);
            case DATA_TRANSFER -> DataTransferPacket.fromBytes(raw, payloadOffset, length);
            case DISCONNECT -> DisconnectPacket.fromBytes(raw, payloadOffset);
            case UNKNOWN -> throw new IllegalArgumentException("Unsupported vpn packet type " + raw[TYPE_OFFSET]);
        };
    }

    protected VpnPacket parseRawPacket(byte[] raw, int size) {
        if (!Arrays.equals(raw, 0, SIGNATURE.length, SIGNATURE, 0, SIGNATURE.length)) {
            log.warn("Packet without signature, skipping ...");

            throw new IllegalArgumentException("Bad vpn signature");
        }

        val type = VpnPacketType.of(raw[TYPE_OFFSET]);
        val payloadOffset = TYPE_OFFSET + 1;
        val length = size - payloadOffset;

        return parsePayload(type, raw, payloadOffset, length);
    }

    protected VpnPacket parseRawHeadless(byte[] raw, int size) {
        val type = VpnPacketType.of(raw[0]);

        return parsePayload(type, raw, 1, size - 1);
    }

    @Override
    public VpnPacket decrypt(DatagramPacket packet) {
        val size = packet.content().readableBytes();

        packet.content().readBytes(getBuffer(), 0, size);
        return decrypt(getBuffer(), size);
    }
}
