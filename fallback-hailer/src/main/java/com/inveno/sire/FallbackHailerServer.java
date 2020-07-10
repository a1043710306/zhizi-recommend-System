package com.inveno.sire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.inveno.sire.util.LoadProfileToMember;

/**
 * Created by Klaus Liu on 2016/9/20.
 */
public class FallbackHailerServer {

    private static final Logger logger = LoggerFactory.getLogger(FallbackHailerServer.class);

    public static void main(String[] args) {
        try {
            logger.debug("FallbackHailerServer start ....");
            new LoadProfileToMember().init();
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:applicationcontext.xml");
            logger.debug("FallbackHailerServer 启动成功 ....");
        } catch (Exception e) {
            logger.error("FallbackHailerServer 启动失败 ....",e);
        }

    }

}
