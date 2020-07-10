package com.inveno.thrift;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface GmpKeyswordInterface<T> {
	public  T process(Context Context) throws TException ;
}
