package com.inveno.core.process.post.process;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.core.enumType.MonitorType;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

@Component("picSimHashControl")
public class PicSimHashControl implements IPostPolicy<List<ResponParam>>{

	private Log logger = LogFactory.getLog(this.getClass());


	@Autowired
	private MonitorLog monitorLog;

	@Override
	public List<ResponParam> process(Context context) throws TException {

		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();

		if(logger.isDebugEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " begin picSimHashControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis());
		}

		if (context.getResponseParamList().size() < 5) {
			return null;
		}

		boolean bEnableForListpage = Boolean.valueOf(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashControlEnableForListpage"));
		boolean bLockscreen = ContextUtils.isLockscreen(context);
		if (!bEnableForListpage && !bLockscreen) {
			return null;
		}

		int picSimHashControlwindowSize = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashControlwindowSize"), 8);
		int picSimHashDistance          = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashDistance"), 28);
		int picSimHashStart             = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashstart"), 0);
		int picSimHashEnd               = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "picSimHashend"), 100);

		if( logger.isTraceEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " begin picSimHashControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis() +" , before list " + context.getResponseParamList());
		}

		List<ResponParam> reList = null;
		try {
			reList = reRank(context.getResponseParamList(), picSimHashStart, picSimHashEnd, picSimHashControlwindowSize, picSimHashDistance, context);
			monitorLog.addResTimeLogByProduct(context, MonitorType.PICSIMHASHMOVE_RESPONSE_TIME,(System.currentTimeMillis() - cur));
			context.setResponseParamList(reList);
		} catch (Exception e) {
			logger.error(" uid :"+ uid  + " ,app "+  app +"  picSimHashControl Exception,time is,and cur = " + System.currentTimeMillis() +e.getCause(),e );
		}

		if( logger.isTraceEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " end picSimHashControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis() +" , after list " + reList);
		}

		long endTime  = System.currentTimeMillis();
		logger.info(" uid :"+ uid  + " ,app "+  app +" end picSimHashControl ,time is " +(endTime-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		return null;

	}

	public static List<ResponParam> reRank(List<ResponParam> inlist, int start,int end, int windowSize, int simHashDistance, Context context) {
		ArrayList<ResponParam> list = (ArrayList<ResponParam>)inlist;
		@SuppressWarnings("unchecked")
		ArrayList<ResponParam> result = (ArrayList<ResponParam>) list.clone();

		int len = result.size();
		HashSet<String> tagsInWindow = new HashSet<String>();
		end = Math.min(end, len);

		for (int i = start; i < end; i++) {
			String simhash =  result.get(i).getSimhashOfPic();

			if (i >= windowSize) {
				tagsInWindow.remove(result.get(i - windowSize).getSimhashOfPic());
			}

			if (StringUtils.isEmpty(simhash) || "-1".equals(simhash) || !simhash.startsWith("0x") || simhash.equalsIgnoreCase("null")) {
				continue;
			}

			boolean isContains = false;
			for(int j = i; j < len; j++){
				String simhashJ =  result.get(j).getSimhashOfPic();
				isContains = ifContain(tagsInWindow, simhashJ, simHashDistance);
				if (!isContains) {
					if (i != j) { // 需要移动
						ResponParam backwardItem = result.get(j); // 前移的item
						result.remove(backwardItem);
						result.add(i, backwardItem);
					}
					break;
				}
			}
			if (isContains) {
				// report cnt++
			}

			if (!StringUtils.isEmpty(simhash) &&  !"-1".equals(simhash) &&simhash.startsWith("0x") && !simhash.equalsIgnoreCase("null")) {
				tagsInWindow.add(result.get(i).getSimhashOfPic());
			}
		}

		return result;
	}


	public static boolean ifContain(HashSet<String> tagsInWindow,String simhashJ,int simHashDistance) {
		for (String simHash : tagsInWindow) {
			if( hammingDistance(new BigInteger(simHash.substring(2), 16), new BigInteger(simhashJ.substring(2), 16), simHashDistance) < simHashDistance){
				return true;
			}
		}
		return false;
	}

	public static int hammingDistance(BigInteger simhash1, BigInteger simhash2, int simHashDistance) {

		BigInteger x = simhash1.xor(simhash2);
		int tot = 0;

		// 统计x中二进制位数为1的个数
		// 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，对吧，然后，n&(n-1)就相当于把后面的数字清0，
		// 我们看n能做多少次这样的操作就OK了。

		while (x.signum() != 0) {
			if (++tot > simHashDistance) {
				break;
			}
			x = x.and(x.subtract(new BigInteger("1")));
		}
		return tot;
	}

	public static void main(String[] args) {
		String simHashStr = "0x5256e7edb1fd1c17aaba1ef0bddac5da";
		String simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";
		simHashStr1 = "0x3bd93c3d9c6674f2d638fcc911fc8823";

		System.out.println(new BigInteger(simHashStr.substring(2), 16).xor(new BigInteger(simHashStr1.substring(2), 16)));

		System.out.println(hammingDistance(new BigInteger(simHashStr.substring(2), 16), new BigInteger(simHashStr1.substring(2), 16), 50));
	}
}
