package com.minehut.daemon.protocol;

public enum PayloadType {

	CREATE("CREATE"), RESET("RESET"), CMD("CMD"), STATUS("STATUS");
	
	private String type;
	
	private PayloadType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
}
