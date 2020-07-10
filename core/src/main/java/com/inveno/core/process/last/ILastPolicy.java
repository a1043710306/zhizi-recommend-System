package com.inveno.core.process.last;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;


public interface ILastPolicy<T> {
	public T process(Context context) throws TException ;
}
