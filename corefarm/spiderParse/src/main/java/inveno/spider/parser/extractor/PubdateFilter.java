package inveno.spider.parser.extractor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PubdateFilter {
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    private String mCompareDate;

    public PubdateFilter(Date date) {
        mCompareDate = df.format(date);
    }
    
    public boolean isEarlier(Date date) {
        return df.format(date).compareTo(mCompareDate) < 0;
    }
}
