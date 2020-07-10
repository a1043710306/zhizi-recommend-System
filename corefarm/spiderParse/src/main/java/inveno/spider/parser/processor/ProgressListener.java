package inveno.spider.parser.processor;

public interface ProgressListener {
    void updateProgress(CrawlWorker worker, Progress progress);
}
