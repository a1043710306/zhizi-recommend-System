package inveno.spider.parser.model;

import java.io.Serializable;

public class ListingPage implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String mAuthor;
    private String mSection;
    private String mUrl;
    private int id;
    private int level;//the same as info_level of s_rss
    
    private String typeCode;
    /**
     * the same as rss_tags of s_rss
     */
    private String tags;
    private String location;
    private int isOpenComment;
    
    //1：资讯 2:视频 3 :音频 4:小说  5 :电商
    private int dataType;
    
    //0-普通资讯,1-头条,2-精编,3-无标识头条。4-热门
    private int infoType;
    
    //分了名字
    private String categoryName;
    
    //是否要判断分类
    private int checkCategoryFlag; 

    public ListingPage()
    {
    }

    public ListingPage(int id, String author, String section, String url)
    {
        this.id = id;
        mAuthor = author;
        mSection = section;
        mUrl = url;
    }

    /**
     * Use this constructor method in the test only.
     * 
     * @param author
     * @param section
     * @param url
     */
    @Deprecated
    public ListingPage(String author, String section, String url)
    {
        mAuthor = author;
        mSection = section;
        mUrl = url;
    }

    public String getAuthor()
    {
        return mAuthor;
    }

    public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public int getId()
    {
        return id;
    }

    public String getSection()
    {
        return mSection;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public void setAuthor(String mAuthor)
    {
        this.mAuthor = mAuthor;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setSection(String mSection)
    {
        this.mSection = mSection;
    }

    public void setUrl(String mUrl)
    {
        this.mUrl = mUrl;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public int getIsOpenComment()
    {
        return isOpenComment;
    }

    public void setIsOpenComment(int isOpenComment)
    {
        this.isOpenComment = isOpenComment;
    }

    public int getDataType()
    {
        return dataType;
    }

    public void setDataType(int dataType)
    {
        this.dataType = dataType;
    }

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public int getCheckCategoryFlag() {
		return checkCategoryFlag;
	}

	public void setCheckCategoryFlag(int checkCategoryFlag) {
		this.checkCategoryFlag = checkCategoryFlag;
	}
}
