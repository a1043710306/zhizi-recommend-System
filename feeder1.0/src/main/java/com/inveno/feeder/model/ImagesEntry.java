package com.inveno.feeder.model;

public class ImagesEntry
{
	private String src;
	private String format;
	private Integer width;
	private Integer height;
	private String desc;
	private String simhash;

	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getSimhash() {
		return simhash;
	}
	public void setSimhash(String _simhash){
		simhash = _simhash;
	}
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("ImagesEntry [");
		sb.append("src=" + src);
		sb.append(", format=" + format);
		sb.append(", width=" + width);
		sb.append(", height=" + height);
		sb.append(", desc=" + desc);
		sb.append(", simhash=" + simhash);
		sb.append("]");
		return "ImagesEntry [src=" + src + ", format=" + format + ", width=" + width + ", height=" + height + ", desc="
				+ desc + "]";
	}
}
