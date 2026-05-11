package pro.ra_tech.ra_vpn.common.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

import static pro.ra_tech.ra_vpn.common.Constants.SIGNATURE;

public class XorPacketEncryptor extends BaseEncryptor {
    private static final byte XOR_KEY = (byte) 0xff;

    @Override
    public ByteBuf encrypt(VpnPacket packet) {
        val dst = getBuffer();
        System.arraycopy(SIGNATURE, 0, dst, 0, SIGNATURE.length);

        val src = packet.getPayload().toBytes();
        dst[SIGNATURE.length] = (byte) (packet.getType().getCode() ^ XOR_KEY);

        for (int i = SIGNATURE.length + 1, j = 0; j < src.length; ++i, ++j) {
            dst[i] = (byte) (src[j] ^ XOR_KEY);
        }

        return Unpooled.wrappedBuffer(dst, 0, SIGNATURE.length + src.length + 1);
    }

    @Override
    public VpnPacket decrypt(byte[] data, int size) {
        for (int i = SIGNATURE.length; i < size; ++i) {
            data[i] = (byte) (data[i] ^ XOR_KEY);
        }

        return parseRawPacket(data, size);
    }

    @Override
    public VpnPacket decryptHeadless(byte[] data, int size) {
        for (int i = 0; i < size; ++i) {
            data[i] = (byte) (data[i] ^ XOR_KEY);
        }

        return parseRawHeadless(data, size);
    }
}
