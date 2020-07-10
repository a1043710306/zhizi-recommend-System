/**
 * 
 */
package inveno.spider.parser.model;

import java.io.Serializable;

/**
 * @author Administrator
 *
 */
public class ParamConfig implements Serializable {
	
	private String name;
	
	private String value;
	
	private String referEntity;
	
	private String referField;
	
	public void setParamConfig(String name, String value, String referEntity, String referField) {
		this.name = name;
		this.value = value;
		this.referEntity = referEntity;
		this.referField = referField;
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

	public String getReferEntity() {
		return referEntity;
	}

	public void setReferEntity(String referEntity) {
		this.referEntity = referEntity;
	}

	public String getReferField() {
		return referField;
	}

	public void setReferField(String referField) {
		this.referField = referField;
	}

}
