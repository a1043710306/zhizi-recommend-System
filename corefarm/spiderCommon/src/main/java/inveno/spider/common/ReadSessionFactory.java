package inveno.spider.common;

import inveno.spider.common.utils.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

/**
 * @author
 * 
 */
public final class ReadSessionFactory {
	private static final Logger LOG = LoggerFactory.make();
	private static final String RESOURCE = "mybatis-read-config.xml";
	private SqlSessionFactory sqlSessionFactory = null;
	private static ReadSessionFactory sessionFactory = null;

	private ReadSessionFactory() {
		try {
			Resources.setCharset(Charset.forName("UTF-8"));
			Reader reader = Resources.getResourceAsReader(RESOURCE);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			LOG.error("#IOException happened in initialising the SessionFactory:" + e.getMessage());
			throw new ExceptionInInitializerError(e);
		}
	}

	public static synchronized ReadSessionFactory getInstance() {
		if (null == sessionFactory) {
			sessionFactory = new ReadSessionFactory();
		}
		return sessionFactory;
	}

	public Connection getConnection() {
		return sqlSessionFactory.openSession(true).getConnection();
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public <T> T selectOne(String sqlID, Object object) {
		SqlSession session = sqlSessionFactory.openSession();
		T tmp = null;
		try {
			tmp = session.selectOne(sqlID, object);
		} finally {
			session.close();
		}

		return tmp;
	}

	public <E> List<E> selectList(String sqlID, Object parameter) {
		SqlSession session = sqlSessionFactory.openSession();
		List<E> tmp = null;
		try {
			tmp = session.selectList(sqlID, parameter);
		} finally {
			session.close();
		}

		return tmp;
	}

	public <K, V> Map<K, V> selectMap(String sqlID, Object parameter, String key) {
		SqlSession session = sqlSessionFactory.openSession();
		Map<K, V> tmp = null;
		try {
			tmp = session.selectMap(sqlID, parameter, key);
		} finally {
			session.close();
		}

		return tmp;
	}
}
