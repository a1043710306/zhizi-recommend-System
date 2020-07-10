package inveno.spider.common.model;

import java.io.Serializable;

import java.util.*;

public class UserComment implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String id;
	private String contentId;
	private String contentType;
	private String contentMD5;
	private String content;
	private int    likeCount;
	private int    dislikeCount;
	private int    userId;
	private String userName;
	private String userPhoto;
	private int    replyUserId;
	private String replyUserName;
	private String replyUserPhoto;
	private String parentId;
	private long   insertTime;
	private long   updateTime;
	private int    status;

	public UserComment()
	{
	}
	
	public String getId()
	{
		return id;
	}
	public void setId(String _id)
	{
		id =_id;
	}
	public String getContentId()
	{
		return contentId;
	}
	public void setContentId(String _contentId)
	{
		contentId = _contentId;
	}
	public String getContentType()
	{
		return contentType;
	}
	public void setContentType(String _contentType)
	{
		contentType = _contentType;
	}
	public String getContentMD5()
	{
		return contentMD5;
	}
	public void setContentMD5(String _contentMD5)
	{
		contentMD5 = _contentMD5;
	}
	public String getContent()
	{
		return content;
	}
	public void setContent(String _content)
	{
		content = _content;
	}
	public int getLikeCount()
	{
		return likeCount;
	}
	public void setLikeCount(int _likeCount)
	{
		likeCount = _likeCount;
	}
	public void setDislikeCount(int _dislikeCount)
	{
		dislikeCount = _dislikeCount;
	}
	public int getUserId()
	{
		return userId;
	}
	public void setUserId(int _userId)
	{
		userId = _userId;
	}
	public String getUserName()
	{
		return userName;
	}
	public void setUserName(String _userName)
	{
		userName = _userName;
	}
	public String getUserPhoto()
	{
		return userPhoto;
	}
	public void setUserPhoto(String _userPhoto)
	{
		userPhoto = _userPhoto;
	}
	public int getReplyUserId()
	{
		return replyUserId;
	}
	public void setReplyUserId(int _replyUserId)
	{
		replyUserId = _replyUserId;
	}
	public String getReplyUserName()
	{
		return replyUserName;
	}
	public void setReplyUserName(String _replyUserName)
	{
		replyUserName = _replyUserName;
	}
	public String getReplyUserPhoto()
	{
		return replyUserPhoto;
	}
	public void setReplyUserPhoto(String _replyUserPhoto)
	{
		replyUserPhoto = _replyUserPhoto;
	}
	public String getParentId()
	{
		return parentId;
	}
	public void setParentId(String _parentId)
	{
		parentId = _parentId;
	}
	public long getInsertTime()
	{
		return insertTime;
	}
	public void setInsertTime(long _insertTime)
	{
		insertTime = _insertTime;
	}
	public long getUpdateTime()
	{
		return updateTime;
	}
	public void setUpdateTime(long _updateTime)
	{
		updateTime = _updateTime;
	}
	public int getStatus()
	{
		return status;
	}
	public void setStatus(int _status)
	{
		status = _status;
	}
}
