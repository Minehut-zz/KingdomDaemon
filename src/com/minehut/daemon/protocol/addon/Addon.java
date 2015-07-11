package com.minehut.daemon.protocol.addon;

import java.util.ArrayList;
import java.util.List;

import com.minehut.daemon.SampleKingdom;

public class Addon {

	public String name, author, systemName;
	
	private List<String> desc;
	
	private List<SampleKingdom> allowedBases;
	
	public Addon(String systemName, String name, String author, List<String> desc) {
		this.systemName = systemName;
		this.name = name;
		this.author = author;
		this.desc = desc;
	}
	
	public List<String> getAllowedSampleTypes() { //Used to check if the current sample base type is proper for the addon
		List<String> allowed = new ArrayList<String>();
		for (SampleKingdom sample : this.allowedBases) {
			allowed.add(sample.getType());
		}
		return allowed;
	}
	
	public void setAllowedBases(List<SampleKingdom> samples) {
		this.allowedBases = samples;
	}
	
	public List<SampleKingdom> getAllowedBases() {
		return this.allowedBases;
	}
	
	public List<String> getDesc() {
		return this.desc;
	}
	
}
