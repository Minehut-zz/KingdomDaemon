package com.minehut.daemon.tools.mc;

import java.util.UUID;

public class MCPlayer {

	public String playerUUID, playerName, rank;
	
	public MCPlayer() {
		this.playerUUID = "NULL";
		this.rank = "regular";
	}

	public MCPlayer(String name, UUID uuid) {
		this.playerName = name;
		this.playerUUID = uuid.toString();
		this.rank = "regular";
	}

	public MCPlayer(String name, UUID uuid, String rank) {
		this.playerName = name;
		this.playerUUID = uuid.toString();
		this.rank = rank;
	}
	
	public MCPlayer setPlayerName(String name) {
		this.playerName = name;
		return this;
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
