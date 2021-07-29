package simplenem12;

public class NemParserException extends Exception {

    public NemParserException(String message) {
        super(message);
    }

    public NemParserException(String message, Throwable t) {
        super(message, t);
    }
}
