package zk.logcollector.plugin.api;

public interface Input extends Plugin {

    LogRecord emit();

    default boolean supportDistributed() {
        return false;
    }

}
