package com.minehut.daemon.protocol.status;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class KingdomDataPayload extends Payload {

	public enum KingdomDataType {
		STARTUP, STARTED, CRASHED;
	}
	
	public KingdomDataType dataType;
	
	public Kingdom kingdom;
	
	public KingdomDataPayload(Kingdom kingdom, KingdomDataType dataType) {
		super(PayloadType.KINGDOM_DATA);
		this.dataType = dataType;
		this.kingdom = kingdom;
	}
	
}
