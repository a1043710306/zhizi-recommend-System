package inveno.spider.reports;

import inveno.spider.common.utils.Config;

public final class Constants {

	public static Config config = null;

	static {
		config = new Config("sql_report_mysql.properties", "utf-8");
	}

	public static String get(String key) {
		return config.getProperty(key);
	}
	
	public static void main(String[] args) {
		System.out.println("-----------------------");
		System.out.println(Constants.get("report.listSourceInfoBySource"));
	}

}
