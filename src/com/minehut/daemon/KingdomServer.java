package com.minehut.daemon;

import java.io.IOException;

import com.minehut.daemon.tools.FileUtil;

public class KingdomServer extends Thread {
	
	private int id = -1, port = -1;
	
	private Kingdom kingdom;
	
	public KingdomServer(Kingdom kingdom, int id) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;
		System.out.println("KingdomServer Thread created, ready to start with the port '" + this.port + "'");
	}
	
	public int getID() {
		return this.id;
	}
	
	public int getPort() {
		return port;
	}
	
	@Override
	public void run() {
		if (id==-1) 
			return;
		
		try {
			FileUtil.editServerProperties(this.kingdom, this.port);
			
			new ProcessBuilder("/bin/bash", "-c", "screen -dmS kingdom" + this.id).start().waitFor();
			
			String cdCMD = "cd /home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id;
			new ProcessBuilder("/bin/bash", "-c", "screen -S kingdom" + this.id +" -p 0 -X stuff '" + cdCMD + "'^M").start().waitFor();
			
			String javaCMD = "java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui";
			new ProcessBuilder("/bin/bash", "-c", "screen -S kingdom" + this.id + " -p 0 -X stuff '" + javaCMD + "'^M").start().waitFor();

			System.out.println("Kingdom started: kingdom" + this.id + " port: " + this.port);
			//TODO: upload to mongo about port?
			//TODO: open input stream to ./logs/latest.log for parsing server logs
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
