package com.inveno.core.process.post.process;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.inveno.common.bean.Context;
import com.inveno.core.process.post.IPostPolicy;
import com.inveno.thrift.ResponParam;

@Component("imgCntControl")
public class ImgCntControl  implements IPostPolicy<List<ResponParam>>{
	
	private Log logger = LogFactory.getLog(ImgCntControl.class);
	
	private Log toErrFile = LogFactory.getLog("toErrFile");
//	private Logger toErrFile = Logger.getLogger("toErrFile");
 	
	@Override
	public List<ResponParam> process(Context context) {
		
		long cur = System.currentTimeMillis();
		String uid = context.getUid();
		String app = context.getApp();
		String abtestVersion  = context.getAbtestVersion();
		
		if(logger.isDebugEnabled()){
			logger.debug(" uid: "+ uid  + " ,app "+  app +" begin ImgCntControl , abtestVersion is "+  abtestVersion  +"  , time is " +(System.currentTimeMillis()) +",size is " + context.getResponseParamList().size() +" ,and cur = " + System.currentTimeMillis()  );
		}
		
		List<ResponParam> list =  context.getResponseParamList();
		try {
			 
			Iterator<ResponParam> it = list.iterator();
			while (it.hasNext()) {
				ResponParam re = it.next();
				if( re.getImgCnt() <=0 ){
					it.remove();
				}
			}
			
		} catch (Exception e) {
			toErrFile.error("uid: "+ uid  + " ,app "+  app +" ImgCntControl error ", e);
 		}
		 
		long end  = System.currentTimeMillis();
		logger.info(" uid :"+ uid  + " ,app "+  app +" end ImgCntControl ,time is " +(end-cur) +",size is " + context.getResponseParamList().size()  +" ,and cur = " + System.currentTimeMillis() );
		context.setResponseParamList(list);
		//System.out.println("===========" + context.getResponseParamList() );
		 
		return null;
	}
	
	
}
