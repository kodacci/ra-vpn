package pro.ra_tech.ra_vpn.common.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.ra_vpn.common.exceptions.CipherConfigException;
import pro.ra_tech.ra_vpn.common.exceptions.PacketDecryptException;
import pro.ra_tech.ra_vpn.common.exceptions.PacketEncryptException;
import pro.ra_tech.ra_vpn.common.proto.VpnPacket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

import static pro.ra_tech.ra_vpn.common.Constants.AES_BITS;
import static pro.ra_tech.ra_vpn.common.Constants.IV_SIZE;
import static pro.ra_tech.ra_vpn.common.Constants.SIGNATURE;

@Slf4j
public class AesPacketEncryptor extends BaseEncryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    public static final String KEY_ALGORITHM = "AES";

    private static final int IV_OFFSET = SIGNATURE.length + 1;

    private final SecureRandom random = new SecureRandom();
    private final byte[] iv = new byte[IV_SIZE];

    private final SecretKey key;
    private final Cipher cipher;

    public AesPacketEncryptor(byte[] base64Key) {
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), KEY_ALGORITHM);
        } catch (Exception ex) {
            throw new CipherConfigException("Error configuring AES encryptor cipher:", ex);
        }
    }

    @Override
    public ByteBuf encrypt(VpnPacket packet) {
        try {
            val buffer = getBuffer();
            // Signature
            System.arraycopy(SIGNATURE, 0, buffer, 0, SIGNATURE.length);
            // Packet type
            buffer[SIGNATURE.length] = packet.getType().getCode();
            // IV
            random.nextBytes(iv);
            System.arraycopy(iv, 0, buffer, IV_OFFSET, iv.length);

            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(AES_BITS, iv));

            val src = packet.getPayload().toBytes();
            val size = cipher.doFinal(src, 0, src.length, buffer, IV_OFFSET + iv.length);

            return Unpooled.wrappedBuffer(buffer, 0, SIGNATURE.length + 1 + size + iv.length);
        } catch(Exception ex) {
            throw new PacketEncryptException("Error encrypting VPN packet", ex);
        }
    }

    private VpnPacket decrypt(byte[] data, int size, int ivOffset, boolean headless) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(AES_BITS, data, ivOffset, iv.length));
            System.arraycopy(data, 0, getBuffer(), 0, ivOffset);
            val decodedSize = cipher.doFinal(data, ivOffset + iv.length, size - ivOffset - iv.length, getBuffer(), ivOffset);

            if (headless) {
                return parseRawHeadless(getBuffer(), ivOffset + decodedSize);
            }

            return parseRawPacket(getBuffer(), ivOffset + decodedSize);
        } catch (Exception ex) {
            throw new PacketDecryptException("Error decrypting VPN packet", ex);
        }
    }

    @Override
    public VpnPacket decrypt(byte[] data, int size) {
        return decrypt(data, size, IV_OFFSET, false);
    }

    @Override
    public VpnPacket decryptHeadless(byte[] data, int size) {
        return decrypt(data, size, 1, true);
    }
}
