/**
 * 
 */
package inveno.spider.parser.model;

import java.io.Serializable;

/**
 * @author Administrator
 *
 */
public class ResultParam implements Serializable {
	
	private String name;
	
	private String value;
	
	/**
	 * 
	 */
	private String originalName;
	
	/**
	 * 路径
	 */
	private String path;
	
	/**
	 * array：通过path获取到的是一个数组，然后根据originalName获取内容，将数组内容组装成字符串
	 * map：从map中根据originalName获取值
	 */
	private String type;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	
	

}
