package zk.logcollector.plugin.api;

public interface Decoder<T> extends Plugin {

    String CONF_KEY = "decoder";

    LogRecord decode(T t);

}
