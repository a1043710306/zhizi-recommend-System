package inveno.spider.rmq.model;

public class RMQQueueFilter implements Filter<RMQQueue, String>
{

    /* (non-Javadoc)
     * @see wisers.crawler.rmq.entity.Filter#isMatched(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isMatched(RMQQueue object, String prefixText)
    {
        if(null==object)
        {
            return false;
        }
        
        return object.getName().startsWith(prefixText);
    }

    /* (non-Javadoc)
     * @see wisers.crawler.rmq.entity.Filter#isMatched(java.lang.Object, E[])
     */
    @Override
    public boolean isMatched(RMQQueue object, String... prefixText)
    {
        if(null==object)
        {
            return false;
        }        
        
        if(null==prefixText||prefixText.length==0)
        {
            return true;
        }
        for(String text:prefixText)
        {
            if(object.getName().startsWith(text))
            {
                return true;
            }
        }
        
        return false;
    }

}
