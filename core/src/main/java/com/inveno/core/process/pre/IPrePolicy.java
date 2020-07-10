package com.inveno.core.process.pre;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface IPrePolicy<T> {
	public  T process(Context context) throws TException ;
}
