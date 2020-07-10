package com.inveno.core.process.expinfo.face;

import java.util.List;
import java.util.Set;

import com.inveno.common.bean.Context;
import com.inveno.thrift.ResponParam;

public interface ExpInfo {
	
	public void getExpInfo(Context context, List<Double> indexListForNoCahce, List<ResponParam> resultList, List<Double> indexList, Set<ResponParam> resultSet,boolean useImpression);
	
}
