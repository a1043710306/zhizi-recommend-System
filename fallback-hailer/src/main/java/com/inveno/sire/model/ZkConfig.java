package com.inveno.sire.model;

public class ZkConfig {

	private String host;
	
	private Integer port;
	
	private String path;
	
	private Integer sessionTimeoutMs;
	
	private Integer connectionTimeoutMs;
	
	private Integer baseSleepTimeMs;
	
	private Integer maxRetries;
	

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getSessionTimeoutMs() {
		return sessionTimeoutMs;
	}

	public void setSessionTimeoutMs(Integer sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}

	public Integer getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public Integer getBaseSleepTimeMs() {
		return baseSleepTimeMs;
	}

	public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
		this.baseSleepTimeMs = baseSleepTimeMs;
	}

	public Integer getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(Integer maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public String toString() {
		return "ZkConfig{" +
				"host='" + host + '\'' +
				", port=" + port +
				", path='" + path + '\'' +
				", sessionTimeoutMs=" + sessionTimeoutMs +
				", connectionTimeoutMs=" + connectionTimeoutMs +
				", baseSleepTimeMs=" + baseSleepTimeMs +
				", maxRetries=" + maxRetries +
				'}';
	}
}
