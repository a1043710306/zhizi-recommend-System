/**
 * 
 */
package inveno.spider.parser.extractor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import inveno.spider.common.RabbitmqHelper;
import inveno.spider.common.RedisHelper2;
import inveno.spider.common.utils.LoggerFactory;
import inveno.spider.parser.base.Html;
import inveno.spider.parser.base.Html2Xml.Strategy;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.base.Page.Meta;
import inveno.spider.parser.base.ParseStrategy;
import inveno.spider.parser.model.ApiConfig;
import inveno.spider.parser.model.CharacterType;
import inveno.spider.parser.model.PageType;
import inveno.spider.parser.model.ParamConfig;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.model.ResultConfig;
import inveno.spider.parser.model.ResultParam;
import inveno.spider.parser.report.CrawlerReport;
import inveno.spider.parser.store.Article;
import inveno.spider.parser.store.ArticleStore;
import inveno.spider.rmq.model.QueuePrefix;
import net.minidev.json.JSONObject;

/**
 * @author Administrator
 *
 */
public class JsonExtractor implements Extractor {

	private static final Logger LOG = LoggerFactory.make();

	/** 用来标记JSON处理器解析配置中的ID，共插件使用 */
	public static final String JSON_PROCESS_DATA_ID = "jsonProcessDataId";

	private Profile profile;

	private ParseStrategy mParseStrategy;

	private ArticleStore mArticleStore;

	private CrawlerReport mCrawlerReport;

	private Page page;

	public JsonExtractor(Profile profile, ParseStrategy parseStrategy, ArticleStore articleStore,
			CrawlerReport crawlerReport) {
		this.profile = profile;
		this.mParseStrategy = parseStrategy;
		this.mArticleStore = articleStore;
		this.mCrawlerReport = crawlerReport;
	}

	@Override
	public List<Page> extract(Page page) {
		LOG.info("ContentExtract " + page.getUrl());
		ApiConfig apiConfig = getApiConfigByUrl(page.getUrl());
		if (apiConfig == null)
			return Collections.emptyList();

		List<ResultConfig> resultConfigs = apiConfig.getResultConfigs();

		Html htmlSource = page.getHtml();

		List<Page> pages = new ArrayList<Page>();

		Map<String, List<Map<String, String>>> processorDatas = parseJsonToDataMap(htmlSource.getHtml(), resultConfigs);
		if (apiConfig.getPageType() == PageType.LIST) {
			// 组装新的URL
			ApiConfig listApiConfig = null;
			for (ApiConfig config : profile.getApiConfigs()) {
				if (config.getPageType() == PageType.LIST) {
					listApiConfig = config;
					break;
				}
			}

			if (listApiConfig != null) {
				ApiConfig detailApiConfig = null;
				for (ApiConfig config : profile.getApiConfigs()) {
					if (config.getPageType() == PageType.DETAIL) {
						detailApiConfig = config;
						break;
					}
				}

				String url = detailApiConfig.getUrl().replaceAll("%26", "&");// 暂时从配置文件读取

				for (Map<String, String> map : processorDatas
						.get(listApiConfig.getResultConfigs().get(0).getEntity())) {
					LOG.debug("result count=" + map.size());
					if (detailApiConfig != null) {
						List<ParamConfig> params = detailApiConfig.getParamConfigs();
						Map<String, String> paramMap = new HashMap<String, String>();
						for (ParamConfig param : params) {
							if (StringUtils.isNotBlank(param.getReferEntity())
									&& StringUtils.isNotBlank(param.getReferField())) {
								paramMap.put(param.getName(), map.get(param.getReferField()));
							} else {
								paramMap.put(param.getName(), param.getValue());
							}
						}

						url = buildNewUrl(url, paramMap);

						Page detailPage = new Page(page.getId(), url, Extractor.Type.API, 1, null);
						detailPage.putAllMeta(page.getMeta());

						detailPage.setProfileName(page.getProfileName());
						detailPage.setPubCode(page.getPubCode());
						detailPage.setAncestorUrl(page.getUrl());

						detailPage.setBatchId(page.getBatchId());

						detailPage.setRssId(String.valueOf(page.getRssId()));
						detailPage.setTypeCode(page.getTypeCode());
						detailPage.setSource(page.getSource());
						detailPage.setLevel(page.getLevel());
						detailPage.putMeta(Meta.tags, page.getMeta(Meta.tags));
						detailPage.putMeta(Meta.location, page.getMeta(Meta.location));
						detailPage.putMeta(Meta.isOpenComment, page.getMeta(Meta.isOpenComment));
						detailPage.putMeta(Meta.dataType, page.getMeta(Meta.dataType));
						detailPage.putMeta(Meta.infoType, page.getMeta(Meta.infoType));
						detailPage.putMeta(Meta.checkCategoryFlag, page.getMeta(Meta.checkCategoryFlag));
						detailPage.putMeta(Meta.categoryName, page.getMeta(Meta.categoryName));

						pages.add(detailPage);
					}
				}
			}
		} else if (apiConfig.getPageType() == PageType.DETAIL) {
			// 组装内容
			for (Map<String, String> map : processorDatas.get(apiConfig.getResultConfigs().get(0).getEntity())) {

				Article oldArticle = buildOldArticle(page, map);
				LOG.info("SYS_MESSAGE_QUEUE :SYS_MESSAGE_QUEUE ");
				LOG.info("oldArticle : "+oldArticle);
//				RabbitmqHelper.getInstance().sendMessage(QueuePrefix.SYS_MESSAGE_QUEUE.name(), oldArticle);
//				RedisHelper2.getInstance().lpush("", value);
				
				String newArticle = buildNewArticle(page, map);
//				RabbitmqHelper.getInstance().sendMessage(QueuePrefix.SYS_NEW_MESSAGE_QUEUE.name(), newArticle);
				LOG.info("newArticle : "+newArticle);
				RedisHelper2.getInstance().lpush(QueuePrefix.SYS_NEW_MESSAGE_QUEUE.name(), newArticle);
			}
		}

		return pages;
	}

