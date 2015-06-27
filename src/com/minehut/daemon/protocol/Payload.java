package com.minehut.daemon.protocol;

public class Payload {

	protected PayloadType type;
	
	protected String payloadData;
	
	public Payload(PayloadType type) {
		this.type = type;
	}
	
	public PayloadType getPayloadType() {
		return this.type;
	}
	
	public void setPayloadData(String data) {
		this.payloadData = data;
	}
	
}
