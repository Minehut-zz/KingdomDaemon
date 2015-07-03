package com.minehut.daemon.protocol.create;

import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;
import com.minehut.daemon.tools.mc.MCPlayer;

public class CreatePayload extends Payload {

	public MCPlayer owner;
	
	public SampleKingdom sample;
	
	public String name;
	
	public CreatePayload(MCPlayer player, SampleKingdom sample, String name) {
		super(PayloadType.CREATE);
		this.owner = player;
		this.sample = sample;
		this.name = name;
	}
	
}
