package com.minehut.daemon.protocol.status.out;

import java.util.List;

import com.minehut.daemon.Kingdom;

public class StatusPlayerKingdomsList {

	public boolean hasKingdom;
	
	public List<Kingdom> kingdoms;
	
	public StatusPlayerKingdomsList setHasKingdom(boolean bool) {
		this.hasKingdom = bool;
		return this;
	}
	
	public StatusPlayerKingdomsList setPlayerKingdomsList(List<Kingdom> list) {
		this.kingdoms = list;
		return this;
	}
	
}
