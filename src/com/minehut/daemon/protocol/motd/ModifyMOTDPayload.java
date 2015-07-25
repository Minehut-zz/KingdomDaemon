package com.minehut.daemon.protocol.motd;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class ModifyMOTDPayload extends Payload {

	private Kingdom kingdom;
	private String MOTD;
	
	public ModifyMOTDPayload(Kingdom kingdom, String MOTD) {
		super(PayloadType.MODIFY_MOTD);
		this.kingdom = kingdom;
		this.MOTD = MOTD;
	}
	
	public Kingdom getKingdom() {
		return this.kingdom;
	}
	
	public String getMOTD() {
		return this.MOTD;
	}

}
