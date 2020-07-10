package com.inveno.core.process.post.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.core.Constants;
import com.inveno.common.util.ContextUtils;
import com.inveno.thrift.ResponParam;

@Component("sourceControl")
public class SourceControl implements IPostPolicy<List<ResponParam>>{
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Override
	public List<ResponParam> process(Context context) throws TException {
		
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtest = context.getAbtestVersion();

		if(logger.isDebugEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " begin sourceControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis());
		}
		
		boolean bLockscreen = ContextUtils.isLockscreen(context);
		if (!bLockscreen) {
			return null;
		}
		
		int sourceControlwindowSize = NumberUtils.toInt(context.getAbtestConfiguration(Constants.CONFIG_SEGMENT_CORE, "sourceControlwindowSize"), 8);

		if( logger.isTraceEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " begin SourceControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis() +" , before list " + context.getResponseParamList());
		}
		
		List<ResponParam> reList = null;
		try {
			reList = reRank(context.getResponseParamList(),sourceControlwindowSize,context);
			context.setResponseParamList(reList);
		} catch (Exception e) {
			logger.error(" uid :"+ uid  + " ,app "+  app +"  SourceControl Exception,time is,and cur = " + System.currentTimeMillis() +e.getCause(),e );
		}
 		
		if( logger.isTraceEnabled()){
			logger.debug(" uid: " + uid + " ,app " + app + " end SourceControl , abtestVersion is " + abtest
					+ "  , time is " + (System.currentTimeMillis()) + ",size is " + context.getResponseParamList().size()
					+ " ,and cur = " + System.currentTimeMillis() +" , after list " + reList);
		}
		
		long endTime  = System.currentTimeMillis();
		logger.info(" uid :"+ uid  + " ,app "+  app +" end SourceControl ,time is " +(endTime-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		return null;
	
	}
	
	public static List<ResponParam> reRank(List <ResponParam> inlist, int windowSize,Context context){
        
        ArrayList<ResponParam> list = (ArrayList<ResponParam>)inlist;
        @SuppressWarnings("unchecked")
		ArrayList<ResponParam> result = (ArrayList<ResponParam>) list.clone();
        
        int len = result.size();
        HashSet<String> tagsInWindow = new HashSet<String>();
        
        for(int i = 0; i < len; i++){
        	String source =  result.get(i).getSource();
            
        	if( i >= windowSize){
        		tagsInWindow.remove(result.get(i - windowSize).getSource());
        	}
        	
            if(StringUtils.isEmpty(source) || "-1".equals(source) || source.equalsIgnoreCase("null")){
        		continue;
        	}
            
            boolean isContains = false;
            for(int j = i; j < len; j++){
            	String sourceJ =  result.get(j).getSource();
				isContains = tagsInWindow.contains(sourceJ);
                if( !isContains ){
                    if(i != j){ // 需要移动
                    	ResponParam backwardItem = result.get(j); // 前移的item
                        result.remove(backwardItem);
                        result.add(i, backwardItem);
                    }
                    break;
                }
            }
            if(isContains){
            	// report cnt++
            }
            
            if( !StringUtils.isEmpty(source) &&  !"-1".equals(source)  && !source.equalsIgnoreCase("null") ){
            	tagsInWindow.add(result.get(i).getSource());
        	}
        }
        
        return result;
    }
}
