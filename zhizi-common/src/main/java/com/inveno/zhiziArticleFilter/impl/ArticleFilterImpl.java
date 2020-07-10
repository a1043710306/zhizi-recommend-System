package com.inveno.zhiziArticleFilter.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.inveno.zhiziArticleFilter.ArticleFilterProperty;
import com.inveno.zhiziArticleFilter.ArticleProperty;
import com.inveno.zhiziArticleFilter.IArticleFilter;


public class ArticleFilterImpl implements IArticleFilter ,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArticleFilterProperty savedArticleFilterProperty = new ArticleFilterProperty();

	public ArticleFilterImpl() 
	{
		savedArticleFilterProperty.setAdultScoreUpperbound(0.9);
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
		fmt.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		Date d = new Date();
		try {
			d = fmt.parse(fmt.format(new Date().getTime()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		savedArticleFilterProperty.setPublishTimeExpiredBefore(d.getTime() - (86400000*2));
		
		savedArticleFilterProperty.setZhizi_request(null);
		
	}
	
	public void setQueryProperty(ArticleFilterProperty property)
	{
		this.savedArticleFilterProperty = property;
	}
	public ArticleFilterProperty getQueryProperty()
	{
		return this.savedArticleFilterProperty;
	}
	
	private int turnStringToInteger(String strValue)
	{
		if (strValue.isEmpty() == false)
		{
			return Integer.parseUnsignedInt(strValue.substring(2), 16);
		}
		else
		{
			return 0;
		}
	}

	public boolean isValidArticle(ArticleProperty article_property) {
		
		if (article_property.getAdult_score() >= savedArticleFilterProperty.getAdultScoreUpperbound())
			return false;
		
		if (article_property.getPublish_timestamp() < savedArticleFilterProperty.getPublishTimeExpiredBefore())
			return false;
		
		if (article_property.getFirm_app() != null &&
				article_property.getFirm_app().contains(article_property.getProduct_id()) == false)
			return false;
		
		if (this.savedArticleFilterProperty.getZhizi_request() != null)
		{
			int content_type = Integer.valueOf(article_property.getType());
			int link_type = Integer.valueOf(article_property.getLink_type());
			int display_type = Integer.valueOf(article_property.getDisplay_type());
			
			int req_content_type = turnStringToInteger(this.savedArticleFilterProperty.getZhizi_request().getContent_type());
			int req_link_type = turnStringToInteger(this.savedArticleFilterProperty.getZhizi_request().getLink_type());
			int req_display_type = turnStringToInteger(this.savedArticleFilterProperty.getZhizi_request().getDisplay());
			
			/*
	 		 * We need to do content_type mapping for ali_test here.
	 		 * Doc's type is 0: news, 1: video. But for ZhiziListReq, 0: not defined, 1: news, 2: short video, 4: long video, ...
	 		 */
			
			if( content_type == 0 || link_type == 0 || display_type ==0 ){
				return false;
			}
			if( content_type > 0 ){
				if ( (req_content_type & content_type) == 0 ) { //ZhiziListReq::content_type is a set
		 			return false;
		 		}
			}
	 		
			if (link_type > 0) {
				if ( (req_link_type  & link_type) == 0 ) //ZhiziListReq::link_type is a set
					return false;
			}
			
			if (display_type > 0) {
				if ( (req_display_type & display_type) == 0 ) //ZhiziListReq::display_type is a set
					return false;
			}
		}
		
		return true;
	}
	
}
