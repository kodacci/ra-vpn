package pro.ra_tech.ra_vpn.keygen;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.crypto.KeyGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static pro.ra_tech.ra_vpn.common.Constants.AES_BITS;

@RequiredArgsConstructor
public class Keygen {
    private final String path;

    void generate() {
        try {
            val keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_BITS);
            val key = keyGenerator.generateKey();
            val encoded = Base64.getEncoder().encode(key.getEncoded());

            Files.write(Path.of(path), encoded);
        } catch (Exception ex) {
            throw new KeygenException("Error generating key:", ex);
        }
    }
}
