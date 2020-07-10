package inveno.spider.common.enumType;

public enum SequenceEnum {

	INFO("I");

	private String value;

	private SequenceEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
