package com.inveno.core.process.model;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface IModelPolicy<T> {
	public T process(Context context) throws TException ;
}
