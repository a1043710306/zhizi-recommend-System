package com.inveno.thrift;

import java.util.List;

import org.apache.thrift.TException;

import com.inveno.common.bean.Context;
import com.inveno.common.bean.ReRankData;


public interface PrimarySelectionFilteredInterface<T> {
	public T process(Context Context) throws TException ;
}
