package com.inveno.thrift;


import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface PrimarySelectionInterface<T> {
	public T process(Context Context) throws TException ;	
	public T relatedNewsList(Context Context) throws TException ;	
}
