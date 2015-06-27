package com.minehut.daemon.protocol;

public class AuthPayload {

	private String key;
	
	public AuthPayload(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}

}
