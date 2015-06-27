package com.minehut.daemon.tools.mc;

public class MCPlayer {

	public String playerUUID, rank;
	
	public MCPlayer() {
		this.playerUUID = "NULL";
		this.rank = "regular";
	}
	
	public MCPlayer setPlayerRank(String rank) {
		this.rank = rank;
		return this;
	}
	
	public MCPlayer setPlayerUUID(String uuid) {
		this.playerUUID = uuid;
		return this;
	}
	
}
