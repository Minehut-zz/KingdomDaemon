package com.minehut.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.minehut.daemon.tools.FileUtil;

public class KingdomServer extends Thread {
	
	private int id = -1, port = -1;
	
	InputStream is;
	
	ProcessBuilder slave;
	
	Process theProcess;
	
	PrintWriter writer;
	
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
	
	public void runCommand(String cmd) {
		try {
			this.writer.println(cmd);
			this.writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		if (id==-1) 
			return;
		
		try {
			//System.out.println("Starting server..");
			
			File homeDir = new File(this.kingdom.getHomeDir());
			
			FileUtil.editServerProperties(this.kingdom, this.port);
			
			Runtime.getRuntime().exec("screen -dmS kingdom" + this.id, null, homeDir);
			
			
			//String cdCMD = "cd /home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id;
			//System.out.println("cdCMD" + cdCMD);
			//Runtime.getRuntime().exec("screen -S kingdom" + this.id +" -p 0 -X stuff '" + cdCMD + "' ^M");
			String javaCMD = "java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui";
			System.out.println("javaCMD: "+ javaCMD);
			
			Runtime.getRuntime().exec("screen -S kingdom" + this.id + " -p 0 -X stuff '" + javaCMD + "'", null, homeDir);
			
			
			
			
			System.out.println("Kingdom started: kingdom" + this.id + " port: " + this.port);
			//this.theProcess = Runtime.getRuntime().exec("", null, homeDir);
			/*
			this.writer = new PrintWriter(new OutputStreamWriter(this.theProcess.getOutputStream()));
			is = this.theProcess.getInputStream();
			
			//System.out.println("Server started, getting output");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}*/
			
			//System.out.println("server closed, thread finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
