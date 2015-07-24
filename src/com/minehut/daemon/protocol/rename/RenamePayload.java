package com.minehut.daemon.protocol.rename;

import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.PayloadType;

public class RenamePayload extends Payload {

	private String oldName, newName;
	
	public RenamePayload(String oldName, String newName) {
		super(PayloadType.RENAME);
		this.oldName = oldName;
		this.newName = newName;
	}
	
	public String getNewName() {
		return this.newName;
	}
	
	public String getOldName() {
		return this.oldName;
	}

}
