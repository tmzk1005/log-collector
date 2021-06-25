package zk.logcollector.core.exception;

/**
 * @author zoukang
 */
public class CreateLogSourceFailedException extends Exception {

    public CreateLogSourceFailedException(Throwable throwable) {
        super(throwable);
    }

    public CreateLogSourceFailedException(String message) {
        super(message);
    }

}
