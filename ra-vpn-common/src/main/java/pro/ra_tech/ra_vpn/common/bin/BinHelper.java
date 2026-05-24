package pro.ra_tech.ra_vpn.common.bin;

import lombok.val;
import org.apache.commons.io.HexDump;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static pro.ra_tech.ra_vpn.common.Constants.MAX_CLIENT_ID_LENGTH;

public interface BinHelper {
    static void hexDump(Logger log, byte[] buffer, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        try {
            HexDump.dump(buffer, 0, sb, offset, length);
            log.info("Packet content: \r\n{}", sb);
        } catch (Exception ex) {
            log.error("Error dumping packet", ex);
        }
    }

    static void hexDump(Logger log, byte[] buffer) {
        hexDump(log, buffer, 0, buffer.length);
    }

    static InetAddress toInetAddress(byte[] bytes, int offset) throws UnknownHostException {
        val addr = new byte[4];
        System.arraycopy(bytes, offset, addr, 0, 4);

        return InetAddress.getByAddress(addr);
    }

    static String toClientId(byte[] bytes, int offset) {
        int idLength = MAX_CLIENT_ID_LENGTH;
        for (int i = offset; i < offset + MAX_CLIENT_ID_LENGTH && i < bytes.length; ++i) {
            if (bytes[i] == 0) {
                idLength = i - offset;
                break;
            }
        }

        if (idLength == 0) {
            throw new IllegalArgumentException("CONNECT: Invalid client id");
        }

        return new String(bytes, offset, idLength);
    }
}
