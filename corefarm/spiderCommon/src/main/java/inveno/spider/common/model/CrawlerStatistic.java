package inveno.spider.common.model;

import java.io.Serializable;

public class CrawlerStatistic implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private java.util.Date timestamp; /* format:yyyy-MM-dd HH:mm:ss*/
    private String type;
    private String source;
    private String sourceType;
    /* 爬取量 */
    private int crawledAmount;
    /* 入库量 */
    private int warehousedAmount;
    /* 自动发佈量 */
    private int publishedAmount;
    /* 推送量 */
    private int deliveredAmount;
    /* 更新时间 */
    private java.util.Date updateTime;
    
    public CrawlerStatistic()
    {
    }
    
    public CrawlerStatistic(java.util.Date _timestamp, String _type, String _source, String _sourceType)
    {
        timestamp  = _timestamp;
        type       = _type;
        source     = _source;
        sourceType = _sourceType;
    }
    
    public java.util.Date getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(java.util.Date _timestamp)
    {
        timestamp = _timestamp;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String _type)
    {
        type = _type;
    }
    public String getSource()
    {
        return source;
    }
    public void setSource(String _source)
    {
        source = _source;
    }
    public String getSourceType()
    {
        return sourceType;
    }
    public void setSourceType(String _sourceType)
    {
        sourceType = _sourceType;
    }
    public int getCrawledAmount()
    {
        return crawledAmount;
    }
    public void setCrawledAmount(int _crawledAmount)
    {
        crawledAmount = _crawledAmount;
    }
    public int getWarehousedAmount()
    {
        return warehousedAmount;
    }
    public void setWarehousedAmount(int _warehousedAmount)
    {
        warehousedAmount = _warehousedAmount;
    }
    public int getPublishedAmount()
    {
        return publishedAmount;
    }
    public void setPublishedAmount(int _publishedAmount)
    {
        publishedAmount = _publishedAmount;
    }
    public int getDeliveredAmount()
    {
        return deliveredAmount;
    }
    public void setDeliveredAmount(int _deliveredAmount)
    {
        deliveredAmount = _deliveredAmount;
    }
    public java.util.Date getUpdateTime()
    {
        return updateTime;
    }
    public void setUpdateTime(java.util.Date _updateTime)
    {
        updateTime = _updateTime;
    }
}
