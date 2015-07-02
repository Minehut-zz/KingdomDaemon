package com.minehut.daemon.protocol.status;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;
import com.minehut.daemon.tools.mc.MCPlayer;

public class PlayerKingdomsListPayload extends Payload {

	public MCPlayer player;
	
	public PlayerKingdomsListPayload(MCPlayer player) {
		super(PayloadType.PLAYER_KINGDOMS_LIST);
		this.player = player;
	}
	
}
