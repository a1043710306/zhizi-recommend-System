package com.inveno.sire.redis;

import java.util.Map;

/**
 * Created by Klaus Liu on 2016/9/20.
 */
public interface IRedis {

	Map<String,Map<String,Double>> getDatas();
}
