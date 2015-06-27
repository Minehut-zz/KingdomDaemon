package com.minehut.daemon.protocol.status.out;

import java.util.List;

import com.minehut.daemon.SampleKingdom;

public class StatusSampleList {

	public List<SampleKingdom> sampleList;
	
	public StatusSampleList setSampleList(List<SampleKingdom> list) {
		this.sampleList = list;
		return this;
	}
	
}
