package com.inveno.sire.service.impl;

import com.inveno.sire.es.Ies;
import com.inveno.sire.redis.IRedis;
import com.inveno.sire.service.IService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Klaus Liu on 2016/9/20.
 */
@Component
public class ServiceSupport implements IService {

    @Autowired
    private IRedis simpleRedisSupport;

    @Autowired
    private Ies simpleEsSupport;

    public void syncDatas() {
        Map<String, Map<String, Double>> mapDocGmp = simpleRedisSupport.getDatas();
        simpleEsSupport.updateDatas(mapDocGmp);
    }
}
