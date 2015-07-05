package com.minehut.daemon.protocol.status;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class KingdomDataPayload extends Payload {

	public enum KingdomDataType {
		STARTUP, STARTED, CRASHED;
	}
	
	public KingdomDataType type;
	
	public Kingdom kingdom;
	
	public KingdomDataPayload(Kingdom kingdom, KingdomDataType type) {
		super(PayloadType.KINGDOM_DATA);
		this.type = type;
	}
	
}