	private Article buildOldArticle(Page page, Map<String, String> map) {
		Article article = new Article();
		
		article.setmUrl(map.get("mUrl"));
		article.setmDate(dealPublishDate(map.get("mDate")));//特殊处理，可能是时间戳也可能是字符串
		article.setmTitle(map.get("mTitle"));
		article.setSummary(map.get("summary"));
		article.setmContent(map.get("mContent"));
		article.setmAuthor(map.get("mAuthor"));
		article.setmRefererUrl(map.get("mRefererUrl"));
		article.setPubCode(page.getPubCode());
		article.setProfileName(page.getProfileName());
		article.setRssId(page.getRssId());
		article.setTypeCode(page.getTypeCode());
		article.setSource(page.getSource());
		article.setBatchId(page.getBatchId());
		article.setCreateTime(new Date());
		
		Map<String, String> images = new HashMap<String, String>();
		if(map.containsKey("imgurl")) {
			images.put(map.get("imgurl"), map.get("imgurl"));
		}
		if (images != null)
		{
		    article.getImages().putAll(images);
		}
		article.setLevel(page.getLevel());
		String isOpenComment = (String) page.getMeta(Meta.isOpenComment);
		if (null != isOpenComment)
		{
		    article.setIsOpenComment(Integer.valueOf(isOpenComment));
		}
		String dataType = (String) page.getMeta(Meta.dataType);
		if (null != dataType)
		{
		    article.setDataType(Integer.valueOf(dataType));
		}
		String infoType = (String) page.getMeta(Meta.infoType);
		if (null != infoType)
		{
		    article.setInfoType(Integer.valueOf(infoType));
		}
		String categoryName = (String) page.getMeta(Meta.categoryName);
		if (null != categoryName)
		{
		    article.setCategoryName(categoryName);
		}
		String checkCategoryFlag = (String) page.getMeta(Meta.checkCategoryFlag);
		if (null != checkCategoryFlag)
		{
		    article.setCheckCategoryFlag(Integer.valueOf(checkCategoryFlag));
		}
		String tags = (String) page.getMeta(Meta.tags);
		if (null != tags)
		{
		    article.setTags(tags);
		}

		String location = (String) page.getMeta(Meta.location);
		if (null != location)
		{
		    article.setLocation(location);
		}
		return article;
	}
	
	//TODO some fields need read form page,such 'language' and 'country' etc.
	private String buildNewArticle(Page page, Map<String, String> map) {
		JSONObject article = new JSONObject();
		
		article.put("pubCode", page.getPubCode());
		String infoType = (String) page.getMeta(Meta.infoType);
		if (null != infoType)
		{
		    article.put("type", Integer.valueOf(infoType));
		} else {
			article.put("type", 0);
		}
		article.put("publisher", page.getSource());
		article.put("sourceType", "web");
		article.put("source", page.getSource());
		article.put("sourceFeeds", "");
		article.put("channel", "");
		article.put("author", map.get("mAuthor"));
		article.put("keywords", new HashMap<String, Object>());
		article.put("categories", new HashMap<String, Object>());
		article.put("language", "zh_CN");
		article.put("country", "CHN");
		article.put("publishTime", dealPublishDate(map.get("mDate")));
		article.put("discoveryTime", new Date());
		article.put("fetchTime", new Date());
		article.put("sourceItemId", "");
		article.put("link", map.get("mUrl"));
		article.put("title", map.get("mTitle"));
		article.put("content", map.get("mContent"));
		article.put("summary", map.get("summary"));
		article.put("copyright", "");
		article.put("hasCopyright", 0);
		article.put("bodyImages", new ArrayList<Object>());
		article.put("listImages", new ArrayList<Object>());
		article.put("bodyImagesCount", 0);
		article.put("listImagesCount", 0);
		article.put("publisherPagerankScore", 0);
		article.put("sourceCommentCount", 0);
		article.put("fallImage", "");
		article.put("displayListImages", new ArrayList<Object>());
		article.put("isOpenComment", 1);
		article.put("commentCount", 0);
		article.put("local", 0);
		
		return article.toJSONString();
	}

