package inveno.spider.common.model;

import java.io.Serializable;

/**
 * 关键词 Class Name: Content.java Description:
 * 
 * @author liyuanyi DateTime 2016年3月20日 下午4:34:25
 * @company inveno
 * @version 1.0
 */
public class KeyWord implements Serializable {

	/**
	 * Description:
	 * 
	 * @author liyuanyi DateTime 2016年3月20日
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String str;

	public KeyWord() {
	}

	public KeyWord(String str) {
		this.str = str;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}