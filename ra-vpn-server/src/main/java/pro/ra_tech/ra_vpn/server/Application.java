package pro.ra_tech.ra_vpn.server;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.val;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;
import pro.ra_tech.ra_vpn.server.tcp.TcpServer;
import pro.ra_tech.ra_vpn.server.udp.UdpServer;

import java.util.concurrent.Callable;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@Command(name = "ra-vpn-server", mixinStandardHelpOptions = true, version = "1.0.0", showDefaultValues = true)
public class Application implements Callable<Integer> {
    public static class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

        @Override
        public Level convert(String value) {
            return Level.valueOf(value);
        }
    }

    @Option(names = {"-h", "--host"}, description = "host to listen on", defaultValue = "0.0.0.0")
    String host;
    @Option(names = {"-p", "--port"}, description = "port to listen on", required = true)
    int port;
    @Option(names = {"-n", "--tun-number"}, description = "tun interface number (e.g. 1 will become utun1)", defaultValue = "21")
    int tunNumber;
    @Option(names = {"-c", "--network-cidr"}, description = "network ip with subnet", defaultValue = "10.10.0.1/24")
    String cidr;
    @Option(names = {"-e", "--encryptor"}, description = "use traffic encryptor, one of: ${COMPLETION-CANDIDATES}", defaultValue = "DUMMY")
    EncryptorType encryptorType = EncryptorType.DUMMY;
    @Option(names = {"-k", "--key-file"}, description = "cipher key file path")
    String encryptorKeyFilePath;
    @Option(names = {"-t", "--tcp"}, description = "use tcp transport")
    boolean tcp;
    @Option(names = {"-l", "--log-level"}, description = "log level, one of: TRACE, DEBUG, INFO, WARN, ERROR", defaultValue = "INFO", converter = LogLevelConverter.class)
    Level logLevel;

    @Override
    public Integer call() {
        ((Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME)).setLevel(logLevel);

        if (encryptorType == EncryptorType.AES && encryptorKeyFilePath == null) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: cipher key file required for AES encryptor"
            );
        }

        if (tcp) {
            new TcpServer(
                    host,
                    port,
                    tunNumber,
                    cidr,
                    encryptorType,
                    encryptorKeyFilePath
            ).start();

            return 0;
        }

        val server = new UdpServer(
                host,
                port,
                tunNumber,
                cidr,
                encryptorType,
                encryptorKeyFilePath
        );

        server.start();

        return 0;
    }

    static void main(String[] args) {
        new CommandLine(new Application()).execute(args);
    }
}

