package inveno.spider.parser.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import inveno.spider.parser.model.ApiConfig;
import inveno.spider.parser.model.ParamConfig;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.model.ResultConfig;
import inveno.spider.parser.model.ResultParam;


public class XmlProfileReader {
    private static final Digester d = new Digester();
    static {
        d.addObjectCreate("crawling_plan", ProfileBuilder.class);
        d.addSetProperties("crawling_plan");

        // feeds
        d.addObjectCreate("crawling_plan/feeds/url", ListingPageBuilder.class);
        d.addSetProperties("crawling_plan/feeds/url");
        d.addCallMethod("crawling_plan/feeds/url", "setUrl", 0);
        d.addSetNext("crawling_plan/feeds/url", "addFeed");
        
        // listings
        d.addObjectCreate("crawling_plan/listings/url", ListingPageBuilder.class);
        d.addSetProperties("crawling_plan/listings/url");
        d.addCallMethod("crawling_plan/listings/url", "setUrl", 0);
        d.addSetNext("crawling_plan/listings/url", "addListing");
        
        // apis
        d.addObjectCreate("crawling_plan/apis/api", ApiConfig.class);
        d.addCallMethod("crawling_plan/apis/api", "setApiConfig", 3);
        d.addCallParam("crawling_plan/apis/api", 0, "httpMethodType");
        d.addCallParam("crawling_plan/apis/api", 1, "type");
        d.addCallParam("crawling_plan/apis/api", 2, "charset");
        
        d.addBeanPropertySetter("crawling_plan/apis/api/reg_url", "regUrl");
        d.addBeanPropertySetter("crawling_plan/apis/api/url", "url");
        
        d.addObjectCreate("crawling_plan/apis/api/params/param", ParamConfig.class);
        d.addSetProperties("crawling_plan/apis/api/params/param");
        d.addBeanPropertySetter("crawling_plan/apis/api/params/param", "value");
        
        d.addSetNext("crawling_plan/apis/api/params/param", "addParamConfig");
        
        d.addObjectCreate("crawling_plan/apis/api/results", ResultConfig.class);
        d.addCallMethod("crawling_plan/apis/api/results", "setResultConfig", 4);
        d.addCallParam("crawling_plan/apis/api/results", 0, "responseType");
        d.addCallParam("crawling_plan/apis/api/results", 1, "entity");
        d.addCallParam("crawling_plan/apis/api/results", 2, "multi");
        d.addCallParam("crawling_plan/apis/api/results", 3, "base-path");
        
        d.addObjectCreate("crawling_plan/apis/api/results/prop", ResultParam.class);
        d.addBeanPropertySetter("crawling_plan/apis/api/results/prop", "originalName");
        d.addSetProperties("crawling_plan/apis/api/results/prop");
        
        d.addSetNext("crawling_plan/apis/api/results/prop", "addResultParam");
        d.addSetNext("crawling_plan/apis/api/results", "addResultConfig");
        d.addSetNext("crawling_plan/apis/api", "addApiConfig");
        
        // feed extractor
        d.addObjectCreate("crawling_plan/feed_extractor", FeedConfigBuilder.class);
        d.addSetProperties("crawling_plan/feed_extractor");
        
        d.addCallMethod("crawling_plan/feed_extractor/feed_path", "setRssAllPath",7);
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 0,"title");
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 1,"content");
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 2,"pubDate");
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 3,"description");
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 4,"source");
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 5,"tags");
        d.addCallParam("crawling_plan/feed_extractor/feed_path", 6,"link");
        d.addSetNext("crawling_plan/feed_extractor", "setFeedConfigBuilder");
        
        // listing extractor
        d.addObjectCreate("crawling_plan/listing_extractor", ListingConfigBuilder.class);
        d.addSetProperties("crawling_plan/listing_extractor");

        d.addCallMethod("crawling_plan/listing_extractor/link_path", "setLinkPath", 6);
        d.addCallParam("crawling_plan/listing_extractor/link_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/link_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/link_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/link_path", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/link_path", 4, "match_strategy");
        d.addCallParam("crawling_plan/listing_extractor/link_path", 5, "unescapeHtml");
        
        d.addCallMethod("crawling_plan/listing_extractor/title_path", "setTitlePath", 6);
        d.addCallParam("crawling_plan/listing_extractor/title_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/title_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/title_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/title_path", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/title_path", 4, "match_strategy");
        d.addCallParam("crawling_plan/listing_extractor/title_path", 5, "additionalPath");
        
        d.addCallMethod("crawling_plan/listing_extractor/summary_path", "setSummaryPath", 5);
        d.addCallParam("crawling_plan/listing_extractor/summary_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/summary_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/summary_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/summary_path", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/summary_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/author_path", "setAuthorPath", 5);
        d.addCallParam("crawling_plan/listing_extractor/author_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/author_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/author_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/author_path", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/author_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/section_path", "setSectionPath", 5);
        d.addCallParam("crawling_plan/listing_extractor/section_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/section_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/section_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/section_path", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/section_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/pageno_path", "setPagenoPath", 5);
        d.addCallParam("crawling_plan/listing_extractor/pageno_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/pageno_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/pageno_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/pageno_path", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/pageno_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/date_path", "setDatePath", 8);
        d.addCallParam("crawling_plan/listing_extractor/date_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/date_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/date_path", 2, "date_strategy");
        d.addCallParam("crawling_plan/listing_extractor/date_path", 3, "pattern");
        d.addCallParam("crawling_plan/listing_extractor/date_path", 4, "country");
        d.addCallParam("crawling_plan/listing_extractor/date_path", 5, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/date_path", 6, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/date_path", 7, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/reply_number_path", "setReplyNumberPath", 6);
        d.addCallParam("crawling_plan/listing_extractor/reply_number_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/reply_number_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/reply_number_path", 2, "num_strategy");
        d.addCallParam("crawling_plan/listing_extractor/reply_number_path", 3, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/reply_number_path", 4, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/reply_number_path", 5, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/click_number_path", "setClickNumberPath", 6);
        d.addCallParam("crawling_plan/listing_extractor/click_number_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/click_number_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/click_number_path", 2, "num_strategy");
        d.addCallParam("crawling_plan/listing_extractor/click_number_path", 3, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/click_number_path", 4, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/click_number_path", 5, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/nextpage_path", "setNextpagePath", 7);
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 0);
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 1, "strategy");
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 2, "maxpages");
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 3, "page_strategy");
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 4, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 5, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/nextpage_path", 6, "match_strategy");
        
        d.addCallMethod("crawling_plan/listing_extractor/mannual_redirection", "setManualRedirectConfig", 6);
        d.addCallParam("crawling_plan/listing_extractor/mannual_redirection", 0, "if_match_redirect");
        d.addCallParam("crawling_plan/listing_extractor/mannual_redirection", 1, "redirect_strategy");
        d.addCallParam("crawling_plan/listing_extractor/mannual_redirection", 2, "regular_expression");
        d.addCallParam("crawling_plan/listing_extractor/mannual_redirection", 3, "replace_with");
        d.addCallParam("crawling_plan/listing_extractor/mannual_redirection", 4, "content_add_refererUrl");
        d.addCallParam("crawling_plan/listing_extractor/mannual_redirection", 5, "unescapeHtml");
        
        d.addSetNext("crawling_plan/listing_extractor", "setListingConfigBuilder");

        // content extractor
        d.addObjectCreate("crawling_plan/content_extractor", ContentConfigBuilder.class);
        d.addSetProperties("crawling_plan/content_extractor");

        d.addCallMethod("crawling_plan/content_extractor/content_path", "setContentPath", 8);
        d.addCallParam("crawling_plan/content_extractor/content_path", 0);
        d.addCallParam("crawling_plan/content_extractor/content_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/content_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/content_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/content_path", 4, "match_strategy");
        d.addCallParam("crawling_plan/content_extractor/content_path", 5, "filter_nodes");
        d.addCallParam("crawling_plan/content_extractor/content_path", 6, "replaceLabelToDiv");
        d.addCallParam("crawling_plan/content_extractor/content_path", 7, "outputURL");
        
        d.addCallMethod("crawling_plan/content_extractor/title_path", "setTitlePath", 6);
        d.addCallParam("crawling_plan/content_extractor/title_path", 0);
        d.addCallParam("crawling_plan/content_extractor/title_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/title_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/title_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/title_path", 4, "match_strategy");
        d.addCallParam("crawling_plan/content_extractor/title_path", 5, "additionalPath");
        
        d.addCallMethod("crawling_plan/content_extractor/summary_path", "setSummaryPath", 5);
        d.addCallParam("crawling_plan/content_extractor/summary_path", 0);
        d.addCallParam("crawling_plan/content_extractor/summary_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/summary_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/summary_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/summary_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/author_path", "setAuthorPath", 5);
        d.addCallParam("crawling_plan/content_extractor/author_path", 0);
        d.addCallParam("crawling_plan/content_extractor/author_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/author_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/author_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/author_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/section_path", "setSectionPath", 5);
        d.addCallParam("crawling_plan/content_extractor/section_path", 0);
        d.addCallParam("crawling_plan/content_extractor/section_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/section_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/section_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/section_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/pageno_path", "setPagenoPath", 5);
        d.addCallParam("crawling_plan/content_extractor/pageno_path", 0);
        d.addCallParam("crawling_plan/content_extractor/pageno_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/pageno_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/pageno_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/pageno_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/date_path", "setDatePath", 8);
        d.addCallParam("crawling_plan/content_extractor/date_path", 0);
        d.addCallParam("crawling_plan/content_extractor/date_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/date_path", 2, "date_strategy");
        d.addCallParam("crawling_plan/content_extractor/date_path", 3, "pattern");
        d.addCallParam("crawling_plan/content_extractor/date_path", 4, "country");
        d.addCallParam("crawling_plan/content_extractor/date_path", 5, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/date_path", 6, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/date_path", 7, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/img_path", "setImagePath", 1);
        d.addCallParam("crawling_plan/content_extractor/img_path", 0, "src_tag");
        
        //post
        d.addCallMethod("crawling_plan/content_extractor/post_link_path", "setPostLinkPath", 5);
        d.addCallParam("crawling_plan/content_extractor/post_link_path", 0);
        d.addCallParam("crawling_plan/content_extractor/post_link_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/post_link_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/post_link_path", 3, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/post_link_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/post_num_path", "setPostNumPath", 6);
        d.addCallParam("crawling_plan/content_extractor/post_num_path", 0);
        d.addCallParam("crawling_plan/content_extractor/post_num_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/post_num_path", 2, "num_strategy");
        d.addCallParam("crawling_plan/content_extractor/post_num_path", 3, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/post_num_path", 4, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/post_num_path", 5, "match_strategy");
        //end
        
        d.addCallMethod("crawling_plan/content_extractor/nextpage_path", "setNextpagePath", 7);
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 0);
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 1, "strategy");
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 2, "maxpages");
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 3, "page_strategy");
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 4, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 5, "replace_with");
        d.addCallParam("crawling_plan/content_extractor/nextpage_path", 6, "match_strategy");
        
        d.addCallMethod("crawling_plan/content_extractor/mannual_redirection", "setMannualRedirectConfig", 4);
        d.addCallParam("crawling_plan/content_extractor/mannual_redirection", 0, "if_match_redirect");
        d.addCallParam("crawling_plan/content_extractor/mannual_redirection", 1, "redirect_strategy");
        d.addCallParam("crawling_plan/content_extractor/mannual_redirection", 2, "regular_expression");
        d.addCallParam("crawling_plan/content_extractor/mannual_redirection", 3, "replace_with");
        
        d.addSetNext("crawling_plan/content_extractor", "setContentConfigBuilder");
        
        //post extractor
        d.addObjectCreate("crawling_plan/post_extractor", PostConfigBuilder.class);
        d.addSetProperties("crawling_plan/post_extractor");

        d.addCallMethod("crawling_plan/post_extractor/comment_path", "setCommentPath", 8);
        d.addCallParam("crawling_plan/post_extractor/comment_path", 0);
        d.addCallParam("crawling_plan/post_extractor/comment_path", 1, "strategy");
        d.addCallParam("crawling_plan/post_extractor/comment_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/comment_path", 3, "replace_with");
        d.addCallParam("crawling_plan/post_extractor/comment_path", 4, "match_strategy");
        d.addCallParam("crawling_plan/post_extractor/comment_path", 5, "filter_nodes");
        d.addCallParam("crawling_plan/post_extractor/comment_path", 6, "replaceLabelToDiv");
        d.addCallParam("crawling_plan/post_extractor/comment_path", 7, "outputURL");
        
        d.addCallMethod("crawling_plan/post_extractor/author_photo_path", "setAuthorPhotoPath", 6);
        d.addCallParam("crawling_plan/post_extractor/author_photo_path", 0);
        d.addCallParam("crawling_plan/post_extractor/author_photo_path", 1, "strategy");
        d.addCallParam("crawling_plan/post_extractor/author_photo_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/author_photo_path", 3, "replace_with");
        d.addCallParam("crawling_plan/post_extractor/author_photo_path", 4, "match_strategy");
        d.addCallParam("crawling_plan/post_extractor/author_photo_path", 5, "additionalPath");
        
        d.addCallMethod("crawling_plan/post_extractor/author_path", "setAuthorPath", 5);
        d.addCallParam("crawling_plan/post_extractor/author_path", 0);
        d.addCallParam("crawling_plan/post_extractor/author_path", 1, "strategy");
        d.addCallParam("crawling_plan/post_extractor/author_path", 2, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/author_path", 3, "replace_with");
        d.addCallParam("crawling_plan/post_extractor/author_path", 4, "match_strategy");
        
        d.addCallMethod("crawling_plan/post_extractor/like_num_path", "setLikeNumPath", 6);
        d.addCallParam("crawling_plan/post_extractor/like_num_path", 0);
        d.addCallParam("crawling_plan/post_extractor/like_num_path", 1, "strategy");
        d.addCallParam("crawling_plan/post_extractor/like_num_path", 2, "num_strategy");
        d.addCallParam("crawling_plan/post_extractor/like_num_path", 3, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/like_num_path", 4, "replace_with");
        d.addCallParam("crawling_plan/post_extractor/like_num_path", 5, "match_strategy");
        
        
        d.addCallMethod("crawling_plan/post_extractor/post_time_path", "setPostTimePath", 8);
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 0);
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 1, "strategy");
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 2, "date_strategy");
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 3, "pattern");
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 4, "country");
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 5, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 6, "replace_with");
        d.addCallParam("crawling_plan/post_extractor/post_time_path", 7, "match_strategy");
        
        d.addCallMethod("crawling_plan/post_extractor/nextpage_path", "setNextpagePath", 7);
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 0);
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 1, "strategy");
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 2, "maxpages");
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 3, "page_strategy");
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 4, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 5, "replace_with");
        d.addCallParam("crawling_plan/post_extractor/nextpage_path", 6, "match_strategy");
        
        d.addCallMethod("crawling_plan/post_extractor/mannual_redirection", "setMannualRedirectConfig", 4);
        d.addCallParam("crawling_plan/post_extractor/mannual_redirection", 0, "if_match_redirect");
        d.addCallParam("crawling_plan/post_extractor/mannual_redirection", 1, "redirect_strategy");
        d.addCallParam("crawling_plan/post_extractor/mannual_redirection", 2, "regular_expression");
        d.addCallParam("crawling_plan/post_extractor/mannual_redirection", 3, "replace_with");
        
        d.addSetNext("crawling_plan/post_extractor", "setPostConfigBuilder");   
        //end
        
    }
    
    public static void main(String[] args) {
		String xml = XmlProfileReader.getFileContext("C:\\t\\news_heynortheast.xml");
		try {
			XmlProfileReader.parse(xml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    public static synchronized Profile parse(String xml) throws Exception {
        try {
            ProfileBuilder builder = (ProfileBuilder) d.parse(new StringReader(xml));
            return builder.build(xml);
        } finally {
        	d.clear();
        	d.resetRoot();
        }
    }
    
    public static String getFileContext(String path)
	{
		File file = new File(path);
		StringBuffer sb = new StringBuffer();
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			InputStreamReader bis = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(bis);
			String str = "";
			while(true) 
			{
				str = br.readLine();
				
				if(null == str)
					break;
				sb.append(str+"\n");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
    
}
