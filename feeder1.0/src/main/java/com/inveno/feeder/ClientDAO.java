package com.inveno.feeder;

import javax.sql.DataSource;


public interface ClientDAO 
{
	/** 
	 * This is the method to be used to initialize
	 * database resources.
	 */
	public void setDataSource(DataSource ds);


}