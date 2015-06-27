package com.minehut.daemon.tools;

public enum LogType {

	WARN("WARN"), ERROR("ERROR"), INFO("INFO"), DEBUG("DEBUG");
	
	String type;
	
	LogType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
}
