package zk.logcollector.plugins.output;

import lombok.extern.slf4j.Slf4j;

import zk.logcollector.plugin.api.LogRecord;
import zk.logcollector.plugin.api.Output;
import zk.logcollector.utils.JsonUtil;

/**
 * @author zoukang60456
 */
@Slf4j
public class StdoutOutput implements Output {

    @Override
    public void emit(LogRecord logRecord) {
        System.out.println(JsonUtil.toPrettyJson(logRecord.getAllFields()));
    }

    @Override
    public void start() {
        log.info("Start stdout output");
    }

    @Override
    public void stop() {
        log.info("Stop stdout output");
    }

}
