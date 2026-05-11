package pro.ra_tech.ra_vpn.common.crypto;

import lombok.val;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.ra_vpn.common.exceptions.CipherConfigException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public interface EncryptorFactory {
    static PacketEncryptor ofType(EncryptorType type, @Nullable String encryptorKeyFilePath) {
        return switch (type) {
            case DUMMY -> new DummyPacketEncryptor();
            case XOR -> new XorPacketEncryptor();
            case AES -> {
                if (encryptorKeyFilePath == null) {
                    throw new CipherConfigException("No cipher key file provided");
                }

                try {
                    val key = Files.readString(Paths.get(encryptorKeyFilePath), StandardCharsets.US_ASCII).trim();
                    yield new AesPacketEncryptor(key.getBytes(StandardCharsets.US_ASCII));
                } catch (IOException ex) {
                    throw new CipherConfigException("Error reading cipher key file:", ex);
                }
            }
        };
    }
}
