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


public final class SessionOtherFactory
{
    private static final Logger LOG = LoggerFactory.make();
    private static final String RESOURCE = "mybatis-other-config.xml";
    private SqlSessionFactory sqlSessionFactory = null;
    private static SessionOtherFactory sessionFactory = null;

    private SessionOtherFactory()
    {
        try
        {
            Resources.setCharset(Charset.forName("UTF-8"));
            Reader reader = Resources.getResourceAsReader(RESOURCE);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
             
        } catch (IOException e)
        {
            LOG.error("#IOException happened in initialising the SessionFactory:"
                    + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static synchronized SessionOtherFactory getInstance()
    {
        if (null == sessionFactory)
        {
            sessionFactory = new SessionOtherFactory();
        }
        return sessionFactory;
    }

    /**
     * Get a new connection. <br/>
     * <br/>
     * <b>Attention</b>: The <i>autocommit</i> default as <i>true</i>.
     * 
     * @return Connection
     */
    public Connection getConnection()
    {
        return sqlSessionFactory.openSession(true).getConnection();
    }

    public SqlSessionFactory getSqlSessionFactory()
    {
        return sqlSessionFactory;
    }

    /**
     * 
     * @param sqlID
     * @param object
     * @return
     * int The number of rows affected by the insert.
     */
    public int insert(String sqlID, Object object)
    {
        SqlSession session = sqlSessionFactory.openSession();
        int affected = -1;
        try
        {
            affected = session.insert(sqlID, object);
            session.commit();
        } finally
        {
            session.close();
        }
        return affected;
    }

    /**
     * 
     * @param sqlID
     * @param object
     * @return
     * int The number of rows affected by the delete.
     */
    public int delete(String sqlID, Object object)
    {
        SqlSession session = sqlSessionFactory.openSession();
        int affected = -1;
        try
        {
            affected = session.delete(sqlID, object);
            session.commit();
        } finally
        {
            session.close();
        }
        return affected;
    }

    /**
     * 
     * @param sqlID
     * @param object
     * @return
     * int The number of rows affected by the update.
     */
    public int update(String sqlID, Object object)
    {
        SqlSession session = sqlSessionFactory.openSession();
        int affected = -1;
        try
        {
            affected = session.update(sqlID, object);

            session.commit();
        } finally
        {
            session.close();
        }
        return affected;
    }

    public <T> T selectOne(String sqlID, Object object)
    {
        SqlSession session = sqlSessionFactory.openSession();
        T tmp = null;
        try
        {
            tmp = session.selectOne(sqlID, object);
        } finally
        {
            session.close();
        }

        return tmp;
    }

    public <E> List<E> selectList(String sqlID, Object parameter)
    {
        SqlSession session = sqlSessionFactory.openSession();
        List<E> tmp = null;
        try
        {
            tmp = session.selectList(sqlID, parameter);

        } finally
        {
            session.close();
        }

        return tmp;
    }

    public <K, V> Map<K, V> selectMap(String sqlID, Object parameter, String key)
    {
        SqlSession session = sqlSessionFactory.openSession();
        Map<K, V> tmp = null;
        try
        {
            tmp = session.selectMap(sqlID, parameter, key);

        } finally
        {
            session.close();
        }

        return tmp;
    }
}
