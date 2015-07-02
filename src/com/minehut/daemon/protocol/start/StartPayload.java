package com.minehut.daemon.protocol.start;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class StartPayload extends Payload {

	public Kingdom kingdom;
	
	public StartPayload() {
		super(PayloadType.START);
	}
	
	public StartPayload(Kingdom kingdom) {
		super(PayloadType.START);
		this.kingdom = kingdom;
	}
	
}
