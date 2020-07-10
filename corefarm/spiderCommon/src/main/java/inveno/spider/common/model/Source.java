package inveno.spider.common.model;

public class Source {
	
	private int id;
	
	private String sourceName;
	
	//是否可信任
	private int exemptReview;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public int getExemptReview() {
		return exemptReview;
	}

	public void setExemptReview(int exemptReview) {
		this.exemptReview = exemptReview;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
