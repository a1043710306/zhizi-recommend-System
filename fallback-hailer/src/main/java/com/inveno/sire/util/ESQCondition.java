package com.inveno.sire.util;

import java.io.Serializable;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;

public class ESQCondition implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private QueryBuilder queryBuilder ;
	
	private QueryBuilder postFilter;
	
	private int size = 100;
	
	private SortBuilder sortBuilder;
	
	private List<SortBuilder> sortBuilders;

	public QueryBuilder getQueryBuilder() {
		return queryBuilder;
	}

	public void setQueryBuilder(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	public QueryBuilder getPostFilter() {
		return postFilter;
	}

	public void setPostFilter(QueryBuilder postFilter) {
		this.postFilter = postFilter;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public SortBuilder getSortBuilder() {
		return sortBuilder;
	}

	public void setSortBuilder(SortBuilder sortBuilder) {
		this.sortBuilder = sortBuilder;
	}
	
	public List<SortBuilder> getSortBuilders() {
		return sortBuilders;
	}

	public void setSortBuilders(List<SortBuilder> sortBuilders) {
		this.sortBuilders = sortBuilders;
	}

	@Override
	public String toString()
	{
		
		return (queryBuilder != null ? queryBuilder.toString():"")+":" 
				+ (postFilter != null ? postFilter.toString():"") + ":"
				  + (sortBuilder != null ? sortBuilder.toString():"");
	}
}
