package inveno.spider.common;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import inveno.spider.common.utils.Config;

public class Constants
{
	private static final Logger logger = Logger.getLogger(Constants.class);
	
    public static  String REDIS_HOST;
    public static  String REDIS_HOST2;
    public static  int REDIS_PORT;
    public static  int REDIS_TIMEOUT;

    public static  String RABBIT_HOST;
    public static  String RABBIT_HOST_URL;
    public static  int RABBIT_PORT;
    public static  String RABBIT_USERNAME;
    public static  String RABBIT_PASSWORD;

    public static  String DB_CONNECTION_URL;
    public static  String DB_USERNAME;
    public static  String DB_PASSWORD;

    public static  String PROFILE_PATH;
    
    public static  String REDIS_RELEASE_HOST;
    
    public static  int REDIS_RELEASE_PORT;

    public static  String IMAGE_CLOUD_ACCESS_APP;
    public static  String IMAGE_CLOUD_ACCESS_KEY;
    public static  String IMAGE_CLOUD_URI_ICON;
    public static  String IMAGE_CLOUD_URI_UPLOAD;
    public static  String BLACKLIST_FALLIMAGE_ID;
    
    public static  String ENV_CODE;

    static
    {
        initProperties();
    }

	/**
	 * 
	 */
	private static void initProperties() {
		try{
			Config config = new Config("spider-common.properties");
	        REDIS_HOST = config.getPropertyNotEmpty("redis.host");
	        REDIS_HOST2 = config.getPropertyNotEmpty("redis.host2");
	        REDIS_PORT = config.getIntPropertyNotEmpty("redis.port");
	        REDIS_TIMEOUT=config.getIntPropertyNotEmpty("redis.timeout");
	
	        //RABBIT_HOST = config.getPropertyNotEmpty("rabbit.host");
	       // RABBIT_PORT = config.getIntPropertyNotEmpty("rabbit.port");
	       // RABBIT_HOST_URL = "http://" + RABBIT_HOST + ":15672";
	       // RABBIT_USERNAME = config.getPropertyNotEmpty("rabbit.username");
	       // RABBIT_PASSWORD = config.getPropertyNotEmpty("rabbit.password");


	
	        DB_CONNECTION_URL = config.getPropertyNotEmpty("db.connection.url");
	        DB_USERNAME = config.getPropertyNotEmpty("db.username");
	        DB_PASSWORD = config.getPropertyNotEmpty("db.password");
	
	        PROFILE_PATH = config.getPropertyNotEmpty("profile.path");
	        
	        REDIS_RELEASE_HOST = config.getPropertyNotEmpty("redis.release.host");
	        REDIS_RELEASE_PORT = config.getIntPropertyNotEmpty("redis.release.port");
	
	        IMAGE_CLOUD_URI_ICON   = (null == config.getProperty("repository.uri.icon"))   ? null : config.getPropertyNotEmpty("repository.uri.icon"); 
	        IMAGE_CLOUD_URI_UPLOAD = (null == config.getProperty("repository.uri.upload")) ? null : config.getPropertyNotEmpty("repository.uri.upload"); 
	        IMAGE_CLOUD_ACCESS_APP = (null == config.getProperty("repository.access.app")) ? null : config.getPropertyNotEmpty("repository.access.app");
	        IMAGE_CLOUD_ACCESS_KEY = (null == config.getProperty("repository.access.key")) ? null : config.getPropertyNotEmpty("repository.access.key");
	        ENV_CODE = (null == config.getProperty("environment")) ? "oversea" : config.getPropertyNotEmpty("environment");
	        
	        BLACKLIST_FALLIMAGE_ID   = (null == config.getProperty("blacklist.fallimage.id"))    ? "" : config.getPropertyNotEmpty("blacklist.fallimage.id");
		}catch (Exception e) {
			logger.error("init spider-common.properties has exception:", e);
		}
	}
	
    /**
     * 定时加载
     */
    public static void startTask() {
    	 ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();  
         // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  
    	 CleanerRunnable runnable = new CleanerRunnable();
         service.scheduleAtFixedRate(runnable, 2, 10, TimeUnit.MINUTES);  
    }
    
    static class CleanerRunnable implements Runnable {

		@Override
		public void run() {
			initProperties();
		}
    	
	}
	public static void main(String args[]){
		initProperties();
	}
	
}
