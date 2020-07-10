package inveno.spider.common.exception;

public class CrawlHttpException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private boolean contentLengthTooLarge = false;

    public CrawlHttpException(String msg) {
        super(msg);
    }
    public CrawlHttpException(Exception e) {
        super(e);
        if (e instanceof CrawlHttpException) {
            CrawlHttpException che = (CrawlHttpException) e;
            this.contentLengthTooLarge = che.isContentLengthTooLarge();
        }
    }
    public CrawlHttpException(String msg, Exception e) {
        super(msg, e);
        if (e instanceof CrawlHttpException) {
            CrawlHttpException che = (CrawlHttpException) e;
            this.contentLengthTooLarge = che.isContentLengthTooLarge();
        }
    }
    public CrawlHttpException(String msg, boolean contentLengthTooLarge) {
        super(msg);
        this.contentLengthTooLarge = contentLengthTooLarge;
    }
    public boolean isContentLengthTooLarge() {
        return contentLengthTooLarge;
    }
}
