package inveno.spider.common.model;

public class Category {
	
	private int id;
	
	private String categoryName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName)
	{
		this.categoryName = categoryName;
	}

	public boolean equals(Category _category)
	{
		if (_category != null)
		{
			return (_category.getId() == id);
		}
		return false;
	}
}
