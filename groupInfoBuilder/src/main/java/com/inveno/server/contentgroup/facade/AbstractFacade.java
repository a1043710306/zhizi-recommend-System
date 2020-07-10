package com.inveno.server.contentgroup.facade;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class AbstractFacade
{
	private static final Logger log = Logger.getLogger(AbstractFacade.class);

	protected JdbcTemplate dbmgr;
	protected DataSourceTransactionManager dbmgrTrans;
    protected DefaultTransactionDefinition def;
	protected ResourceBundle smgr;

	protected String getSql(String key)
	{
		return smgr.getString(this.getClass().getSimpleName()+"."+key);
	}
	public void setDataSource(javax.sql.DataSource ds)
	{
		log.info(">> setDataSource");
		dbmgr = new JdbcTemplate(ds);
		dbmgrTrans = new DataSourceTransactionManager(ds);
        def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		smgr = ResourceBundle.getBundle("sql_mysql");
		log.info("<< setDataSource dbmgr=" + dbmgr);
	}	
}