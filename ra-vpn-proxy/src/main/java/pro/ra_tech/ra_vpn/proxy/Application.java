package pro.ra_tech.ra_vpn.proxy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.ra_tech.ra_vpn.common.crypto.EncryptorType;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@Command(name = "ra-vpn-proxy", mixinStandardHelpOptions = true, version = "1.0.0", showDefaultValues = true)
public class Application implements Callable<Integer> {
    public static class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

        @Override
        public Level convert(String value) {
            return Level.valueOf(value);
        }
    }

    @Option(names = {"-H", "--host"}, description = "host to listen on", defaultValue = "0.0.0.0")
    String host;
    @Option(names = {"-p", "--port"}, description = "port to listen on", required = true)
    int port;
    @Option(names = {"-s", "--server-host"}, description = "VPN server host to proxy packets to", required = true)
    String serverHost;
    @Option(names = {"-r", "--server-port"}, description = "VPN server port to proxy packets to", required = true)
    int serverPort;
    @Option(names = {"-e", "--encryptor"}, description = "traffic encryptor, one of: ${COMPLETION-CANDIDATES}", defaultValue = "DUMMY")
    EncryptorType encryptorType = EncryptorType.DUMMY;
    @Option(names = {"-k", "--key-file"}, description = "cipher key file path")
    String encryptorKeyFilePath;
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

        new ProxyServer(
                host,
                port,
                new InetSocketAddress(serverHost, serverPort),
                encryptorType,
                encryptorKeyFilePath
        ).start();

        return 0;
    }

    static void main(String[] args) {
        new CommandLine(new Application()).execute(args);
    }
}