	/**
	 * 处理资讯的原始发布时间
	 * @param dateObj
	 * @return
	 */
	private Date dealPublishDate(Object dateObj) {
		if(dateObj == null)
			return null;
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(dateObj.toString().matches("\\d+")) {
			date = new Date(1000L * Long.valueOf(dateObj.toString()));
		}
		try {
			date = format.parse(dateObj.toString());
		} catch (ParseException e) {
			LOG.error("parse string to date has exception." + e);
		}
		return date;
	}

	// 构造列表页面url，这里不需要从其他api返回的内容中组装请求参数，只支持get
	private String buildNewUrl(String originalUrl, Map<String, String> paramMap) {
		// 从url获取所有参数，暂时只支持a=a&b=b这种，对于parameterStr={"a"="a","b"="b"}这种不支持
		String paramStr = originalUrl.substring(originalUrl.indexOf("?") + 1);
		String[] paramArray = paramStr.split("&");
		Map<String, String> hasOwnParams = new HashMap<String, String>();
		if (paramArray.length > 0) {
			String param = null;
			String name = null;
			for (int i = 0; i < paramArray.length; i++) {
				param = paramArray[i];
				if (param != null && param.contains("=")) {
					name = param.split("=")[0];
					if (paramMap.containsKey(name)) {
						hasOwnParams.put(name, paramMap.get(name));
					} else {
						if (param.split("=").length == 2)
							hasOwnParams.put(name, param.split("=")[1]);
						if (param.split("=").length == 1)
							hasOwnParams.put(name, "");
					}
				}
			}
		}

		// 组装新的请求参数
		StringBuffer requestBuffer = new StringBuffer();
		for (Entry<String, String> entry : hasOwnParams.entrySet()) {
			requestBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		String newParamStr = requestBuffer.substring(0, requestBuffer.length() - 1);

		String newUrl = originalUrl.substring(0, originalUrl.indexOf("?") + 1) + newParamStr;
		return newUrl;
	}

	/**
	 * 将json转成map
	 * 
	 * @param html
	 * @param resultConfig
	 * @return
	 */
	private Map<String, List<Map<String, String>>> parseJsonToDataMap(String html, List<ResultConfig> resultConfigs) {
		Map<String, List<Map<String, String>>> parseDatas = new HashMap<String, List<Map<String, String>>>();
		if (html != null && !html.isEmpty() && resultConfigs.size() > 0) {
			for (ResultConfig resultConfig : resultConfigs) {
				if (!resultConfig.isMulti()) {
					Map<String, String> curData = parseJsonData(html, resultConfig);
					buildData(parseDatas, resultConfig.getEntity(), curData);
				} else {
					List<Map<String, String>> curDatas = parseMultiJsonData(html, resultConfig);
					if (null != curDatas) {
						for (Map<String, String> item : curDatas)
							buildData(parseDatas, resultConfig.getEntity(), item);
					}
				}
			}
		}
		return parseDatas;
	}

	private void buildData(Map<String, List<Map<String, String>>> source, String entity, Map<String, String> data) {
		// 如果jsonConfigId是null，则默认将数据添加到空字符串为key的map中
		if (entity == null || entity.isEmpty())
			entity = "";

		List<Map<String, String>> curDatas = source.get(entity);
		if (null == curDatas) {
			curDatas = new LinkedList<Map<String, String>>();
			source.put(entity, curDatas);
		}
		if (null != data) {
			if (entity != null && !entity.isEmpty()) {
				// 用来标记，方便在插件中识别是哪种类型的数据
				data.put(JSON_PROCESS_DATA_ID, entity);
			}
			curDatas.add(data);
		}

	}

	/**
	 * @Description 按照模板的配置，解析部分json字符串，并将解析结果返回。
	 * @param sourceJsonText
	 * @param configs
	 * @return
	 */
	private Map<String, String> parseJsonData(String html, ResultConfig resultConfig) {
		Map<String, String> datas = new HashMap<String, String>();
		if (null != resultConfig) {
			List<ResultParam> configs = resultConfig.getResultParams();
			if (html != null && !html.isEmpty() && null != configs) {
				Configuration conf = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);// 如果leaf节点不存在，则返回null
				DocumentContext docContext = JsonPath.using(conf).parse(html);
				Map<String, Object> baseData = docContext.read(resultConfig.getBasePath());
				if (null != baseData) {
					String originalBaseJsonPath = resultConfig.getBasePath();
					if (null != configs) {
						extractResultProps(originalBaseJsonPath, baseData, datas, configs, docContext);
					}
				}
			}
		}
		return datas;
	}

