package inveno.spider.parser.exception;

public class ExtractException extends Exception {
    private static final long serialVersionUID = 1L;

    public ExtractException(String msg) {
        super(msg);
    }
    public ExtractException(Exception cause) {
        super(cause);
    }
    public ExtractException(String msg, Exception cause) {
        super(msg, cause);
    }
}
