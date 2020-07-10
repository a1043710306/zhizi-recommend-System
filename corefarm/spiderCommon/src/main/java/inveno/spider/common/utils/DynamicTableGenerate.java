package inveno.spider.common.utils;

import java.util.Calendar;
import java.util.Date;

public class DynamicTableGenerate {

	/**
	 * @param time 
	 * @param before  是否生成上一个月的表明
	 * @return
	 */
	public static String generateTableName(Date time, boolean before, String tableNamePrefix) {
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		int year = c.get(Calendar.YEAR);
		int mounth = c.get(Calendar.MONTH);
		String tableName = null;
		if(before) {
			c.add(Calendar.MONTH, -1);
			year = c.get(Calendar.YEAR);
			mounth = c.get(Calendar.MONTH);
		}
		if(mounth + 1 < 10) {
			tableName = tableNamePrefix + "_" + year + "_0" + (mounth + 1);
		} else {
			tableName = tableNamePrefix + "_" + year + "_" + (mounth + 1);
		}
		return tableName;
	}
}