	private List<Map<String, String>> parseMultiJsonData(String sourceJsonText, ResultConfig resultConfig) {
		List<Map<String, String>> datas = new LinkedList<Map<String, String>>();
		if (null != resultConfig) {
			String baseJsonPath = resultConfig.getBasePath();
			if (baseJsonPath != null && !baseJsonPath.isEmpty() && (!baseJsonPath.endsWith(".*")))
				baseJsonPath += ".*";
			Configuration conf = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);// 如果leaf节点不存在，则返回null
			DocumentContext docContext = JsonPath.using(conf).parse(sourceJsonText);
			List<Map<String, Object>> baseDatas = docContext.read(baseJsonPath);
			if (null != baseDatas) {
				String originalBaseJsonPath = resultConfig.getBasePath();
				for (Map<String, Object> item : baseDatas) {
					Map<String, String> curData = new HashMap<String, String>();
					List<ResultParam> configs = resultConfig.getResultParams();
					if (null != configs) {
						extractResultProps(originalBaseJsonPath, item, curData, configs, docContext);
						datas.add(curData);
					}
				}
			}
		}

		return datas;
	}

	/**
	 * 抽取返回内容
	 * 
	 * @param originalBaseJsonPath
	 * @param item
	 * @param curData
	 * @param configs
	 */
	private void extractResultProps(String originalBaseJsonPath, Map<String, Object> item, Map<String, String> curData,
			List<ResultParam> configs, DocumentContext docContext) {
		
		StringBuffer contentBuffer = new StringBuffer();
		String contentText = null;
		String imgUrl = null;
		
		for (ResultParam resultParam : configs) {
			String fixKey = resultParam.getOriginalName();
			if (fixKey.startsWith(originalBaseJsonPath)) {
				fixKey = fixKey.substring(originalBaseJsonPath.length() + 1);
			}
			Object jsonValue = item.get(fixKey);
			if (null != jsonValue)
				curData.put(resultParam.getName(), jsonValue.toString());
			
			// 对name为imgurl和contenttext的特殊处理,然后组装content
			if (resultParam.getName().equals("contenttext")) {
				// 对数组类型进行处理
				StringBuffer sb = new StringBuffer();
				if (resultParam.getType().equals("array")) {
					String path = resultParam.getPath();
					List<Map<String, String>> list = null;
					try {
						list = docContext.read(originalBaseJsonPath + "." + path);
					} catch (Exception e) {
						LOG.error("parse json to object has exception|" + e);
					}
					if(list != null && list.size() > 0) {
						for(Map<String, String> map : list) {
							if(map.get(resultParam.getOriginalName()) != null)
								sb.append(map.get(resultParam.getOriginalName()));
						}
					}
				}
				contentText = sb.toString();
			}
			if (resultParam.getName().equals("imgurl")) {
				if("map".equals(resultParam.getType())){
					Map<String, Object> objMap = (Map<String, Object>) item.get(resultParam.getPath());
					Object val = objMap.get(resultParam.getOriginalName());
					if(val != null)
						imgUrl = val.toString();
				} else {
					try {
						imgUrl = docContext.read(originalBaseJsonPath + "." + resultParam.getOriginalName());
					} catch (Exception e) {
						LOG.error("parse json to object has exception|" + e);
					}
				}
			}
		}
		
		// 对img和text进行添加标签处理
		if(imgUrl != null) {
			contentBuffer.append("<img src=\"").append(imgUrl).append("\" ></img>");
			curData.put("imgurl", imgUrl);
		}
		if(contentText != null) {
			contentBuffer.append("<p>").append(contentText).append("</p>");
		} else {
			contentBuffer.append("<p>").append(curData.get("mContent")).append("</p>");
		}
		
		curData.put("mContent", contentBuffer.toString());
	}

	@Override
	public String getCharset() {
		List<ApiConfig> apiConfigs = profile.getApiConfigs();
		if (apiConfigs != null && apiConfigs.size() > 0)
			return apiConfigs.get(0).getCharacterType().name();
		// 设置默认编码
		return CharacterType.UTF8.name();
	}

	private ApiConfig getApiConfigByUrl(String url) {
		ApiConfig apiConfig = null;
		for (ApiConfig config : profile.getApiConfigs()) {
			if (url.matches(config.getRegUrl())) {
				apiConfig = config;
				break;
			}
		}
		return apiConfig;
	}

	@Override
	public Strategy getHtml2XmlStrategy() {
		return null;
	}
	
}
