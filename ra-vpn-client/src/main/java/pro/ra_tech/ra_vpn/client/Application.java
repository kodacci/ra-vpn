package pro.ra_tech.ra_vpn.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.val;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import pro.ra_tech.ra_vpn.client.tcp.TcpClient;
import pro.ra_tech.ra_vpn.client.udp.UdpClient;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@CommandLine.Command(
        name = "ra-vpn-client",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        showDefaultValues = true
)
public class Application implements Callable<Integer> {
    public static class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

        @Override
        public Level convert(String value) {
            return Level.valueOf(value);
        }
    }

    @CommandLine.Option(names = {"-h", "--host"}, description = "host to connect to", required = true)
    String host;
    @CommandLine.Option(names = {"-p", "--port"}, description = "port to connect to", required = true)
    int port;
    @CommandLine.Option(names = {"-n", "--tun-number"}, description = "tun number", defaultValue = "21")
    int tunNumber;
    @CommandLine.Option(names = {"-e", "--encryptor"}, description = "use traffic encryptor, one of: ${COMPLETION-CANDIDATES}", defaultValue = "DUMMY")
    EncryptorType encryptorType = EncryptorType.DUMMY;
    @CommandLine.Option(names = {"-i", "--virtual-ip" }, description = "virtual ip address", defaultValue = "10.10.0.10")
    String virtualIp;
    @CommandLine.Option(names = {"-d", "--client-id" }, description = "client id", defaultValue = "test-client")
    String clientId;
    @CommandLine.Option(names = {"-k", "--key-file" }, description = "cipher key file path")
    String encryptorKeyFilePath;
    @CommandLine.Option(names = {"-t", "--tcp"}, description = "use tcp transport")
    boolean tcp;
    @CommandLine.Option(names = {"-l", "--log-level"}, description = "log level, one of: TRACE, DEBUG, INFO, WARN, ERROR", defaultValue = "INFO", converter = LogLevelConverter.class)
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
            new TcpClient(
                    new InetSocketAddress(host, port),
                    tunNumber,
                    virtualIp,
                    clientId,
                    encryptorType,
                    encryptorKeyFilePath
            ).start();

            return 0;
        }

        val client = new UdpClient(
                new InetSocketAddress(host, port),
                tunNumber,
                virtualIp,
                clientId,
                encryptorType,
                encryptorKeyFilePath
        );

        client.start();

        return 0;
    }

    static void main(String[] args) {
        new CommandLine(new Application()).execute(args);
    }
}

