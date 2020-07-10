package inveno.spider.reports.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import tw.qing.sys.StringManager;

public class JdbcTemplateFactory {

	private static JdbcTemplate template = null;
	private static JdbcTemplate queryTemplate = null;
	private static Lock lock = new ReentrantLock();

	public static JdbcTemplate getQueryTemplate() {
		if (queryTemplate == null) {
			lock.lock();
			try {
				StringManager smgr = StringManager.getManager("system");
				String username = smgr.getString("DailySourceCrawlerStatistic.query.mysql.username");
				String password = smgr.getString("DailySourceCrawlerStatistic.query.mysql.passwd");
				String conenctUrl = smgr.getString("DailySourceCrawlerStatistic.query.mysql.host");
				DriverManagerDataSource dataSource = new DriverManagerDataSource();
				dataSource.setDriverClassName("com.mysql.jdbc.Driver");
				dataSource.setUrl(conenctUrl);
				dataSource.setUsername(username);
				dataSource.setPassword(password);
				queryTemplate = new JdbcTemplate(dataSource);
			} catch (Exception e) {
				System.err.println(e);
				System.exit(-1);
			} finally {
				lock.unlock();
			}
		}

		return queryTemplate;
	}

	public static JdbcTemplate geJdbcTemplate() {
		if (template == null) {
			lock.lock();
			try {

				StringManager smgr = StringManager.getManager("system");
				String username = smgr.getString("DailySourceCrawlerStatistic.mysql.username");
				String password = smgr.getString("DailySourceCrawlerStatistic.mysql.passwd");
				String conenctUrl = smgr.getString("DailySourceCrawlerStatistic.mysql.host");

				DriverManagerDataSource dataSource = new DriverManagerDataSource();
				dataSource.setDriverClassName("com.mysql.jdbc.Driver");
				dataSource.setUrl(conenctUrl);
				dataSource.setUsername(username);
				dataSource.setPassword(password);
				template = new JdbcTemplate(dataSource);
			} catch (Exception e) {
				System.err.println(e);
				System.exit(-1);
			} finally {
				lock.unlock();
			}
		}

		return template;
	}

}
