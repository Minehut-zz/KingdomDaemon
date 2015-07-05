package com.minehut.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import com.minehut.daemon.tools.FileUtil;

public class KingdomServer extends Thread {
	
	private int id = -1, port = -1;
	
	public int startup = 0; //TODO: startup = 73%
	
	public Kingdom kingdom;
	
	public ServerState state;
	
	public enum ServerState {
		SHUTDOWN, RUNNING, STARTING, CRASHED;
	}
	
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
	Tailer tailer;
	@Override
	public void run() {
		if (id==-1) 
			return;
		
		try {
			this.state = ServerState.STARTING;
			FileUtil.editServerProperties(this.kingdom, this.port);
			
		
			String kdDir = "/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id;
			
			new ProcessBuilder("/bin/bash", "-c", "screen -dmLS kingdom" + this.id).start().waitFor();
			//this.sendScreenCommand("echo kingdom")
			this.sendScreenCommand("cd " + kdDir).waitFor();
			
			this.sendScreenCommand("java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui").waitFor();
			
			this.state = ServerState.RUNNING;
			
			
			System.out.println("Kingdom started: kingdom" + this.id + " port: " + this.port);
			
			File log = new File("./screenlog." + (this.id -1));
			if (log.exists()) {
				log.delete();
			}
			FileWriter writer = new FileWriter(log);
			writer.write("STARTING KINGDOM - (KINGDOM DAEMON v4.2.0)\n");
			writer.close();
			
			
			TailerListener listener = new LogListener(this);
		    tailer = new Tailer(log, listener);
		    tailer.run();
			
		    
		    
		    new ProcessBuilder("/bin/bash", "-c", "screen -X -S kingdom" + this.id + " quit").start().waitFor(); //Should kill the screen after the kingdom shuts down
			
		    System.out.println("kingdom" + this.id + " has shutdown, using port " + this.port);
			
		    log.delete();
		    
			//TODO: upload to mongo about port?
			//TODO: open input stream to ./logs/latest.log for parsing server logs
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void parseLog(String line) {
		if (line.contains("Stopping the server") ||
				line.contains("Stopping server")) {
				this.state = ServerState.SHUTDOWN;
				System.out.println(line);
				tailer.stop();
			} else 
			if (line.contains("Done (")) {
				System.out.println(line);
			} else 
			if (line.contains("FAILED TO BIND TO PORT!")) {
				this.state = ServerState.SHUTDOWN;
				System.out.println(line);
				tailer.stop();
			}
	}
	
	public class LogListener extends TailerListenerAdapter {
		KingdomServer server;
		public LogListener(KingdomServer server) {
			this.server = server;
		}
		@Override
		public void handle(String line) { //TODO: More advanced parsing, simple parsing methods for showcase
			server.parseLog(line);
		}
	}
	
	public Process sendScreenCommand(String cmd) throws InterruptedException, IOException {
		return new ProcessBuilder("/bin/bash", "-c", "screen -S kingdom" + this.id + " -p 0 -X stuff '" + cmd + "'^M").start();

	}
	
}
