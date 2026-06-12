package pro.ra_tech.ra_vpn.common.crypto;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.ra_tech.ra_vpn.common.proto.ConnectPacket;

import java.net.InetAddress;

class XorEncryptorTest {
    @Test
    void connectPacketTest() {
        val addr = InetAddress.ofLiteral("0.0.0.0");
        val packet = new ConnectPacket("test", addr, addr);
        val cryptor = new XorPacketEncryptor();

        val data = cryptor.encrypt(packet);
        val result = cryptor.decrypt(data.array(), data.array().length);

        Assertions.assertArrayEquals(packet.getPayload().toBytes(), result.getPayload().toBytes());
    }
}
