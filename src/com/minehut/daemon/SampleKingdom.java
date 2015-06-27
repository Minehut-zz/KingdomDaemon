package com.minehut.daemon;

public class SampleKingdom {

	protected String version, type, name;
	
	@Override
	public String toString() {
		return "[\"SampleKingdom\", version=\"" +version + "\", type=\"" + type + "\", name=\"" + name + "\"]";
	}
	
}
