package com.inveno.fallback.es;

import java.util.List;

import org.elasticsearch.action.search.SearchResponse;

import com.inveno.fallback.model.MessageContentEntry;

public interface FallbackES {

	
	int udateEsContents();
	
	List<MessageContentEntry> getEsContents(String firmApp,Integer categoryId,String language,String contentType,List<String> contentTypes,Double...args);
	
	SearchResponse getEsContents(String...args);
}
