package inveno.spider.parser.processor;

import java.util.ArrayList;
import java.util.List;

public class ProgressHelper {
    private List<ProgressListener> mListeners = new ArrayList<ProgressListener>(1);
    public void addProgressListener(ProgressListener listener) {
        mListeners.add(listener);
    }
    
    public void updateProgress(CrawlWorker worker, Progress progress) {
        for (ProgressListener listener : mListeners) {
            listener.updateProgress(worker, progress);
        }
    }
}
