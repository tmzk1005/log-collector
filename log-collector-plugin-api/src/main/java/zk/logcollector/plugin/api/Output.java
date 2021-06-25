package zk.logcollector.plugin.api;

public interface Output extends Plugin {

    void emit(LogRecord logRecord);

}
