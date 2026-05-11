package pro.ra_tech.ra_vpn.common.tun;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TunReaderHandler {
    public static Thread start(TunReader tunReader) {
        val thread = new Thread(() -> {
            try {
                log.info("Started reading from tun device {}", tunReader.getTunName());
                for (;;) {
                    tunReader.read();
                }
            } catch (InterruptedException _) {
                log.warn("Tun reader interrupted");
                Thread.currentThread().interrupt();
            }
        }, "TunReader" + tunReader.getTunName());

        thread.start();

        return thread;
    }
}
