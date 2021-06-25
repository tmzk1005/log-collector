package zk.logcollector.plugin.api;

public interface Extractor extends Plugin {

    LogRecord extract(final LogRecord logRecord);

}
