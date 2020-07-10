package inveno.spider.reports.util;

import java.util.Calendar;

public class GenerateTableName {

	public static String generatorTableName(boolean before) {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int mounth = c.get(Calendar.MONTH);
		String tableName = null;
		if(before) {
			c.add(Calendar.MONTH, -1);
			year = c.get(Calendar.YEAR);
			mounth = c.get(Calendar.MONTH);
		}
		if(mounth + 1 < 10) {
			tableName = "t_content_" + year + "_0" + (mounth + 1);
		} else {
			tableName = "t_content_" + year + "_" + (mounth + 1);
		}
		return tableName;
	}
	
}
