package inveno.spider.parser.processor;

public class Progress {
    private int mCurrent;
    private int mTotal;
    public Progress() {
        this(0,0);
    }
    public Progress(int current, int total) {
        this.mCurrent = current;
        this.mTotal = total;
    }
    public int getCurrent() {
        return mCurrent;
    }
    public int getTotal() {
        return mTotal;
    }
    public void addTotal(int value) {
        mTotal += value;
    }
    public void addCurrent(int value) {
        mCurrent += value;
    }
    public Progress copy() {
        return new Progress(mCurrent, mTotal);
    }
}
