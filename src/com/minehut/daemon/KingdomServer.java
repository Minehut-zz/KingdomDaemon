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
	
	public String startup = "0%"; //TODO: startup = 73%
	
	public Kingdom kingdom;
	
	public ServerState state;
	
	private Tailer tailer;
	
	public boolean old = false;

	private long previousListSendTime;
	
	public enum ServerState {
		SHUTDOWN, RUNNING, STARTING, CRASHED;
	}
	
	public KingdomServer(Kingdom kingdom, int id) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;

		this.previousListSendTime = System.currentTimeMillis();
	}
	
	public KingdomServer(Kingdom kingdom, int id, boolean old) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;
		this.old = old;

		this.previousListSendTime = System.currentTimeMillis();
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
			this.state = ServerState.STARTING;
			FileUtil.editServerProperties(this.kingdom, this.port);

			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "screen -dmLS kingdom" + this.id);
			pb.directory(new File("/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id));
			pb.start().waitFor();
			
			//this.sendScreenCommand("echo kingdom")
			
			this.state = ServerState.RUNNING;
			this.startServer();
			System.out.println("Kingdom started: kingdom" + this.id + " port: " + this.port);
			
			File log = new File("/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id + "/screenlog.0");
			
			FileWriter writer = new FileWriter(log);
			writer.write("STARTING KINGDOM - (KINGDOM DAEMON v4.2.0)\n");
			writer.write("kingdomID:" + this.id + "\n");//TODO: Daemon will use this to grab kingdom data after restarting
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
	
	private void startServer() throws InterruptedException {
		String kdDir = "/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id;
		
		this.sendScreenCommand("cd " + kdDir).waitFor();
		this.sendScreenCommand("java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui").waitFor();
	}
	
	public void parseLog(String line) {

		/* List Command Cooldown */
		if (((System.currentTimeMillis() - this.previousListSendTime) / 1000) > 5) {
			System.out.println("Testing this line for player count --> " + line);
			if (line.contains("There are ") && line.contains(" players online:")) {

				System.out.println("Passed check for player count line. Parsing now...");

				String[] firstPart = line.split("There are ");
				String[] secondPart = firstPart[1].split(" players online:");

				String countString = secondPart[0];
				System.out.println("Player Count: " + countString);

				int count = Integer.parseInt(countString);
			} else {
				System.out.println("This line failed check for player count --> " + line);
				if (!line.startsWith(">")) {
					this.sendScreenCommand("list");
					this.previousListSendTime = System.currentTimeMillis();
				}
			}
		}




		if (line.contains(" INFO]: Stopping server")) {
			this.state = ServerState.SHUTDOWN;
			System.out.println(line);
			tailer.stop();
		} else if (line.contains(" INFO]: Done (")) {
			this.startup = "100%";
			System.out.println(line);
		} else if (line.contains("FAILED TO BIND TO PORT!")) {
			this.state = ServerState.SHUTDOWN;
			System.out.println(line);
			tailer.stop();
		} else if (line.contains("Preparing spawn area: ")) {
			String[] startupArray = line.split("Preparing spawn area: ");
			this.startup = startupArray[1];
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
	
	public Process sendScreenCommand(String cmd) {
		Process process = null;
		try {
			process =  new ProcessBuilder("/bin/bash", "-c", "screen -S kingdom" + this.id + " -p 0 -X stuff '" + cmd + "'^M").start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return process;
	}
	
}
