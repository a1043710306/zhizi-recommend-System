package com.inveno.core.process.pre.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.common.filter.impl.ReadedInfoHandler;
import com.inveno.common.util.ContextUtils;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.pre.IPrePolicy;
import com.inveno.thrift.ResponParam;

@Component("readedFileterPolicy")
public class ReadedFileterPolicy implements IPrePolicy<List<ResponParam>> {

	private Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private MonitorLog monitorLog;

	@Autowired
	ReadedInfoHandler readFilterInfo;

	@Override
	public List<ResponParam> process(Context context) throws TException {
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		try {
			readedFilter(context);
			monitorLog.addResTimeLogByProduct(context, MonitorType.ACS_RESPONSE_TIME,(System.currentTimeMillis() - cur));
		} catch (Exception e) {
			logger.error("=== readedFilter Exception ,and uid is "  + uid + " abtest "+  context.getAbtestVersion() +"===",e);
		}
		return null;
	}


	/**
	 * @throws Exception
	 *
	 * readedFilter(已读过滤)
	 * @Title: readedFilter
	 * @param @param list
	 * @param @return    设定文件
	 * @return List<String>    返回类型
	 * @throws
	 */
	private void readedFilter(Context context) throws Exception{
		String uid = context.getUid();
		long cur = System.currentTimeMillis();

		List<ResponParam> responseParamList = context.responseParamList;

		List<ResponParam> reList = new ArrayList<ResponParam>();

		if (responseParamList.size() >= 500) {
			if (logger.isDebugEnabled()) {
				logger.debug("uid is: "+ uid +" ,begin readFile ReadedFileterPolicy,and time is " + (System.currentTimeMillis()-cur) +",and size is " + 500 +",and cur: " + System.currentTimeMillis() );
			}
			reList.addAll(new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(0,499 ))));
			if (reList.size() < context.getNum()) {
				if (logger.isDebugEnabled()) {
					logger.debug("uid is: "+ uid +" ,begin readFile ReadedFileterPolicy,and time is " + (System.currentTimeMillis()-cur) +",and size is " + reList.size() +",and cur: " + System.currentTimeMillis() );
				}

				if (responseParamList.size() > 1500) {
					reList.addAll( new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(500,1500 ))));
					reList.addAll( new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(1500,responseParamList.size() ))));
				} else {
					reList.addAll( new ArrayList<ResponParam>(readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList.subList(500,responseParamList.size() ))));
				}
			}
		} else {
			reList = readFilterInfo.filterIds(context,context.getUid(), context.getApp(), responseParamList);
		}

		logger.info("uid is: "+ uid +" ,end readFile ReadedFileterPolicy, moudle=ReadedFileterPolicy,filterList size= = "+responseParamList.size()+",and time is " + (System.currentTimeMillis()-cur) +",and size is " + reList.size() +",and cur: " + System.currentTimeMillis() );

		if (reList.size() < context.getNum()) {
			monitorLog.addCntLogByProduct(context, MonitorType.FALLBACK_REQUEST_COUNT_RECREADED);
		}

		//logger.info(" end ================PreProcessImpl readedFilter====================time is " + (System.currentTimeMillis() - cur) +",and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(reList);
	}
}
