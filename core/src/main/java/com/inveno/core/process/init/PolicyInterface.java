package com.inveno.core.process.init;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface PolicyInterface<T> {
	public  T process(Context context) throws TException ;
}
