package com.inveno.sire.task;

import com.inveno.sire.service.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Klaus Liu on 2016/9/22.
 * 定时同步数据任务
 */
@Component
public class RedisToEsDatasSupportTask implements ITask {

    private static final Logger logger = LoggerFactory.getLogger(RedisToEsDatasSupportTask.class);

    @Autowired
    private IService serviceSupport;

    public void syncDatas() {
        serviceSupport.syncDatas();
    }

    public void execute(){
        logger.debug("start timeing sync datas ...");
        syncDatas();
        logger.debug("end timeing sync datas ...");
    }

}
