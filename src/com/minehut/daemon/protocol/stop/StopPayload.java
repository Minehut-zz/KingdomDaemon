package com.minehut.daemon.protocol.stop;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class StopPayload extends Payload {

	public String kingdomName;
	
	public StopPayload(String kingdomName) {
		super(PayloadType.STOP);
		this.kingdomName = kingdomName;
	}
	
}
