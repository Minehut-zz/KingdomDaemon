package com.minehut.daemon.protocol.motd;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class ModifyMOTDPayload extends Payload {

	private String kingdomName, MOTD;
	
	public ModifyMOTDPayload(String kingdomName, String MOTD) {
		super(PayloadType.MODIFY_MOTD);
		this.kingdomName = kingdomName;
		this.MOTD = MOTD;
	}
	
	public String getKingdomName() {
		return this.kingdomName;
	}
	
	public String getMOTD() {
		return this.MOTD;
	}

}
