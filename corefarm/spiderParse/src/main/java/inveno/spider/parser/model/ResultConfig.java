/**
 * 
 */
package inveno.spider.parser.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class ResultConfig implements Serializable {
	
	private ResponseType responseType;
	
	private String entity;
	
	private boolean multi = false;
	
	private String basePath;
	
	private List<ResultParam> resultParams = new ArrayList<ResultParam>();
	
	public void setResultConfig(String responseType, String entity, String multi, String basePath) {
		this.responseType = ResponseType.valueOf(responseType);
		this.entity = entity;
		this.multi = Boolean.valueOf(multi);
		this.basePath = basePath;
	}
	
	public void addResultParam(ResultParam resultParam) {
		this.resultParams.add(resultParam);
	}
	
	public ResponseType getResponseType() {
		return responseType;
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public List<ResultParam> getResultParams() {
		return resultParams;
	}

	public void setResultParams(List<ResultParam> resultParams) {
		this.resultParams = resultParams;
	}

}
