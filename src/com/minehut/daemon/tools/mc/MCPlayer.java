package com.minehut.daemon.tools.mc;

import java.util.UUID;

public class MCPlayer {

	public String playerUUID, rank;
	
	public MCPlayer() {
		this.playerUUID = "NULL";
		this.rank = "regular";
	}

	public MCPlayer( UUID uuid) {
		this.playerUUID = uuid.toString();
		this.rank = "regular";
	}

	public MCPlayer(UUID uuid, String rank) {
		this.playerUUID = uuid.toString();
		this.rank = rank;
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
