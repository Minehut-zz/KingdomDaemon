package com.minehut.daemon.protocol;

public enum PayloadType {

	CREATE("CREATE"), RESET("RESET"), CMD("CMD"), 
	STATUS("STATUS"), START("START"), STOP("STOP"),
	SAMPLE_KINGDOMS_LIST("SAMPLE_KINGDOMS_LIST"),
	PLAYER_KINGDOMS_LIST("PLAYER_KINGDOMS_LIST"),
	KINGDOM_DATA("KINGDOM_DATA");
	
	private String type;
	
	private PayloadType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
}
