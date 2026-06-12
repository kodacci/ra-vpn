package pro.ra_tech.ra_vpn.common.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

import static pro.ra_tech.ra_vpn.common.Constants.SIGNATURE;

public class DummyPacketEncryptor extends BaseEncryptor {
    @Override
    public ByteBuf encrypt(VpnPacket packet) {
        val dst = getBuffer();

        System.arraycopy(SIGNATURE, 0, dst, 0, SIGNATURE.length);
        dst[SIGNATURE.length] = packet.getType().getCode();

        val bytes = packet.getPayload().toBytes();
        System.arraycopy(bytes, 0, dst, SIGNATURE.length + 1, bytes.length);

        return Unpooled.wrappedBuffer(dst, 0, SIGNATURE.length + bytes.length + 1);
    }

    @Override
    public VpnPacket decrypt(byte[] data, int size) {
        return parseRawPacket(data);
    }

    @Override
    public VpnPacket decryptHeadless(byte[] data, int size) {
        return parseRawHeadless(data, size);
    }
}
