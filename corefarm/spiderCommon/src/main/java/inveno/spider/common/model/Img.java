package inveno.spider.common.model;

import java.io.Serializable;

/**
 * 
 * 图片的JSON字段 Class Name: Img.java Description:
 * 
 * @author liyuanyi DateTime 2016年3月18日 下午5:32:46
 * @company inveno
 * @version 1.0
 */
public class Img implements Serializable {

	/**
	 * Description:
	 * 
	 * @author liyuanyi DateTime 2016年3月20日
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 图片url
	 */
	private String src;

	private int width;

	private int height;

	/**
	 * 图片的描述
	 */
	private String desc;
	/**
	 * 图片的格式: jpg, gif, png..etc
	 */
	private String format;

	public Img() {
	}

	public Img(String src, String desc, int width, int height) {
		this.src = src;
		this.width = width;
		this.height = height;
		this.desc = desc;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String normalizeImageLink() {
		StringBuffer sb = new StringBuffer();
		sb.append(src);
		if (src.indexOf("?") < 0)
			sb.append("?");
		else
			sb.append("&");
		sb.append("size=" + width + "*" + height);
		sb.append("&fmt=." + ((format == null) ? "jpg" : format));
		return sb.toString();
	}

	@Override
	public String toString() {
		return normalizeImageLink();
	}

}
