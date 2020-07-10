package com.inveno.core.enumType;

import java.io.Serializable;

public enum InterfaceType implements Serializable {

	FORYOU("FORYOU"), QCN("qcn"), Q("q"), SCENARIO("SCENARIO"), QB("qb");

	private String type;

	private InterfaceType(String interFaceType) {
		this.type = interFaceType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
