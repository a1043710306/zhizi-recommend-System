package com.inveno.fallback.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.fallback.constant.FallbackConstant;
import com.inveno.fallback.es.FallbackES;
import com.inveno.fallback.model.MessageContentEntry;
import com.inveno.fallback.redis.FallbackRedis;
import com.inveno.fallback.server.DataBaseContentLoader;
import com.inveno.fallback.server.PropertyContentLoader;
import com.inveno.fallback.service.FallbackService;

@Component
public class SimpleEsQueryAndWriterToRedisFallbackTaskImpl implements FallbackService {

	private static final Logger logger = LoggerFactory.getLogger(SimpleEsQueryAndWriterToRedisFallbackTaskImpl.class);

	@Autowired
	private FallbackES fallbackESImpl;

	@Autowired
	private FallbackRedis fallbackRedisImpl;

	//All
	List<MessageContentEntry> allCategoryidList = new ArrayList<MessageContentEntry>();

	public String handler() {
		// TODO Auto-generated method stub

		logger.debug("start search es system and write redis process...");

		try {
			List<Integer> categoryIds = DataBaseContentLoader.getValues(FallbackConstant.T_CATEGORYID_VALUES);

			//渠道FIRM_APP和多语言版本
			String[] channelLanguages = PropertyContentLoader.getValues(FallbackConstant.FIRM_APP_LANGUAGES).split(",");

			//多语言
			String[] contentTypes = PropertyContentLoader.getValues(FallbackConstant.CONTENT_TYPES).split(":");

			logger.debug("fallback handle category list: " + categoryIds);
			for (String channelLanguage : channelLanguages) {
				String[] realChlLangs = channelLanguage.split("=");
				String channel = realChlLangs[0];
				String[] languages = realChlLangs[1].split(":");
				for (String language : languages) {

					for (String contentType : contentTypes) {

						String finalContentType = contentType.equals("1") ? "" : FallbackConstant.BLANK_VALUE + contentType;

						String keyAll = FallbackConstant.REDIS_FALLBACK_VALUE + FallbackConstant.BLANK_VALUE + channel
								+ FallbackConstant.BLANK_VALUE + language + FallbackConstant.BLANK_VALUE + FallbackConstant.REDIS_ALL_VALUE
								+ finalContentType;

						for (Integer categoryId : categoryIds) {

							String keyCategoryid = FallbackConstant.REDIS_FALLBACK_VALUE + FallbackConstant.BLANK_VALUE + channel
									+ FallbackConstant.BLANK_VALUE + language + FallbackConstant.BLANK_VALUE + categoryId
									+ finalContentType;

							List<MessageContentEntry> messageContentModels = fallbackESImpl.getEsContents(channel, categoryId, language, contentType, Arrays.asList(contentTypes));

							allCategoryidList.addAll(messageContentModels);

							logger.debug("fallback keyCategoryid=" + keyCategoryid + " ES query result size=" + messageContentModels.size());
							fallbackRedisImpl.process(messageContentModels, keyCategoryid,
								Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.REDIS_STORE_CATEGORYID_COUNT)),
								categoryId.toString());
						}

						if (allCategoryidList.size() > 0) {
							fallbackRedisImpl.process(allCategoryidList, keyAll,
									Integer.valueOf(PropertyContentLoader.getValues(FallbackConstant.REDIS_STORE_ALL_COUNT)), "all");
						}
						allCategoryidList.clear();
					}
				}
			}

		} catch (Exception e) {
			logger.error("SimpleEsQueryAndWriterToRedisServiceImpl exception:", e);
		}

		return "success";
	}
}
