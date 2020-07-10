package inveno.spider.common.model;

import java.io.Serializable;

public class PublisherRate implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int id;
	
	private String language;

	private String publisher;

	private int rate;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String _language)
	{
		language = _language;
	}

	public String getPublisher()
	{
		return publisher;
	}

	public void setPublisher(String _publisher)
	{
		publisher = _publisher;
	}

	public int getRate()
	{
		return rate;
	}

	public void setRate(int _rate)
	{
		rate = _rate;
	}
}
