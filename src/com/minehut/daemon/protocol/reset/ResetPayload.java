package com.minehut.daemon.protocol.reset;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class ResetPayload extends Payload {

	public Kingdom kingdom;
	
	public ResetPayload(Kingdom kingdom) {
		super(PayloadType.RESET);
		this.kingdom = kingdom;
	}
	
	public ResetPayload() {
		super(PayloadType.RESET);
	}
	
}
