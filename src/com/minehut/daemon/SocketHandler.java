package com.minehut.daemon;

import java.net.Socket;

public class SocketHandler {

	private Socket sock;
	private SocketFactory parentFactory;
	//private long time = System.currentTimeMillis();
	
	public SocketHandler(SocketFactory parent, Socket socket) {
		this.parentFactory = parent;
		this.sock = socket;
	}
	
	public Socket getSocket() {
		return this.sock;
	}
	
	public SocketFactory getParentFactory() {
		return this.parentFactory;
	}
	
}
