package inveno.spider.reports.dao;

import java.util.Date;

public class MediaCrawlerStatistic {
	private int _id;
	private String _media;
	private String _q_media_info;
	private int _num;
	private Date _date;
	public MediaCrawlerStatistic(){
		
	}
	
	public int getId() {
		return _id;
	}
	
	public void setId(int id){
		this._id = id;
	}
	
	public String getMedia(){
		return _media;
	}
	
	public void setMedia(String media){
		this._media = media;
	}
	
	public String getQMediaInfo(){
		return _q_media_info;
	}
	
	public void setQMediaInfo(String q_media_info){
		this._q_media_info = q_media_info;
	}
	
	public int getNum(){
		return _num;
	}
	
	public void setNum(int num){
		this._num = num;
	}
	
	public Date getDate(){
		return _date;
	}
	
	public void setDate(Date date){
		this._date = date;
	}
	
	
}
