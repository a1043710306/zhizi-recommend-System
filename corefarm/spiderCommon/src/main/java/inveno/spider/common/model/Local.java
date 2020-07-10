package inveno.spider.common.model;

import java.io.Serializable;

/**
 * 地域 Class Name: Content.java Description:
 * 
 * @author liyuanyi DateTime 2016年3月20日 下午4:35:14
 * @company inveno
 * @version 1.0
 */
public class Local implements Serializable {

	/**
	 * Description:
	 * 
	 * @author liyuanyi DateTime 2016年3月20日
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 国家
	 */
	private String country;

	/**
	 * 省份
	 */
	private String province;

	/**
	 * 城市
	 */
	private String city;

	/**
	 * 相关度
	 */
	private double confidence;

	public Local() {
	}

	public Local(String country, String province, String city, double confidence) {
		this.country = country;
		this.province = province;
		this.city = city;
		this.confidence = confidence;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
}
