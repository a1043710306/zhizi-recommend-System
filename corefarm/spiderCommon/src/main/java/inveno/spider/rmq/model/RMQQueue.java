package inveno.spider.rmq.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class RMQQueue implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @SerializedName("name")
    private String name;

    @SerializedName("messages")
    private long count;

    private transient Date updateTime;

    public RMQQueue()
    {
        this.updateTime = new Date();
    }

    public RMQQueue(String name, long count)
    {
        this();
        this.name = name;
        this.count = count;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the count
     */
    public long getCount()
    {
        return count;
    }

    /**
     * @return the updateTime
     */
    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void decCount()
    {
        this.count -= 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(1000);
        sb.append("name:").append(this.name).append("\t");
        sb.append("message total:").append(this.count);
        return sb.toString();
    }
}
