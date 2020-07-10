package inveno.spider.parser.report;


import inveno.spider.parser.store.Article;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

public class ArticleReportVerifier {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private String mThreeDaysAgo;
    private String mToday;
    {
        mToday = df.format(new Date());
        mThreeDaysAgo = df.format(DateUtils.addDays(new Date(), -3));
    }
    public void verify(Article article, ArticleEvent event) {
//      article.getUrl();
//      article.getAuthor();
//      article.getSection();

        // verify title
        if(article.getTitle()==null) {
            event.appendMessage("Title is null");
        } else if(StringUtils.isBlank(article.getTitle())) {
            event.appendMessage("Title is blank");
        } else if(article.getTitle().contains("\n") || article.getTitle().contains("\r")) {
            event.appendMessage("Title contains new line");
        }

        // verify content
        if(article.getContent()==null) {
            event.appendMessage("Content is null");
        } else if(StringUtils.isBlank(article.getContent())) {
            event.appendMessage("Content is blank");
        }
        
        // verify date
        if(article.getDate()==null) {
            event.appendMessage("Date is null");
        } else {
            String date = df.format(article.getDate());
            if(date.compareTo(mToday) > 0) {
                event.appendMessage("Date in future " + date);
            } else if(date.compareTo(mThreeDaysAgo) < 0) {
                event.appendMessage("Date too old " + date);
            } else if(date.compareTo(mToday) < 0) {
                event.appendMessage("Date is not today " + date);
            }
        }
    }
}
