package inveno.spider.common.model;

import java.io.Serializable;
import java.util.Date;

public class SourceFeedFirmAppMapping implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int id;
	private int sourceFeedsId;
	private String sourceType;
	private String source;
	private String sourceFeeds;
	private String channel;
	private String contentType;
	private String firmApp;
	
	public int getId()
	{
		return id;
	}

	public void setId(int _id)
	{
		id = _id;
	}

	public int getSourceFeedsId()
	{
		return id;
	}

	public void setSourceFeedsId(int _sourceFeedsId)
	{
		sourceFeedsId = _sourceFeedsId;
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

	public String getSourceFeeds()
	{
		return sourceFeeds;
	}

	public void setSourceFeeds(String _sourceFeeds)
	{
		sourceFeeds = _sourceFeeds;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String _channel)
	{
		channel = _channel;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String _contentType)
	{
		contentType = _contentType;
	}

	public String getFirmApp()
	{
		return firmApp;
	}

	public void setFirmApp(String _firmApp)
	{
		firmApp = _firmApp;
	}
}
