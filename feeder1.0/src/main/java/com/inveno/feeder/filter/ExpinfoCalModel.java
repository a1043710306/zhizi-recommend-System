package com.inveno.feeder.filter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.Properties;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.inveno.feeder.constant.FeederConstants;
import com.inveno.feeder.thrift.FeederInfo;

import redis.clients.jedis.JedisCluster;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * 流量探索计算模型
 * @author klaus liu
 *
 */
public class ExpinfoCalModel
{
	private static final Logger expinfoLogger = Logger.getLogger("feeder.expinfo");

	private static J48 gmpClassfier;

	private static Double sourceAvgCtrValue;

	private static Double publisherArticleCountValue;

	private static Double publisherGt008ArticlesCountValue;

	private static JedisCluster jedisClient;

	private static HashMap<String, Instances> mClassifierInstance = new HashMap<String, Instances>();

	static
	{
		File configFile = new File("connection-info.properties");
		try
		{
			FileReader reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);

			String strRedisIPWithPort = props.getProperty("online-redis-ip_port");
			jedisClient = com.inveno.feeder.util.JedisHelper.newClientInstance(strRedisIPWithPort.split(";"));
			InitGmpClassifierModel(jedisClient);
		}
		catch(Exception e)
		{
    		expinfoLogger.fatal("ExpinfoClalModel read connection-info.properties file exception =", e);
		}
	}

	//获取分类器-调用一次
	public static void InitGmpClassifierModel(JedisCluster jedisClient)
	{
		//J48 j48 = null;
		ObjectInputStream ois = null;
		byte[] modelBytes = jedisClient.get(FeederConstants.GMP_CLASSIFIER_MODEL.getBytes());
		ByteArrayInputStream bais = new ByteArrayInputStream(modelBytes);
        try{
        	ois = new ObjectInputStream(bais);
        	gmpClassfier = (J48)ois.readObject();
		}catch(Exception e){
			expinfoLogger.fatal("InitGmpClassifierModel method exception :"+e);
		}
		//return gmpClassfier;
	}

	//获取分类树
	public static Double getRedisTreeValue(FeederInfo feederInfo, String path) throws Exception
	{
		//修改成相对路径
		Instances testData = (Instances)mClassifierInstance.get(path);
		if (testData == null)
		{
    		expinfoLogger.info("[getRedisTreeValue] start load file " + path);
			DataSource testSource = new DataSource(path);
			testData = testSource.getDataSet();
			testData.setClassIndex(0);
			mClassifierInstance.put(path, testData);
			expinfoLogger.info("[getRedisTreeValue] end load file " + path);
		}

		Instance demo = new Instance(7);
		demo.setDataset(testData);
		try
		{
			String stringSourceAvgCtrValue = jedisClient.hget("gmp-predict-source-avg-ctr",feederInfo.getSource());
			String stringPublisherArticleCountValue = jedisClient.hget("gmp-predict-publisher-articles",feederInfo.getPublisher());
			String stringPublisherGt008ArticlesCountValue = jedisClient.hget("gmp-predict-publisher-gt008-articles",feederInfo.getPublisher());
			sourceAvgCtrValue =  Double.valueOf(stringSourceAvgCtrValue == null ? "0.0" : stringSourceAvgCtrValue);
			publisherArticleCountValue =  Double.valueOf(stringPublisherArticleCountValue == null ? "0.0" : stringPublisherArticleCountValue);
			publisherGt008ArticlesCountValue =  Double.valueOf(stringPublisherGt008ArticlesCountValue == null ? "0.0" : stringPublisherGt008ArticlesCountValue);

			/*创建分类实例*/
			demo.setValue(0, "False");//GMP,输入"True"或"False"都可，第一参数为属性序号，第二参数为属性值，下同
			demo.setValue(1, feederInfo.getBody_image_count());//body_images_count
			demo.setValue(2, feederInfo.getList_image_count());//list_images_count
			demo.setValue(3, feederInfo.getHasCopyright());//trcontent_rate
			demo.setValue(4, sourceAvgCtrValue);//source_avg_ctr
			demo.setValue(5, publisherArticleCountValue);//publisher_articles_count
			demo.setValue(6, publisherGt008ArticlesCountValue);//publisher_gt008_articles_count

			expinfoLogger.info("[getRedisTreeValue] start gmpClassfier classifyInstance");
			double classification = gmpClassfier.classifyInstance(demo);
			expinfoLogger.info("[getRedisTreeValue] end gmpClassfier classifyInstance, result=" + classification);
			if (classification == 1)
			{
				return calcExpinfo(feederInfo, jedisClient);
			}
		}
		catch (Exception e)
		{
			expinfoLogger.fatal("getRedisTreeValue method exception :", e);
		}
		return 0.0;
	}

	//计算选择
	public static Double calcExpinfo(FeederInfo feederInfo, JedisCluster jedisClient)
	{
		expinfoLogger.info("calcExpinfo calc start ...");
		//list_images_count
		Integer listImagesCount = feederInfo.getList_image_count();
		Double listImagesCountWeight = Double.valueOf(jedisClient.get("list_images_count_weight"));
		Double listImagesCountMaxValue = Double.valueOf(jedisClient.get("list_images_count_max_value"));
		Double listImagesCountMinValue = Double.valueOf(jedisClient.get("list_images_count_min_value"));
		Double listImagesCountScore = calc(listImagesCountWeight,listImagesCount == null?0.0:listImagesCount.doubleValue(),listImagesCountMaxValue,listImagesCountMinValue);
		expinfoLogger.info("listImagesCount="+listImagesCount+",listImagesCountWeight="+listImagesCountWeight
				+",listImagesCountMaxValue="+listImagesCountMaxValue+",listImagesCountMinValue="+listImagesCountMinValue);
		//Double listImagesCountScore = listImagesCountWeight*(listImagesCount-listImagesCountMinValue)/(listImagesCountMaxValue-listImagesCountMinValue);

		//body_images_count
		Integer bodyImagesCount = feederInfo.getBody_image_count();
		Double bodyImagesCountWeight = Double.valueOf(jedisClient.get("body_images_count_weight"));
		Double bodyImagesCountMaxValue = Double.valueOf(jedisClient.get("body_images_count_max_value"));
		Double bodyImagesCountMinValue = Double.valueOf(jedisClient.get("body_images_count_min_value"));
		Double bodyImagesCountScore =  calc(bodyImagesCountWeight,bodyImagesCount == null?0.0:bodyImagesCount.doubleValue(),bodyImagesCountMaxValue,bodyImagesCountMinValue);
		expinfoLogger.info("bodyImagesCount="+bodyImagesCount+",bodyImagesCountWeight="+bodyImagesCountWeight
				+",bodyImagesCountMaxValue="+bodyImagesCountMaxValue+",bodyImagesCountMinValue="+bodyImagesCountMinValue);
		//Double bodyImagesCountScore = bodyImagesCountWeight*(bodyImagesCount-bodyImagesCountMinValue)/(bodyImagesCountMaxValue-bodyImagesCountMinValue);

		//content_rate
		Integer contentRate = feederInfo.getRate();
		Double contentRateWeight = Double.valueOf(jedisClient.get("content_rate_weight"));
		Double contentRateMaxValue = Double.valueOf(jedisClient.get("content_rate_max_value"));
		Double contentRateMinValue = Double.valueOf(jedisClient.get("content_rate_min_value"));
		Double contentRateScore = calc(contentRateWeight,contentRate == null?0.0:contentRate.doubleValue(),contentRateMaxValue,contentRateMinValue);
		expinfoLogger.info("contentRate="+contentRate+",contentRateWeight="+contentRateWeight
				+",contentRateMaxValue="+contentRateMaxValue+",contentRateMinValue="+contentRateMinValue);
		//Double contentRateScore = contentRateWeight*(contentRate-contentRateMinValue)/(contentRateMaxValue-contentRateMinValue);

		//source_avg_ctr
		Double sourceavgCtr = sourceAvgCtrValue;
		Double sourceavgCtrWeight = Double.valueOf(jedisClient.get("source_avg_ctr_weight"));
		Double sourceavgCtrMaxValue = Double.valueOf(jedisClient.get("source_avg_ctr_max_value"));
		Double sourceavgCtrMinValue = Double.valueOf(jedisClient.get("source_avg_ctr_min_value"));
		Double sourceavgCtrScore = calc(sourceavgCtrWeight,sourceavgCtr,sourceavgCtrMaxValue,sourceavgCtrMinValue);
		expinfoLogger.info("sourceavgCtr="+sourceavgCtr+",sourceavgCtrWeight="+sourceavgCtrWeight
				+",sourceavgCtrMaxValue="+sourceavgCtrMaxValue+",sourceavgCtrMinValue="+sourceavgCtrMinValue);
		//Double sourceavgCtrScore = sourceavgCtrWeight*(sourceavgCtr-sourceavgCtrMinValue)/(sourceavgCtrMaxValue-sourceavgCtrMinValue);

		//publisher_articles_count
		Double publisherArticlesCount = publisherArticleCountValue;
		Double publisherArticlesCountWeight = Double.valueOf(jedisClient.get("publisher_articles_count_weight"));
		Double publisherArticlesCountMaxValue = Double.valueOf(jedisClient.get("publisher_articles_count_max_value"));
		Double publisherArticlesCountMinValue = Double.valueOf(jedisClient.get("publisher_articles_count_min_value"));
		Double publisherArticlesCountScore = calc(publisherArticlesCountWeight,publisherArticlesCount,publisherArticlesCountMaxValue,publisherArticlesCountMinValue);
		expinfoLogger.info("publisherArticlesCount="+publisherArticlesCount+",publisherArticlesCountWeight="+publisherArticlesCountWeight
				+",publisherArticlesCountMaxValue="+publisherArticlesCountMaxValue+",publisherArticlesCountMinValue="+publisherArticlesCountMinValue);
		//Double publisherArticlesCountScore = publisherArticlesCountWeight*(publisherArticlesCount-publisherArticlesCountMinValue)/(publisherArticlesCountMaxValue-publisherArticlesCountMinValue);

		//publisher_gt008_articles_count
		Double publisherGt008ArticlesCount = publisherGt008ArticlesCountValue;
		Double publisherGt008ArticlesCountWeight = Double.valueOf(jedisClient.get("publisher_gt008_articles_count_weight"));
		Double publisherGt008ArticlesCountMaxValue = Double.valueOf(jedisClient.get("publisher_gt008_articles_count_max_value"));
		Double publisherGt008ArticlesCountMinValue = Double.valueOf(jedisClient.get("publisher_gt008_articles_count_min_value"));
		Double publisherGt008ArticlesCountScore = calc(publisherGt008ArticlesCountWeight,publisherGt008ArticlesCount,publisherGt008ArticlesCountMaxValue,publisherGt008ArticlesCountMinValue);
		expinfoLogger.info("publisherGt008ArticlesCount="+publisherGt008ArticlesCount+",publisherGt008ArticlesCountWeight="+publisherGt008ArticlesCountWeight
				+",publisherGt008ArticlesCountMaxValue="+publisherGt008ArticlesCountMaxValue+",publisherGt008ArticlesCountMinValue="+publisherGt008ArticlesCountMinValue);
		//Double publisherGt008ArticlesCountScore = publisherGt008ArticlesCountWeight*(publisherGt008ArticlesCount-publisherGt008ArticlesCountMinValue)/(publisherGt008ArticlesCountMaxValue-publisherGt008ArticlesCountMinValue);

		Double result = listImagesCountScore+bodyImagesCountScore+contentRateScore+sourceavgCtrScore+publisherArticlesCountScore+publisherGt008ArticlesCountScore;
		expinfoLogger.info("contentId="+feederInfo.getContent_id()+",listImages="+listImagesCount+",bodyImages="+bodyImagesCount+",contentRate="+contentRate
				+",sourceavgCtr="+sourceavgCtr+",publisherArticles="+publisherArticlesCount+",gt008Articles="+publisherGt008ArticlesCount+",socre="+result);
		Double finalResult = Math.abs(1-result);
		return finalResult;
	}

	private  static Double calc(Double weight,Double count,Double maxValue,Double minValue){
		return weight*((count-minValue)/(maxValue-minValue));
	}

    public static void main(String[] args) throws Exception{
    	FeederInfo feederInfo = new FeederInfo();
    	feederInfo.setBody_image_count(2);
    	feederInfo.setList_image_count(3);
    	feederInfo.setHasCopyright(0);
    	getRedisTreeValue(feederInfo,"");
	}
}
