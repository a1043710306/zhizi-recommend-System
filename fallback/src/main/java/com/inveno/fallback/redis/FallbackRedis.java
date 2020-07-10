package com.inveno.fallback.redis;

import java.util.List;

import com.inveno.fallback.model.MessageContentEntry;

public interface FallbackRedis {
	
     void process(List<MessageContentEntry> messageContentModelList, String key, int size, String category);
     
}
