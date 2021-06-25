package zk.logcollector.plugin.api;

public interface Encoder<T> extends Plugin {

    String CONF_KEY = "encoder";

    T encode(LogRecord logRecord);

}
