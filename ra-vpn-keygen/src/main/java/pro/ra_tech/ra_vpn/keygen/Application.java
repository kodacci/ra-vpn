package pro.ra_tech.ra_vpn.keygen;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(
        name = "ra-vpn-keygen",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        showDefaultValues = true
)
public class Application implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = "key.txt")
    String outFilePath;

    @Override
    public Integer call() {
        try {
            val keygen = new Keygen(outFilePath);
            keygen.generate();
        } catch (Exception ex) {
            log.error(ex.getMessage());

            return 1;
        }

        return 0;
    }

    static void main(String[] args) {
        new CommandLine(new Application()).execute(args);
    }
}
