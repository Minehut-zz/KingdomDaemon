package com.minehut.daemon;

public class SampleKingdom {

	protected String version, type, name;
	
	public SampleKingdom setName(String s) {
		this.name = s;
		return this;
	}
	
	public SampleKingdom setType(String s) {
		this.type = s;
		return this;
	}
	
	public SampleKingdom setVersion(String s) {
		this.version = s;
		return this;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	@Override
	public String toString() {
		return "[\"SampleKingdom\", version=\"" +version + "\", type=\"" + type + "\", name=\"" + name + "\"]";
	}
	
}
