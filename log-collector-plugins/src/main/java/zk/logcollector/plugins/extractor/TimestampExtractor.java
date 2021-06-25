package zk.logcollector.plugins.extractor;

import zk.logcollector.plugin.api.Extractor;
import zk.logcollector.plugin.api.LogRecord;

/**
 * @author zoukang60456
 */
public class TimestampExtractor implements Extractor {

    @Override
    public LogRecord extract(final LogRecord logRecord) {
        logRecord.setField("timestamp", System.currentTimeMillis());
        return logRecord;
    }

}
