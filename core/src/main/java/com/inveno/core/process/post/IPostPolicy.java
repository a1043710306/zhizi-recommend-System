package com.inveno.core.process.post;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface IPostPolicy<T> {
	public T process(Context context) throws TException ;
}
