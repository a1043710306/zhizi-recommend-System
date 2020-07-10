package inveno.spider.rmq.model;

import java.util.Comparator;

public class RMQQueueComparator implements Comparator<RMQQueue>
{

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(RMQQueue o1, RMQQueue o2)
    {
        if(o1.getCount()>o2.getCount())
        {
            return 1;
        }else if(o1.getCount()==o2.getCount())
        {
            return 0;
        }else{
            return -1;
        }
    }

}
