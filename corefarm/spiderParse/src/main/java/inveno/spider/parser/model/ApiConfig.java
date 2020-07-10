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
public class ApiConfig implements Serializable {
	
	private HttpMethodType httpMethodType;
	
	private PageType pageType;
	
	private CharacterType characterType;
	
	private String regUrl;
	
	private String url;
	
	private List<ParamConfig> paramConfigs = new ArrayList<ParamConfig>();
	
	private List<ResultConfig> resultConfigs = new ArrayList<ResultConfig>();
	
	public void setApiConfig(String httpMethodType, String pageType, String characterType) {
		this.httpMethodType = HttpMethodType.valueOf(httpMethodType);
		this.pageType = PageType.valueOf(pageType);
		this.characterType = CharacterType.valueOf(characterType);
	}
	
	public void addParamConfig(ParamConfig paramConfig) {
		this.paramConfigs.add(paramConfig);
	}
	
	public void addResultConfig(ResultConfig resultConfig) {
		this.resultConfigs.add(resultConfig);
	}

	public CharacterType getCharacterType() {
		return characterType;
	}

	public void setCharacterType(CharacterType characterType) {
		this.characterType = characterType;
	}

	public HttpMethodType getHttpMethodType() {
		return httpMethodType;
	}

	public void setHttpMethodType(HttpMethodType httpMethodType) {
		this.httpMethodType = httpMethodType;
	}

	public PageType getPageType() {
		return pageType;
	}

	public void setPageType(PageType pageType) {
		this.pageType = pageType;
	}

	public String getRegUrl() {
		return regUrl;
	}

	public void setRegUrl(String regUrl) {
		this.regUrl = regUrl;
	}

	public List<ParamConfig> getParamConfigs() {
		return paramConfigs;
	}

	public void setParamConfigs(List<ParamConfig> paramConfigs) {
		this.paramConfigs = paramConfigs;
	}

	public List<ResultConfig> getResultConfigs() {
		return resultConfigs;
	}

	public void setResultConfigs(List<ResultConfig> resultConfigs) {
		this.resultConfigs = resultConfigs;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
