package pro.ra_tech.ra_vpn.common.crypto;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.ra_tech.ra_vpn.common.bin.BinHelper;
import pro.ra_tech.ra_vpn.common.proto.ConnectPacket;
import pro.ra_tech.ra_vpn.common.proto.DataTransferPacket;
import pro.ra_tech.ra_vpn.common.proto.KeepAliveAckPacket;
import pro.ra_tech.ra_vpn.common.proto.KeepAlivePacket;
import pro.ra_tech.ra_vpn.common.proto.payload.ConnectPayload;

import javax.crypto.KeyGenerator;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static pro.ra_tech.ra_vpn.common.Constants.AES_BITS;
import static pro.ra_tech.ra_vpn.common.crypto.AesPacketEncryptor.KEY_ALGORITHM;

@Slf4j
class AesPacketEncryptorTest {
    private AesPacketEncryptor buildEncryptor() throws NoSuchAlgorithmException {
        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(AES_BITS);

        val key = keyGenerator.generateKey();
        return new AesPacketEncryptor(Base64.getEncoder().encode(key.getEncoded()));
    }

    @Test
    void connectPacketTest() throws NoSuchAlgorithmException {
        val addr = InetAddress.ofLiteral("1.1.1.1");
        val packet = new ConnectPacket("test", addr, addr);

        val encryptor = buildEncryptor();
        val data = encryptor.encrypt(packet);
        val result = encryptor.decrypt(data.array(), data.readableBytes());

        Assertions.assertEquals(packet.getPayload(), result.getPayload());
    }

    @Test
    void keepAlivePacketTest() throws NoSuchAlgorithmException {
        val addr = InetAddress.ofLiteral("1.1.1.1");
        val packet = new KeepAlivePacket(new ConnectPayload("test", addr, addr));

        val encryptor = buildEncryptor();
        val data = encryptor.encrypt(packet);
        val result = encryptor.decrypt(data.array(), data.readableBytes());

        Assertions.assertEquals(packet.getPayload(), result.getPayload());
    }

    @Test
    void keepAliveAckPacketTest() throws NoSuchAlgorithmException {
        val addr = InetAddress.ofLiteral("1.1.1.1");
        val packet = new KeepAliveAckPacket("test", addr, addr);

        val encryptor = buildEncryptor();
        val data = encryptor.encrypt(packet);
        val result = encryptor.decrypt(data.array(), data.readableBytes());

        Assertions.assertEquals(packet.getPayload(), result.getPayload());
    }

    @Test
    void dataTransferPacketTest() throws NoSuchAlgorithmException {
        val addr = InetAddress.ofLiteral("1.1.1.1");
        val testData = new byte[] { 9, 9, 9, 9, 9, 9, 9, 9, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 2, 3, 4, 5, 3, 2 };
        val packet = new DataTransferPacket(testData, addr, addr);

        val encryptor = buildEncryptor();
        val data = encryptor.encrypt(packet);
        val result = encryptor.decrypt(data.array(), data.readableBytes());

        Assertions.assertArrayEquals(packet.getPayload().toBytes(), result.getPayload().toBytes());
    }
}
