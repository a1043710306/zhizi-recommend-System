package com.inveno.core.enumType;

import java.io.Serializable;

public enum MonitorType implements Serializable{
	
	REQUEST_COUNT("zhizi_core_request_count"),
	REQUEST_CACHE_COUNT("zhizi_core_request_cache_count"),
	REQUEST_L1CACHE_COUNT("zhizi_core_request_L1cache_count"),
	REQUEST_NOCACHE_COUNT("zhizi_core_request_nocache_count"),
	FALLBACK_REQUEST_COUNT("zhizi_core_fallback_request_count"),
	CACHE_RESPONSE_TIME("zhizi_core_cache_response_time"),
	NO_CACHE_RESPONSE_TIME("zhizi_core_no_cache_response_time"),
	RESPONSE_TIME_GT_200("zhizi_core_response_time_gt_200"),
	ACS_RESPONSE_TIME("zhizi_core_acs_response_time"),
	MIXED_ACS_RESPONSE_TIME("zhizi_core_mixed_acs_response_time"),
	ACS_REQUEST_COUNT("zhizi_core_acs_request_count"),
	GBDT_RESPONSE_TIME("zhizi_core_gbdt_response_time"),
	MIXED_GBDT_RESPONSE_TIME("zhizi_core_mixed_gbdt_response_time"),
	GMP_RESPONSE_TIME("zhizi_core_gmp_response_time"),
	GMP_INVOKE_COUNT("zhizi_core_gmp_invoke_count"),
	GMP_EMPTY_LIST_COUNT("zhizi_core_gmp_emptylist_count"),
	GMP_FAIL_COUNT("zhizi_gmp_fail_count"),
	EXP_INFO_CNT("zhizi_core_exfinfo_cnt"),
	EXP_INFO_RESPONSE_TIME("zhizi_core_exfinfo_response_time"),
	GBDT_INVOKE_COUNT("zhizi_core_gbdt_invoke_count"),
	GBDT_FAIL_COUNT("zhizi_core_gbdt_fail_count"),
	ALLINFO_READED("zhizi_core_allinfo_readed"),
	FALLBACK_REQUEST_COUNT_RECREADED("zhizi_core_fallback_request_count_recReaded"),
	SIMHASHMOVE_COUNT("zhizi_core_simhashMove_count"),
	RULE_RESPONSE_TIME("zhizi_core_rule_response_time"),
	SIMHASHMOVE_RESPONSE_TIME("zhizi_core_simhashMove_response_time"),
	PICSIMHASHMOVE_RESPONSE_TIME("zhizi_core_pic_simhashMove_response_time"),
	SOURCERANKRULE_RESPONSE_TIME("zhizi_core_sourcerankrule_response_time"),
	MIXEDSOURCERANK_RESPONSE_TIME("zhizi_core_mixedsourcerank_response_time"),
	VIDEOMIX_RESPONSE_TIME("zhizi_core_videomix_response_time"),
	MEMESMIX_RESPONSE_TIME("zhizi_core_memesmix_response_time"),
	MULTIMIX_RESPONSE_TIME("zhizi_core_multimix_response_time"),
	UFS_RESPONSE_TIME("zhizi_core_ufs_response_time"),
	UFS_INVOKE_COUNT("zhizi_core_ufs_invoke_count"),
	UFS_FAIL_COUNT("zhizi_core_ufs_fail_count"),
	MULTIMIX_MEMES_FAIL_COUNT("zhizi_core_multimix_memes_fail_count"),
	MULTIMIX_GIF_FAIL_COUNT("zhizi_core_multimix_gif_fail_count"),
	ACS_RESPONSE_FAIL_COUNT("zhizi_acs_fail_count"),
	RELATED_REQUEST_COUNT("zhizi_relatenews_request_count"),
	RELATED_EMPTY_PUBLISHER_COUNT("zhizi_relatenews_publisher_empty_count"),
	RELATED_EXCEPTION_COUNT("zhizi_relatenews_exception_count"),
	RELATED_RESPONSE_TIME_GT_200("zhizi_relatenews_response_time_gt_200"),	
	RELATED_GMP_INVOKE_COUNT("zhizi_relatenews_gmp_invoke_count"),
	RELATED_GMP_FAIL_COUNT("zhizi_relatenews_gmp_fail_count"),
	RELATED_REQUEST_CACHE_COUNT("zhizi_relatenews_request_cache_count"),
	RELATED_REQUEST_NOCACHE_COUNT("zhizi_relatenews_request_nocache_count"),
	RELATED_CACHE_RESPONSE_TIME("zhizi_relatenews_cache_response_time"),
	RELATED_NO_CACHE_RESPONSE_TIME("zhizi_relatenews_no_cache_response_time"),
	RELATED_GMP_EMPTY_LIST_COUNT("zhizi_relatenews_gmp_emptylist_count"),
	RELATED_GMP_RESPONSE_TIME("zhizi_relatenews_gmp_response_time"),
	INIT_INTEREST_EXPLORE_AND_BOOST_STRATEGY_RESPONSE_TIME("zhizi_core_init_interest_explore_and_boost_strategy_response_time"),
	;
	
	private String type;
	
	private MonitorType(String type)
	{
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
