package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.typeHandlers;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
public class CantReduceException extends Exception {
    public CantReduceException() {
    }

    public CantReduceException(String message) {
        super(message);
    }

    public CantReduceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CantReduceException(Throwable cause) {
        super(cause);
    }

    public CantReduceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
