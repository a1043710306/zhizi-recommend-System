package inveno.spider.common.model;

import java.io.Serializable;

public class CategoryVersionMapping implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int version;
	private int categoryId;
	private int mappingVersion;
	private int mappingCategoryId;

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int _version)
	{
		this.version = _version;
	}

	public int getCategoryId()
	{
		return categoryId;
	}

	public void setCategoryId(int _categoryId)
	{
		this.categoryId = _categoryId;
	}

	public int getMappingVersion()
	{
		return mappingVersion;
	}

	public void setMappingVersion(int _mappingVersion)
	{
		this.mappingVersion = _mappingVersion;
	}

	public int getMappingCategoryId()
	{
		return mappingCategoryId;
	}

	public void setMappingCategoryId(int _mappingCategoryId)
	{
		this.mappingCategoryId = _mappingCategoryId;
	}
}
