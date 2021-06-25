package zk.logcollector.plugin.api;

public class ConfigureFailedException extends Exception {

    public ConfigureFailedException(Throwable throwable) {
        super(throwable);
    }

    public ConfigureFailedException(String message) {
        super(message);
    }

}
