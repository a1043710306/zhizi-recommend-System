package inveno.spider.parser.exception;

public class AnalyzeException extends Exception {
    private static final long serialVersionUID = 1L;

    public AnalyzeException(String msg) {
        super(msg);
    }
    public AnalyzeException(Exception cause) {
        super(cause);
    }
    public AnalyzeException(String msg, Exception cause) {
        super(msg, cause);
    }
}
