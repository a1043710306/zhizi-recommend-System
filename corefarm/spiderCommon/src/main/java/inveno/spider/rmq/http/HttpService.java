package inveno.spider.rmq.http;


/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public interface HttpService
{
    /**
     * 
     * @param user
     * @param pass
     * @param apiUrl
     *        for example: "/api/queues/",see as {@link inveno.spider.rmq.model.RMQAPI}
     * @return
     * @throws AuthorizationException
     */
  String get(String user,String pass,String apiUrl) throws AuthorizationException;
}
