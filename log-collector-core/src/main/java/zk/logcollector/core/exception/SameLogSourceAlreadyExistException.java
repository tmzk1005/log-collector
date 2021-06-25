package zk.logcollector.core.exception;

/**
 * @author zoukang
 */
public class SameLogSourceAlreadyExistException extends CreateLogSourceFailedException {

    private final String logSourceName;

    public SameLogSourceAlreadyExistException(String logSourceName) {
        super("Same log source name " + logSourceName + " already exist.");
        this.logSourceName = logSourceName;
    }

    public String getLogSourceName() {
        return logSourceName;
    }

}
