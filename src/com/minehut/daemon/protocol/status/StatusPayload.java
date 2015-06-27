package com.minehut.daemon.protocol.status;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class StatusPayload extends Payload {

	private StatusType statusType;
	
	public StatusPayload(StatusType statusType) {
		super(PayloadType.STATUS);
		this.statusType = statusType;
	}

	public StatusType getStatusType() {
		return this.statusType;
	}
	
}
