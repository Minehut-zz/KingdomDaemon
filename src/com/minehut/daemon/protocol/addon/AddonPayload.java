package com.minehut.daemon.protocol.addon;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class AddonPayload extends Payload {

	public AddonPayloadType addonPayloadType;
	
	public Addon addon;
	
	public Kingdom kingdom;
	
	public AddonPayload(Kingdom kingdom, Addon addon, AddonPayloadType addonPayloadType) {
		super(PayloadType.ADDON);
		this.kingdom = kingdom;
		this.addon = addon;
		this.addonPayloadType = addonPayloadType;
	}
	
}
