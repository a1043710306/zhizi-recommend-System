package com.inveno.feeder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;


public class ClientJDBCTemplateComment implements ClientDAO
{
	private static final Logger log = Logger.getLogger(ClientJDBCTemplateComment.class);

	@SuppressWarnings("unused")
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;

	private static ClientJDBCTemplateComment instance = null;

	public static synchronized ClientJDBCTemplateComment getInstance()
	{
		if (instance == null)
		{
			instance = new ClientJDBCTemplateComment();
		}
		return instance;
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplateObject = new JdbcTemplate(dataSource);
	}

	/**
	 * 获取文章4个表情数
	 * @param contentId
	 * @return
	 */
	public String getBallot(String contentId) {
		String ballotNum = "0";
		String SQL = "select sum(abs_count) as num from t_article_ballot_statistics where content_id = '"+contentId+"'";
		List<Map<String, Object>> dataList = jdbcTemplateObject.queryForList(SQL);
		log.info("getBallot SQL: " + SQL);
		if (!CollectionUtils.isEmpty(dataList))
		{
			for (Map<String, Object> mData : dataList)
			{
				Object num = mData.get("num");
				if(num == null){
					ballotNum = "0";
				}else{
					ballotNum = num.toString();
				}


			}
		}
		return ballotNum;
	}



}