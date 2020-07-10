package com.inveno.sire.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Klaus Liu on 2016/9/21.
 * 加载本地文件到内存
 */
public class LoadProfileToMember {

    private static final Logger logger = LoggerFactory.getLogger(LoadProfileToMember.class);

    private static Map<String,String> cacheData= new HashMap<String,String>();

    private Properties prop = new Properties();

    public void init() {
        // TODO Auto-generated method stub
        logger.debug("start loader properties into local member...");
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("es.properties");
        try {
            prop.load(is);
            Set<Object> keys = prop.keySet();
            Iterator<Object> it = keys.iterator();
            while(it.hasNext()){
                Object objKey = it.next();
                if(objKey instanceof String){
                    String realKey = (String)objKey;
                    cacheData.put(realKey,(String)prop.get(realKey));
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("load properties error:",e);
        }
        logger.debug("loader properties into local member successful...");
    }

    public static String getValues(String key){
        return cacheData.get(key);
    }

}
