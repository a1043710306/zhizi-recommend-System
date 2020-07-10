package com.inveno.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inveno.thrift.ResponParam;

public class RecommendInfoData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<ResponParam> listResponRaram = new ArrayList<ResponParam>();

	private Map<Integer, List<ResponParam>> mapContenttypeRecommendationList = new HashMap<Integer, List<ResponParam>>();

	private Map<Integer, List<ResponParam>> mapContenttypeInterestBoostList = new HashMap<Integer, List<ResponParam>>();

	private Map<Integer, List<ResponParam>> mapContenttypeInterestExploreList = new HashMap<Integer, List<ResponParam>>();

	/**
	 * Map<ContentType, Map<StrategyCode, DocumentList>>
	 */
	private Map<Integer, Map<String, List<ResponParam>>> contenttypeStrategyDocumentList = new HashMap<Integer, Map<String, List<ResponParam>>>();

	public List<ResponParam> getListResponRaram() {
		return listResponRaram;
	}

	public void setListResponRaram(List<ResponParam> listResponRaram) {
		this.listResponRaram = listResponRaram;
	}
	
	public Map<Integer, Map<String, List<ResponParam>>> getContenttypeStrategyDocumentList() {
		return contenttypeStrategyDocumentList;
	}
	
	public void setContenttypeStrategyDocumentList(Map<Integer, Map<String, List<ResponParam>>> contenttypeStrategyDocumentList) {
		this.contenttypeStrategyDocumentList = contenttypeStrategyDocumentList;
	}
	
	public Map<Integer, List<ResponParam>> getContenttypeRecommendationList() {
		return mapContenttypeRecommendationList;
	}

	public void setContenttypeRecommendationList(Map<Integer, List<ResponParam>> _mapContenttypeRecommendationList) {
		mapContenttypeRecommendationList = _mapContenttypeRecommendationList;
	}

	public Map<Integer, List<ResponParam>> getContenttypeInterestBoostList() {
		return mapContenttypeInterestBoostList;
	}

	public void setContenttypeInterestBoostList(Map<Integer, List<ResponParam>> _mapContenttypeInterestBoostList) {
		mapContenttypeInterestBoostList = _mapContenttypeInterestBoostList;
	}

	public Map<Integer, List<ResponParam>> getContenttypeInterestExploreList() {
		return mapContenttypeInterestExploreList;
	}

	public void setContenttypeInterestExploreList(Map<Integer, List<ResponParam>> _mapContenttypeInterestExploreList) {
		mapContenttypeInterestExploreList = _mapContenttypeInterestExploreList;
	}
}