package inveno.spider.parser.extractor;


import inveno.spider.parser.Constants;
import inveno.spider.parser.store.Article;
import inveno.spider.parser.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class ArticleVerifier
{
    public static boolean verifyArticle(Article article, String type, Date fromDate)
    {
        // if no content, ignore
        if (StringUtils.isBlank(article.getContent()))
            return false;
        if ("blog".equals(type))
        {
            if (fromDate != null)
            {
                if (article.getDate().before(fromDate))
                    return false;
            }
            if (article.getDate().before(Constants.EARLIEST_DATE))
                return false;

        }
        else
        { // news
            // 如果时间在今天之后，不抓取此页
            if (Utils.isAfterToday(article.getDate()))
                return false;

            // for news only crawl today's article
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            if (df.format(article.getDate()).compareTo(df.format(new Date())) < 0)
            {
                return false;
            }
        }

        return true;
    }

}
