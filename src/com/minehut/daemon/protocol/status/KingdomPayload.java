package com.minehut.daemon.protocol.status;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class KingdomPayload extends Payload {

	public String kingdomName;
	
	public KingdomPayload(String name) {
		super(PayloadType.KINGDOM);
		this.kingdomName = name;
	}
	
}
