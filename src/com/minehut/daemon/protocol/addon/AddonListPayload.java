package com.minehut.daemon.protocol.addon;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class AddonListPayload extends Payload {

	public AddonListPayload() {
		super(PayloadType.ADDON_LIST);
	}
	
}
