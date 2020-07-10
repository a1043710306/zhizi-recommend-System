package com.inveno.zhiziArticleFilter;


public interface IArticleFilter {
	
	public void setQueryProperty(ArticleFilterProperty property);
	public ArticleFilterProperty getQueryProperty();
	
	public boolean isValidArticle(ArticleProperty article_property);
	
}