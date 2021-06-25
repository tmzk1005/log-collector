package zk.logcollector.plugins.input;

import java.util.Objects;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;

import zk.logcollector.plugin.api.Input;
import zk.logcollector.plugin.api.LogRecord;

/**
 * @author zoukang60456
 */
@Slf4j
public class StdinInput implements Input {

    private transient Scanner scanner;

    public StdinInput() {
    }

    @Override
    public LogRecord emit() {
        try {
            String line = scanner.nextLine();
            return new LogRecord(line);
        } catch (Exception e) {
            // 分布式场景下，任务漂移重启时会发生异常，因为scanner的source也就是System.in关闭了，需要重新设置Scanner
            stop();
            start();
            return null;
        }
    }

    @Override
    public boolean supportDistributed() {
        return true;
    }

    @Override
    public void start() {
        log.info("Start stdin input");
        if (Objects.isNull(scanner)) {
            scanner = new Scanner(System.in);
        }
    }

    @Override
    public void stop() {
        log.info("Stop stdin input");
        // 不要close，要不然底层的System.in被close后就不能再reopen，导致任务不能重启
        scanner = null;
    }

}
